package com.energyxxer.trident.compiler.lexer;

import com.energyxxer.commodore.defpacks.CategoryDeclaration;
import com.energyxxer.commodore.defpacks.DefinitionPack;
import com.energyxxer.commodore.module.CommandModule;
import com.energyxxer.commodore.standard.StandardDefinitionPacks;
import com.energyxxer.enxlex.lexical_analysis.profiles.*;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSection;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.util.Lazy;
import com.energyxxer.util.StringLocation;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.energyxxer.trident.compiler.lexer.TridentTokens.*;

public class TridentLexerProfile extends LexerProfile {

    public static final Lazy<TridentLexerProfile> INSTANCE = new Lazy<>(() -> new TridentLexerProfile(StandardDefinitionPacks.MINECRAFT_JAVA_LATEST_SNAPSHOT));

    public static final HashMap<TokenType, LexerContext> usefulContexts = new HashMap<>();

    static {
        usefulContexts.put(RESOURCE_LOCATION, new ResourceLocationContext("[a-z0-9_\\.-]","[a-z0-9_/\\.-]", RESOURCE_LOCATION));
        usefulContexts.put(CASE_INSENSITIVE_RESOURCE_LOCATION, new ResourceLocationContext("[a-zA-Z0-9_\\.-]","[a-zA-Z0-9_/\\.-]", CASE_INSENSITIVE_RESOURCE_LOCATION));
    }

    public TridentLexerProfile(DefinitionPack defPack) {
        ArrayList<String> defcategories = new ArrayList<>();
        try {
            defPack.load();
            for(CategoryDeclaration decl : defPack.getDefinedCategories()) {
                //Debug.log("Added category '" + decl.getCategory() + "'");
                defcategories.add(decl.getCategory().toLowerCase());
            }
        } catch (IOException e) {
            defcategories.addAll(Arrays.asList("entity, block, item, particle, enchantment, dimension, effect, difficulty, gamemode, gamerule, slot".split(", ")));
            e.printStackTrace();
        }

        this.initialize(defcategories);
    }

    public TridentLexerProfile(CommandModule module) {
        ArrayList<String> defcategories = new ArrayList<>();
        module.getAllNamespaces().forEach(n -> n.getTypeManager().getAllDictionaries().forEach(d -> {
            if(!defcategories.contains(d.getCategory())) defcategories.add(d.getCategory());
        }));

        this.initialize(defcategories);
    }

    private void initialize(Collection<String> defcategories) {

        //Numbers
        contexts.add(new LexerContext() {

            private Pattern regex = Pattern.compile("([+-]?\\d+(\\.\\d+)?)([bdfsL]?)", Pattern.CASE_INSENSITIVE);

            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                Matcher matcher = regex.matcher(str);

                if(matcher.lookingAt()) {
                    int length = matcher.end();
                    return new ScannerContextResponse(true, str.substring(0,length), (Character.isLetter(str.charAt(length-1)) ? TridentTokens.TYPED_NUMBER : ((str.substring(0, length).contains(".")) ? TridentTokens.REAL_NUMBER : TridentTokens.INTEGER_NUMBER)));
                } else return new ScannerContextResponse(false);
            }

            @Override
            public ScannerContextResponse analyzeExpectingType(String str, TokenType type, LexerProfile profile) {
                Matcher matcher = regex.matcher(str);

                if(matcher.lookingAt()) {
                    int length = matcher.end();

                    TokenType obtainedType = Character.isLetter(str.charAt(length-1)) ? TridentTokens.TYPED_NUMBER : ((str.substring(0, length).contains(".")) ? TridentTokens.REAL_NUMBER : TridentTokens.INTEGER_NUMBER);

                    if(type == TYPED_NUMBER) obtainedType = type;
                    else if(type == REAL_NUMBER && obtainedType == INTEGER_NUMBER) obtainedType = REAL_NUMBER;

                    if(type == obtainedType) {
                        return new ScannerContextResponse(true, str.substring(0,length), type);
                    } else return new ScannerContextResponse(false);
                } else return new ScannerContextResponse(false);
            }

            @Override
            public ContextCondition getCondition() {
                return ContextCondition.LEADING_WHITESPACE;
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Arrays.asList(TridentTokens.TYPED_NUMBER, TridentTokens.INTEGER_NUMBER, TridentTokens.REAL_NUMBER);
            }
        });

        //Short numbers ('.0', '.5' ...)
        contexts.add(new LexerContext() {

            private Pattern regex = Pattern.compile("[+-]?\\d*(\\.\\d+)?", Pattern.CASE_INSENSITIVE);

            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                return new ScannerContextResponse(false);
            }

            @Override
            public ScannerContextResponse analyzeExpectingType(String str, TokenType type, LexerProfile profile) {
                Matcher matcher = regex.matcher(str);

                if(matcher.lookingAt() && matcher.end() > 0) {
                    int length = matcher.end();
                    return new ScannerContextResponse(true, str.substring(0,length), SHORT_REAL_NUMBER);
                } else return new ScannerContextResponse(false);
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Collections.singleton(SHORT_REAL_NUMBER);
            }
        });

        //Time literal
        contexts.add(new LexerContext() {

            private Pattern regex = Pattern.compile("(\\d+(\\.\\d+)?[tsd]?)");

            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                Matcher matcher = regex.matcher(str);

                if(matcher.lookingAt()) {
                    int length = matcher.end();
                    return new ScannerContextResponse(true, str.substring(0,length), TridentTokens.TIME);
                } else return new ScannerContextResponse(false);
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Collections.singletonList(TIME);
            }
        });

        contexts.add(new StringTypeMatchLexerContext(new String[] { ".", ",", ":", "=", "(", ")", "[", "]", "{", "}", "<", ">", "~", "^", "!", "#" },
                new TokenType[] { DOT, COMMA, COLON, EQUALS, BRACE, BRACE, BRACE, BRACE, BRACE, BRACE, BRACE, BRACE, TILDE, CARET, NOT, HASH }
                ));

        contexts.add(new StringMatchLexerContext(TridentTokens.SCOREBOARD_OPERATOR, "%=", "*=", "+=", "-=", "/=", "><", "=", ">", "<"));

        //Glue
        contexts.add(new LexerContext() {
            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                return new ScannerContextResponse(false);
            }

            @Override
            public ScannerContextResponse analyzeExpectingType(String str, TokenType type, LexerProfile profile) {
                if(str.length() > 0 && Character.isWhitespace(str.charAt(0))) return new ScannerContextResponse(false);
                return new ScannerContextResponse(true, "", TridentTokens.GLUE);
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Collections.singletonList(GLUE);
            }

            @Override
            public boolean ignoreLeadingWhitespace() {
                return false;
            }
        });

        //Line glue
        contexts.add(new LexerContext() {
            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                return new ScannerContextResponse(false);
            }

            @Override
            public ScannerContextResponse analyzeExpectingType(String str, TokenType type, LexerProfile profile) {
                for(int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if(c == '\n') return new ScannerContextResponse(false);
                    if(!Character.isWhitespace(c)) return new ScannerContextResponse(true, "", TridentTokens.LINE_GLUE);
                }
                return new ScannerContextResponse(true, "", TridentTokens.LINE_GLUE);
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Collections.singletonList(LINE_GLUE);
            }

            @Override
            public boolean ignoreLeadingWhitespace() {
                return false;
            }
        });

        //Verbatim commands
        contexts.add(new LexerContext() {

            String header = "/";

            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                return new ScannerContextResponse(false);
            }

            @Override
            public ScannerContextResponse analyzeExpectingType(String str, TokenType type, LexerProfile profile) {
                if(str.length() <= 0) return new ScannerContextResponse(false);

                if(type == VERBATIM_COMMAND_HEADER) {
                    if(str.startsWith("/")) return new ScannerContextResponse(true, "/", VERBATIM_COMMAND_HEADER);
                    else return new ScannerContextResponse(false);
                } else {
                    if(str.startsWith("$")) return new ScannerContextResponse(false);
                    int endIndex = str.length();
                    if(str.contains("\n")) {
                        endIndex = str.indexOf("\n");
                    }

                    return new ScannerContextResponse(true, str.substring(0, endIndex), VERBATIM_COMMAND);
                }
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Arrays.asList(VERBATIM_COMMAND, VERBATIM_COMMAND_HEADER);
            }
        });

        //Trailing string
        contexts.add(new LexerContext() {

            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                return new ScannerContextResponse(false );
            }

            @Override
            public ScannerContextResponse analyzeExpectingType(String str, TokenType type, LexerProfile profile) {
                if(str.contains("\n")) {
                    return new ScannerContextResponse(true, str.substring(0, str.indexOf("\n")), TRAILING_STRING);
                } else return new ScannerContextResponse(true, str, TRAILING_STRING);
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Collections.singletonList(TRAILING_STRING);
            }
        });

        //String literals
        contexts.add(new LexerContext() {

            String delimiters = "\"";

            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                if(str.length() <= 0) return new ScannerContextResponse(false);
                char startingCharacter = str.charAt(0);

                if(delimiters.contains(Character.toString(startingCharacter))) {

                    StringBuilder token = new StringBuilder(Character.toString(startingCharacter));
                    StringLocation end = new StringLocation(1,0,1);

                    HashMap<TokenSection, String> escapedChars = new HashMap<>();

                    for(int i = 1; i < str.length(); i++) {
                        char c = str.charAt(i);

                        if(c == '\n') {
                            end.line++;
                            end.column = 0;
                        } else {
                            end.column++;
                        }
                        end.index++;

                        if(c == '\n') {
                            ScannerContextResponse response = new ScannerContextResponse(true, token.toString(), end, TridentTokens.STRING_LITERAL, escapedChars);
                            response.setError("Illegal line end in string literal", i, 1);
                            return response;
                        }
                        token.append(c);
                        if(c == '\\') {
                            token.append(str.charAt(i+1));
                            escapedChars.put(new TokenSection(i,2), "string_literal.escape");
                            i++;
                        } else if(c == startingCharacter) {
                            return new ScannerContextResponse(true, token.toString(), end, TridentTokens.STRING_LITERAL, escapedChars);
                        }
                    }
                    //Unexpected end of input
                    ScannerContextResponse response = new ScannerContextResponse(true, token.toString(), end, TridentTokens.STRING_LITERAL, escapedChars);
                    response.setError("Unexpected end of input", str.length()-1, 1);
                    return response;
                } else return new ScannerContextResponse(false);
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Collections.singletonList(STRING_LITERAL);
            }
        });

        //Selector headers
        contexts.add(new LexerContext() {

            private String headers = "pears";

            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                if(str.length() < 2) return new ScannerContextResponse(false);
                if(!str.startsWith("@")) return new ScannerContextResponse(false);
                if(headers.contains(str.charAt(1) + "")) {
                    return new ScannerContextResponse(true, str.substring(0,2), SELECTOR_HEADER);
                }
                return new ScannerContextResponse(false);
            }

            @Override
            public ContextCondition getCondition() {
                return ContextCondition.LEADING_WHITESPACE;
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Collections.singletonList(SELECTOR_HEADER);
            }
        });

        //Resource Locations
        contexts.add(usefulContexts.get(RESOURCE_LOCATION));
        contexts.add(usefulContexts.get(CASE_INSENSITIVE_RESOURCE_LOCATION));

        //Comments
        contexts.add(new LexerContext() {
            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                if(!str.startsWith("#")) return new ScannerContextResponse(false);
                if(str.contains("\n")) {
                    HashMap<TokenSection, String> subSections = new HashMap<>();
                    if(str.startsWith("#:")) {
                        subSections.put(new TokenSection(0, 2), "deprecated_syntax");
                    }
                    return new ScannerContextResponse(true, str.substring(0, str.indexOf("\n")), COMMENT, subSections);
                } else return new ScannerContextResponse(true, str, COMMENT);
            }

            @Override
            public ContextCondition getCondition() {
                return ContextCondition.LINE_START;
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Collections.singletonList(COMMENT);
            }
        });

        //Directive headers
        contexts.add(new StringMatchLexerContext(DIRECTIVE_HEADER, "@").setOnlyWhenExpected(true));

        //Swizzle
        contexts.add(new LexerContext() {
            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                return new ScannerContextResponse(false);
            }

            private String extractIdentifierA(String str) {
                int i = 0;
                while (i < str.length() && Character.toString(str.charAt(i)).matches("[a-zA-Z0-9._\\-+]")) {
                    i++;
                }
                return str.substring(0, i);
            }

            @Override
            public ScannerContextResponse analyzeExpectingType(String str, TokenType type, LexerProfile profile) {
                str = extractIdentifierA(str);
                if(str.length() == 0 || str.length() > 3) return new ScannerContextResponse(false);

                ArrayList<Character> possibleChars = new ArrayList<>();
                possibleChars.add('x');
                possibleChars.add('y');
                possibleChars.add('z');
                for(int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if(possibleChars.contains(c)) {
                        possibleChars.remove((Character)c);
                    } else {
                        return new ScannerContextResponse(false);
                    }
                }

                return new ScannerContextResponse(true, str, SWIZZLE);
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Collections.singletonList(SWIZZLE);
            }
        });

        contexts.add(new IdentifierLexerContext(IDENTIFIER_TYPE_A, "[a-zA-Z0-9._\\-+]"));
        contexts.add(new IdentifierLexerContext(IDENTIFIER_TYPE_B, "[^@\\$\\s]\\S*"));
        contexts.add(new IdentifierLexerContext(IDENTIFIER_TYPE_C, "\\S*"));
        contexts.add(new IdentifierLexerContext(IDENTIFIER_TYPE_D, "[a-zA-Z0-9_\\-+]"));

        //contexts.add(new IdentifierLexerContext(IDENTIFIER_TYPE_X, "[a-zA-Z0-9._]", "[a-zA-Z._]"));

        contexts.add(new LexerContext() {

            private List<String> reservedWords = Arrays.asList("integer", "real", "boolean", "string", "entity", "block", "item", "text_component", "nbt", "nbt_value", "nbt_path", "coordinate", "int_range", "real_range", "var", "define", "do", "while", "within", "for", "switch", "function", "if", "else", "try", "catch", "tdndebug", "new");

            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                return new ScannerContextResponse(false);
            }

            @Override
            public ScannerContextResponse analyzeExpectingType(String str, TokenType type, LexerProfile profile) {
                int i = 0;
                while(i < str.length() && (
                        (i == 0 && str.substring(i,i+1).matches("[a-zA-Z_]")
                                ||
                                (i > 0 && str.substring(i,i+1).matches("[a-zA-Z0-9_]"))
                        ))) {
                    i++;
                }
                str = str.substring(0, i);
                if(i > 0 && !reservedWords.contains(str)) return new ScannerContextResponse(true, str, type);
                return new ScannerContextResponse(false);
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Collections.singletonList(IDENTIFIER_TYPE_X);
            }
        });

        contexts.add(new LexerContext() {

            @Override
            public ScannerContextResponse analyze(String str, LexerProfile profile) {
                return new ScannerContextResponse(false);
            }

            @Override
            public ScannerContextResponse analyzeExpectingType(String str, TokenType type, LexerProfile profile) {
                int i = 0;
                while(i < str.length() && (
                        (i == 0 && str.substring(i,i+1).matches("[a-zA-Z_]")
                                ||
                                (i > 0 && str.substring(i,i+1).matches("[a-zA-Z0-9_]"))
                        ))) {
                    i++;
                }
                str = str.substring(0, i);
                if(i > 0) return new ScannerContextResponse(true, str, type);
                return new ScannerContextResponse(false);
            }

            @Override
            public Collection<TokenType> getHandledTypes() {
                return Collections.singletonList(IDENTIFIER_TYPE_Y);
            }
        });

        contexts.add(new StringMatchLexerContext(DIRECTIVE_ON_KEYWORD, "compile"));
        contexts.add(new StringMatchLexerContext(KEYWORD, "var", "define", "mark", "do", "while", "within", "using", "as", "append", "for", "in", "switch", "function", "if", "else", "try", "catch", "tdndebug"));
        contexts.add(new StringMatchLexerContext(SYNTACTIC_SUGAR, "isset"));
        contexts.add(new StringMatchLexerContext(BOOLEAN, "true", "false"));
        contexts.add(new IdentifierLexerContext(COMMAND_HEADER, "[a-zA-Z0-9._\\-+]"));
        contexts.add(new IdentifierLexerContext(MODIFIER_HEADER, "[a-zA-Z0-9._\\-+]"));

        contexts.add(new StringMatchLexerContext(SYMBOL, "*", "<=", ">=", "<", ">", "!=", "=", "$", ";"));
        contexts.add(new StringMatchLexerContext(ARROW, "->"));

        contexts.add(new StringMatchLexerContext(COMPILER_OPERATOR, "+=", "-=", "*=", "/=", "%=", "+", "-", "*", "/", "%", "<=", ">=", "<", ">", "==", "!=", "=", "&&", "||", "&", "|", "^"));
        contexts.add(new StringMatchLexerContext(COMPILER_POSTFIX_OPERATOR, "++", "--"));
        contexts.add(new StringMatchLexerContext(COMPILER_PREFIX_OPERATOR, "++", "--", "!"));

        contexts.add(new StringMatchLexerContext(SORTING, "nearest", "farthest", "arbitrary", "random"));
        contexts.add(new StringMatchLexerContext(NUMERIC_DATA_TYPE, "byte", "double", "float", "int", "long", "short"));
        contexts.add(new StringMatchLexerContext(SOUND_CHANNEL, "ambient", "block", "hostile", "master", "music", "neutral", "player", "record", "voice", "weather"));
        contexts.add(new StringMatchLexerContext(ANCHOR, "feet", "eyes"));

        contexts.add(new StringMatchLexerContext(NULL, "null"));

        contexts.add(new StringMatchLexerContext(DEFINITION_CATEGORY, defcategories.toArray(new String[0])));

    }

    @Override
    public boolean canMerge(char ch0, char ch1) {
        return isValidIdentifierPart(ch0) && isValidIdentifierPart(ch1);
    }

    private boolean isValidIdentifierPart(char ch) {
        return ch != '$' && Character.isJavaIdentifierPart(ch);
    }

    @Override
    public boolean useNewlineTokens() {
        return true;
    }

    @Override
    public void putHeaderInfo(Token header) {
        header.attributes.put("TYPE","tdn");
        header.attributes.put("DESC","Trident Function File");
    }

    static class ResourceLocationContext implements LexerContext {
        private final String acceptedNamespaceChars;
        private final String acceptedPathChars;

        private final TokenType tokenType;

        public ResourceLocationContext(String acceptedNamespaceChars, String acceptedPathChars, TokenType tokenType) {
            this.acceptedNamespaceChars = acceptedNamespaceChars;
            this.acceptedPathChars = acceptedPathChars;

            this.tokenType = tokenType;
        }

        @Override
        public ScannerContextResponse analyze(String str, LexerProfile profile) {
            return new ScannerContextResponse(false);
        }

        @Override
        public ScannerContextResponse analyzeExpectingType(String str, TokenType type, LexerProfile profile) {
            boolean namespaceFound = false;
            int nonNamespaceCharIndex = -1;
            int index = 0;
            for(char c : str.toCharArray()) {
                boolean validNs = (""+c).matches(acceptedNamespaceChars);
                boolean validPt = (""+c).matches(acceptedPathChars);
                if(!validNs && !validPt) {
                    if(!namespaceFound && c == ':') {
                        namespaceFound = true;
                        if(nonNamespaceCharIndex >= 0) break;
                    } else break;
                } else {
                    if(!namespaceFound && !validNs) {
                        if(nonNamespaceCharIndex <= 0) nonNamespaceCharIndex = index;
                    } else if(namespaceFound && !validPt) {
                        break;
                    }
                }
                if(Character.isWhitespace(c)) break;
                index++;
            }
            if(namespaceFound && nonNamespaceCharIndex >= 0) index = nonNamespaceCharIndex;
            if(index == 0) return new ScannerContextResponse(false);
            else {
                HashMap<TokenSection, String> tokenSections = new HashMap<>();
                boolean relative = str.startsWith("/");
                if(relative) {
                    tokenSections.put(new TokenSection(0, 1), "resource_location.relative");
                }

                return new ScannerContextResponse(true, str.substring(0, index), tokenType, tokenSections);
            }
        }

        @Override
        public Collection<TokenType> getHandledTypes() {
            return Collections.singletonList(tokenType);
        }
    }
}
