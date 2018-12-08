package com.energyxxer.trident.compiler.commands.parsers.constructs;

import com.energyxxer.commodore.CommandUtils;
import com.energyxxer.commodore.block.Block;
import com.energyxxer.commodore.block.Blockstate;
import com.energyxxer.commodore.functionlogic.nbt.TagCompound;
import com.energyxxer.commodore.functionlogic.score.Objective;
import com.energyxxer.commodore.item.Item;
import com.energyxxer.commodore.module.Namespace;
import com.energyxxer.commodore.tags.BlockTag;
import com.energyxxer.commodore.tags.ItemTag;
import com.energyxxer.commodore.tags.TagGroup;
import com.energyxxer.commodore.tags.TagManager;
import com.energyxxer.commodore.types.Type;
import com.energyxxer.commodore.types.TypeDictionary;
import com.energyxxer.commodore.types.defaults.FunctionReference;
import com.energyxxer.commodore.types.defaults.TypeManager;
import com.energyxxer.commodore.util.NumberRange;
import com.energyxxer.commodore.util.TimeSpan;
import com.energyxxer.enxlex.pattern_matching.structures.TokenList;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.pattern_matching.structures.TokenStructure;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.NoticeType;
import com.energyxxer.trident.compiler.TridentCompiler;
import com.energyxxer.trident.compiler.TridentUtil;
import com.energyxxer.trident.compiler.commands.EntryParsingException;
import com.energyxxer.trident.compiler.commands.parsers.general.ParserManager;
import com.energyxxer.trident.compiler.commands.parsers.variable_functions.VariableFunction;
import com.energyxxer.trident.compiler.semantics.Symbol;
import com.energyxxer.trident.compiler.semantics.custom.entities.CustomEntity;

import java.util.Arrays;
import java.util.List;

public class CommonParsers {
    public static Type parseEntityType(TokenPattern<?> id, TridentCompiler compiler) {
        return parseType(id, compiler, m -> m.entity);
    }
    public static Object parseEntityReference(TokenPattern<?> id, TridentCompiler compiler) {
        if(id instanceof TokenStructure && ((TokenStructure) id).getContents().getName().equals("VARIABLE_MARKER")) {
            return retrieveSymbol(((TokenStructure) id).getContents(), compiler, Type.class, CustomEntity.class);
        } else return parseType(id, compiler, m -> m.entity);
    }
    public static Type parseItemType(TokenPattern<?> id, TridentCompiler compiler) {
        return parseType(id, compiler, m -> m.item);
    }
    public static Type parseBlockType(TokenPattern<?> id, TridentCompiler compiler) {
        return parseType(id, compiler, m -> m.block);
    }
    public static Type parseType(TokenPattern<?> id, TridentCompiler compiler, TypeGroupPicker picker) {
        if(id == null) return null;
        if(id instanceof TokenStructure && ((TokenStructure) id).getContents().getName().equals("VARIABLE_MARKER")) {
            return retrieveSymbol(((TokenStructure) id).getContents(), compiler, Type.class);
        }
        TridentUtil.ResourceLocation typeLoc = new TridentUtil.ResourceLocation(id);
        return picker.pick(compiler.getModule().getNamespace(typeLoc.namespace).types).get(typeLoc.body);
    }

    public static Type parseTag(TokenPattern<?> id, TridentCompiler compiler, String category, TypeGroupPicker typePicker, TagGroupPicker tagPicker) {
        if(id == null) return null;
        boolean isTag = id.find("") != null;
        TridentUtil.ResourceLocation typeLoc = new TridentUtil.ResourceLocation(id.find("RESOURCE_LOCATION").flatten(false));
        Namespace ns = compiler.getModule().getNamespace(typeLoc.namespace);

        Type type = null;

        if(isTag) {
            type = tagPicker.pick(ns.tags).get(typeLoc.body);
        } else {
            type = typePicker.pick(ns.types).get(typeLoc.body);
        }

        if(type == null) {
            if(isTag) {
                compiler.getReport().addNotice(new Notice(NoticeType.ERROR, "No such " + category + " tag exists: #" + typeLoc, id));
            } else {
                compiler.getReport().addNotice(new Notice(NoticeType.ERROR, "No such " + category + " type exists: " + typeLoc, id));
            }
            throw new EntryParsingException();
        }

        return type;
    }

    public static Type parseFunctionTag(TokenPattern<?> id, TridentCompiler compiler) {
        if(id == null) return null;
        boolean isTag = id.find("") != null;
        TridentUtil.ResourceLocation typeLoc = new TridentUtil.ResourceLocation(id.find("RESOURCE_LOCATION").flatten(false));
        Namespace ns = compiler.getModule().getNamespace(typeLoc.namespace);

        Type type = null;

        if(isTag) {
            type = ns.tags.functionTags.get(typeLoc.body);
        } else if(ns.functions.contains(typeLoc.body)) {
            type = new FunctionReference(ns.functions.get(typeLoc.body));
        }

        if(type == null) {
            if(isTag) {
                compiler.getReport().addNotice(new Notice(NoticeType.ERROR, "No such function tag exists: #" + typeLoc, id));
            } else {
                compiler.getReport().addNotice(new Notice(NoticeType.ERROR, "No such function exists: " + typeLoc, id));
            }
            throw new EntryParsingException();
        }

        return type;
    }

    public static ItemTag parseItemTag(TokenPattern<?> id, TridentCompiler compiler) {
        TridentUtil.ResourceLocation tagLoc = new TridentUtil.ResourceLocation(id.flattenTokens().get(0).value);
        ItemTag returned = compiler.getModule().getNamespace(tagLoc.namespace).tags.itemTags.get(tagLoc.body);
        if(returned == null) {
            compiler.getReport().addNotice(new Notice(NoticeType.ERROR, "No such item tag exists: #" + tagLoc, id));
            throw new EntryParsingException();
        }
        return returned;
    }

    public static BlockTag parseBlockTag(TokenPattern<?> id, TridentCompiler compiler) {
        TridentUtil.ResourceLocation tagLoc = new TridentUtil.ResourceLocation(id.flattenTokens().get(0).value);
        BlockTag returned = compiler.getModule().getNamespace(tagLoc.namespace).tags.blockTags.get(tagLoc.body);
        if(returned == null) {
            compiler.getReport().addNotice(new Notice(NoticeType.ERROR, "No such block tag exists: #" + tagLoc, id));
            throw new EntryParsingException();
        }
        return returned;
    }

    public static Item parseItem(TokenPattern<?> pattern, TridentCompiler compiler) {
        if(pattern.getName().equals("ITEM_TAGGED") || pattern.getName().equals("ITEM")) return parseItem(((TokenStructure) pattern).getContents(), compiler);

        boolean isStandalone = pattern.getName().equals("CONCRETE_RESOURCE");

        Type type;

        if(isStandalone) {
            type = parseItemType(pattern.find("RESOURCE_NAME.ITEM_ID"), compiler);
        } else {
            type = parseItemTag(pattern.find("RESOURCE_NAME.RESOURCE_LOCATION"), compiler);
        }

        TagCompound tag = NBTParser.parseCompound(pattern.find(".NBT_COMPOUND"), compiler);
        return new Item(type, tag);
    }

    public static Block parseBlock(TokenPattern<?> pattern, TridentCompiler compiler) {
        if(pattern.getName().equals("BLOCK_TAGGED") || pattern.getName().equals("BLOCK")) return parseBlock(((TokenStructure) pattern).getContents(), compiler);

        boolean isStandalone = pattern.getName().equals("CONCRETE_RESOURCE");

        Type type;

        if(isStandalone) {
            type = parseBlockType(pattern.find("RESOURCE_NAME.BLOCK_ID"), compiler);
        } else {
            type = parseBlockTag(pattern.find("RESOURCE_NAME.RESOURCE_LOCATION"), compiler);
        }


        Blockstate state = parseBlockstate(pattern.find("BLOCKSTATE_CLAUSE.BLOCKSTATE"));
        TagCompound tag = NBTParser.parseCompound(pattern.find("NBT_CLAUSE.NBT_COMPOUND"), compiler);
        return new Block(type, state, tag);
    }

    public static Blockstate parseBlockstate(TokenPattern<?> pattern) {
        if(pattern == null) return null;
        TokenPattern<?> rawList = pattern.find("BLOCKSTATE_LIST");

        Blockstate blockstate = null;
        if(rawList instanceof TokenList) {
            TokenList list = (TokenList) rawList;
            for(TokenPattern<?> inner : list.getContents()) {
                if(inner.getName().equals("BLOCKSTATE_PROPERTY")) {
                    String key = inner.find("BLOCKSTATE_PROPERTY_KEY").flattenTokens().get(0).value;
                    String value = inner.find("BLOCKSTATE_PROPERTY_VALUE").flattenTokens().get(0).value;

                    if(blockstate == null) blockstate = new Blockstate();
                    blockstate.put(key, value);
                }
            }
        }
        return blockstate;
    }

    public static NumberRange<Integer> parseIntRange(TokenPattern<?> pattern) {
        TokenPattern<?> exact = pattern.find("EXACT");
        if(exact != null) return new NumberRange<>(Integer.parseInt(pattern.flatten(false)));
        List<TokenPattern<?>> minRaw = pattern.searchByName("MIN");
        List<TokenPattern<?>> maxRaw = pattern.deepSearchByName("MAX");
        Integer min = null;
        Integer max = null;
        if(!minRaw.isEmpty()) {
            min = Integer.parseInt(minRaw.get(0).flatten(false));
        }
        if(!maxRaw.isEmpty()) {
            max = Integer.parseInt(maxRaw.get(0).flatten(false));
        }
        return new NumberRange<>(min, max);
    }

    public static NumberRange<Double> parseRealRange(TokenPattern<?> pattern) {
        TokenPattern<?> exact = pattern.find("EXACT");
        if(exact != null) return new NumberRange<>(Double.parseDouble(pattern.flatten(false)));
        List<TokenPattern<?>> minRaw = pattern.searchByName("MIN");
        List<TokenPattern<?>> maxRaw = pattern.deepSearchByName("MAX");
        Double min = null;
        Double max = null;
        if(!minRaw.isEmpty()) {
            min = Double.parseDouble(minRaw.get(0).flatten(false));
        }
        if(!maxRaw.isEmpty()) {
            max = Double.parseDouble(maxRaw.get(0).flatten(false));
        }
        return new NumberRange<>(min, max);
    }

    public static Objective parseObjective(TokenPattern<?> pattern, TridentCompiler compiler) {
        String name = pattern.flatten(true);
        if(!compiler.getModule().getObjectiveManager().contains(name)) {
            compiler.getReport().addNotice(new Notice(NoticeType.WARNING, "Unregistered objective name '" + name + "'", pattern));
            return compiler.getModule().getObjectiveManager().create(name, true);
        } else {
            return compiler.getModule().getObjectiveManager().get(name);
        }
    }

    public static String parseStringLiteralOrIdentifierA(TokenPattern<?> pattern) {
        String str = "";
        if(pattern != null) {
            str = pattern.flatten(false);
            if(!pattern.deepSearchByName("STRING_LITERAL").isEmpty()) {
                str = CommandUtils.parseQuotedString(str);
            }
        }
        return str;
    }

    /**
     * CommonParsers should not be instantiated.
     * */
    private CommonParsers() {
    }

    public static TimeSpan parseTime(TokenPattern<?> time, TridentCompiler compiler) {
        String raw = time.flatten(false);
        TimeSpan.Units units = TimeSpan.Units.TICKS;

        for(TimeSpan.Units unitValue : TimeSpan.Units.values()) {
            if(raw.endsWith(unitValue.suffix)) {
                units = unitValue;
                raw = raw.substring(0, raw.length() - unitValue.suffix.length());
                break;
            }
        }

        return new TimeSpan(Double.parseDouble(raw), units);
    }

    public static Object parseAnything(TokenPattern<?> pattern, TridentCompiler compiler) {
        switch(pattern.getName()) {
            case "INTEGER": return Integer.parseInt(pattern.flatten(false));
            case "REAL": return Double.parseDouble(pattern.flatten(false));
            case "STRING_LITERAL": return CommandUtils.parseQuotedString(pattern.flatten(false));
            case "BOOLEAN": return pattern.flatten(false).equals("true");
            case "ENTITY": return EntityParser.parseEntity(pattern, compiler);
            case "BLOCK_TAGGED":
            case "BLOCK":
                return parseBlock(pattern, compiler);
            case "ITEM_TAGGED":
            case "ITEM":
                return parseItem(pattern, compiler);
            case "COORDINATE_SET":
                return CoordinateParser.parse(pattern, compiler);
            case "NBT_COMPOUND":
                return NBTParser.parseCompound(pattern, compiler);
            case "NBT_PATH":
                return NBTParser.parsePath(pattern, compiler);
            case "TEXT_COMPONENT":
                return TextParser.parseTextComponent(pattern, compiler);
            default: {
                compiler.getReport().addNotice(new Notice(NoticeType.ERROR, "Unknown value grammar name: '" + pattern.getName() + "'"));
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T retrieveSymbol(TokenPattern<?> pattern, TridentCompiler compiler, Class<T> expected) {
        Object obj = retrieveSymbol(pattern, compiler);
        if(expected.isInstance(obj)) {
            return (T) obj;
        } else {
            compiler.getReport().addNotice(new Notice(NoticeType.ERROR, "Symbol '" + pattern.find("VARIABLE_NAME").flatten(false) + "' does not contain a value of type " + expected.getSimpleName(), pattern));
            throw new EntryParsingException();
        }
    }

    @SuppressWarnings("unchecked")
    public static Object retrieveSymbol(TokenPattern<?> pattern, TridentCompiler compiler, Class... expected) {
        Object obj = retrieveSymbol(pattern, compiler);
        for(Class cls : expected) {
            if(cls.isInstance(obj)) return obj;
        }
        compiler.getReport().addNotice(new Notice(NoticeType.ERROR, "Symbol '" + pattern.find("VARIABLE_NAME").flatten(false) + "' does not contain a value of type " + Arrays.asList(expected).map((Class c) -> c.getSimpleName()).toSet().join(", "), pattern));
        throw new EntryParsingException();
    }

    public static Object retrieveSymbol(TokenPattern<?> pattern, TridentCompiler compiler) {
        String name = pattern.find("VARIABLE_NAME").flatten(false);
        var modPattern = pattern.find("VARIABLE_MODIFIER.VARIABLE_MODIFIER_FUNCTION");
        String mod = modPattern != null ? modPattern.flatten(false) : null;

        Symbol symbol = compiler.getStack().search(name);

        if(symbol != null) {

            if(mod != null) {
                VariableFunction varFunction = ParserManager.getParser(VariableFunction.class, mod);
                if (varFunction != null) return varFunction.process(symbol.getValue(), pattern, compiler);
            }

            return symbol.getValue();
        } else {
            compiler.getReport().addNotice(new Notice(NoticeType.ERROR, "Symbol '" + name + "' is not defined", pattern));
            throw new EntryParsingException();
        }
    }

    public interface TypeGroupPicker {
        TypeDictionary<?> pick(TypeManager m);
    }

    public interface TagGroupPicker {
        TagGroup<?> pick(TagManager m);
    }
}