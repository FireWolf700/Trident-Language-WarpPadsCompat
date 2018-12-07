package com.energyxxer.trident.compiler.lexer;

import com.energyxxer.commodore.standard.StandardDefinitionPacks;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.*;
import com.energyxxer.util.logger.Debug;

import java.io.IOException;
import java.util.HashMap;

import static com.energyxxer.trident.compiler.lexer.TridentTokens.*;

public class TridentProductions {

    public static final LazyTokenStructureMatch FILE;
    public static final LazyTokenStructureMatch ENTRY;

    public static final LazyTokenItemMatch COMMENT_S;
    public static final LazyTokenItemMatch VERBATIM_COMMAND_S;
    public static final LazyTokenGroupMatch DIRECTIVE;
    public static final LazyTokenStructureMatch INSTRUCTION;
    public static final LazyTokenStructureMatch COMMAND;
    public static final LazyTokenStructureMatch MODIFIER;

    public static final LazyTokenGroupMatch RESOURCE_LOCATION_TAGGED;

    public static final LazyTokenStructureMatch SELECTOR;
    public static final LazyTokenStructureMatch SELECTOR_ARGUMENT;
    public static final LazyTokenStructureMatch TEXT_COMPONENT;

    public static final LazyTokenStructureMatch PLAYER_NAME;

    public static final LazyTokenStructureMatch TEXT_COLOR;

    public static final LazyTokenStructureMatch INTEGER_NUMBER_RANGE = new LazyTokenStructureMatch("INTEGER_NUMBER_RANGE");
    public static final LazyTokenStructureMatch REAL_NUMBER_RANGE = new LazyTokenStructureMatch("REAL_NUMBER_RANGE");

    public static final LazyTokenStructureMatch NBT_COMPOUND = new LazyTokenStructureMatch("NBT_COMPOUND");
    public static final LazyTokenStructureMatch NBT_LIST = new LazyTokenStructureMatch("NBT_LIST");
    public static final LazyTokenStructureMatch NBT_VALUE = new LazyTokenStructureMatch("NBT_VALUE");

    public static final LazyTokenStructureMatch NBT_PATH = new LazyTokenStructureMatch("NBT_PATH");
    
    public static final LazyTokenStructureMatch SINGLE_COORDINATE = new LazyTokenStructureMatch("SINGLE_COORDINATE");
    public static final LazyTokenStructureMatch ABSOLUTE_COORDINATE = new LazyTokenStructureMatch("ABSOLUTE_COORDINATE");
    public static final LazyTokenStructureMatch RELATIVE_COORDINATE = new LazyTokenStructureMatch("RELATIVE_COORDINATE");
    public static final LazyTokenStructureMatch LOCAL_COORDINATE = new LazyTokenStructureMatch("LOCAL_COORDINATE");
    public static final LazyTokenStructureMatch MIXABLE_COORDINATE = new LazyTokenStructureMatch("MIXABLE_COORDINATE");
    public static final LazyTokenStructureMatch COORDINATE_SET = new LazyTokenStructureMatch("COORDINATE_SET");
    public static final LazyTokenStructureMatch TWO_COORDINATE_SET = new LazyTokenStructureMatch("TWO_COORDINATE_SET");

    public static final LazyTokenStructureMatch BLOCKSTATE = new LazyTokenStructureMatch("BLOCKSTATE");
    public static final LazyTokenStructureMatch BLOCK = new LazyTokenStructureMatch("BLOCK");
    public static final LazyTokenStructureMatch BLOCK_TAGGED = new LazyTokenStructureMatch("BLOCK_TAGGED");
    public static final LazyTokenStructureMatch ITEM = new LazyTokenStructureMatch("ITEM");
    public static final LazyTokenStructureMatch ITEM_TAGGED = new LazyTokenStructureMatch("ITEM_TAGGED");

    public static final LazyTokenStructureMatch PARTICLE = new LazyTokenStructureMatch("PARTICLE");

    public static final LazyTokenStructureMatch BLOCK_ID = new LazyTokenStructureMatch("BLOCK_ID");
    public static final LazyTokenStructureMatch ITEM_ID = new LazyTokenStructureMatch("ITEM_ID");
    public static final LazyTokenStructureMatch ENTITY_ID = new LazyTokenStructureMatch("ENTITY_ID");
    public static final LazyTokenStructureMatch EFFECT_ID = new LazyTokenStructureMatch("EFFECT_ID");
    public static final LazyTokenStructureMatch PARTICLE_ID = new LazyTokenStructureMatch("PARTICLE_ID");
    public static final LazyTokenStructureMatch ENCHANTMENT_ID = new LazyTokenStructureMatch("ENCHANTMENT_ID");
    public static final LazyTokenStructureMatch DIMENSION_ID = new LazyTokenStructureMatch("DIMENSION_ID");
    public static final LazyTokenStructureMatch SLOT_ID = new LazyTokenStructureMatch("SLOT_ID");

    public static final LazyTokenStructureMatch GAMEMODE = new LazyTokenStructureMatch("GAMEMODE");
    public static final LazyTokenStructureMatch GAMERULE = new LazyTokenStructureMatch("GAMERULE");
    public static final LazyTokenStructureMatch GAMERULE_SETTER = new LazyTokenStructureMatch("GAMERULE_SETTER");
    public static final LazyTokenStructureMatch STRUCTURE = new LazyTokenStructureMatch("STRUCTURE");
    public static final LazyTokenStructureMatch DIFFICULTY = new LazyTokenStructureMatch("DIFFICULTY");

    public static final LazyTokenStructureMatch STRING_LITERAL_OR_IDENTIFIER_A = new LazyTokenStructureMatch("STRING_LITERAL_OR_IDENTIFIER_A");

    //grouped arguments
    public static final LazyTokenStructureMatch ENTITY = new LazyTokenStructureMatch("ENTITY");
    public static final LazyTokenStructureMatch VARIABLE_MARKER = choice(
            group(symbol("$"), glue(), brace("{"), ofType(CASE_INSENSITIVE_RESOURCE_LOCATION).setName("VARIABLE_NAME"), brace("}")),
            group(symbol("$"), glue(), ofType(CASE_INSENSITIVE_RESOURCE_LOCATION).setName("VARIABLE_NAME"))
    ).setName("VARIABLE_MARKER");
    public static final LazyTokenStructureMatch POINTER;

    static {
        FILE = new LazyTokenStructureMatch("FILE");
        ENTRY = new LazyTokenStructureMatch("ENTRY");
        COMMAND = new LazyTokenStructureMatch("COMMAND");
        INSTRUCTION = new LazyTokenStructureMatch("INSTRUCTION");
        MODIFIER = new LazyTokenStructureMatch("MODIFIER");
        TEXT_COMPONENT = new LazyTokenStructureMatch("TEXT_COMPONENT");
        SELECTOR = new LazyTokenStructureMatch("SELECTOR");
        SELECTOR_ARGUMENT = new LazyTokenStructureMatch("SELECTOR_ARGUMENT");
        PLAYER_NAME = choice(identifierB()).setName("PLAYER_NAME");

        COMMENT_S = new LazyTokenItemMatch(COMMENT).setName("COMMENT");
        VERBATIM_COMMAND_S = new LazyTokenItemMatch(VERBATIM_COMMAND).setName("VERBATIM_COMMAND");

        {
            var directiveBody = new LazyTokenStructureMatch("DIRECTIVE_BODY");

            DIRECTIVE = group(ofType(DIRECTIVE_HEADER), directiveBody).setName("DIRECTIVE");

            directiveBody.add(group(literal("on"), ofType(DIRECTIVE_ON_KEYWORD)).setName("ON_DIRECTIVE"));
            directiveBody.add(group(literal("tag"), ofType(RESOURCE_LOCATION)).setName("TAG_DIRECTIVE"));
            directiveBody.add(group(literal("require"), ofType(RESOURCE_LOCATION)).setName("REQUIRE_DIRECTIVE"));
        }

        RESOURCE_LOCATION_TAGGED = group(optional(hash(), ofType(GLUE)), ofType(RESOURCE_LOCATION).setName("RESOURCE_LOCATION")).setName("RESOURCE_LOCATION_TAGGED");

        ENTRY.add(COMMENT_S);
        ENTRY.add(group(list(MODIFIER).setOptional().setName("MODIFIERS"), literal("run").setOptional(), COMMAND).setName("COMMAND_WRAPPER"));
        ENTRY.add(INSTRUCTION);
        ENTRY.add(VERBATIM_COMMAND_S);

        STRING_LITERAL_OR_IDENTIFIER_A.add(string());
        STRING_LITERAL_OR_IDENTIFIER_A.add(identifierA());

        {
            var separator = new LazyTokenGroupMatch(true).setName("LINE_PADDING");
            separator.append(new LazyTokenListMatch(TokenType.NEWLINE, true));
            var l = new LazyTokenListMatch(new LazyTokenGroupMatch(true).append(ENTRY), separator, true).setName("ENTRIES");
            FILE.add(group(optional(list(DIRECTIVE).setOptional(true).setName("DIRECTIVES")),l,ofType(TokenType.END_OF_FILE)));
        }

        TEXT_COLOR = choice("black", "dark_blue", "dark_aqua", "dark_green", "dark_red", "dark_purple", "gold", "light_gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white").setName("TEXT_COLOR");


        ENTITY.add(PLAYER_NAME);
        ENTITY.add(SELECTOR);
        ENTITY.add(VARIABLE_MARKER);

        //region Commands
        //region say
        {
            var g = new LazyTokenGroupMatch();
            g.append(matchItem(COMMAND_HEADER, "say"));
            g.append(ofType(TRAILING_STRING));
            COMMAND.add(g);
        }
        //endregion
        //region tellraw
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "tellraw"),
                    ENTITY,
                    TEXT_COMPONENT
            ));
        }
        //endregion
        //region defaultgamemode
        {
            var g = new LazyTokenGroupMatch();
            g.append(matchItem(COMMAND_HEADER, "defaultgamemode"));
            g.append(GAMEMODE);
            COMMAND.add(g);
        }
        //endregion
        //region gamemode
        {
            var g = new LazyTokenGroupMatch();
            g.append(matchItem(COMMAND_HEADER, "gamemode"));
            g.append(GAMEMODE);
            g.append(new LazyTokenGroupMatch(true).append(sameLine()).append(ENTITY).setName("PLAYER"));
            COMMAND.add(g);
        }
        //endregion
        //region tag
        {
            var g = new LazyTokenGroupMatch();
            g.append(matchItem(COMMAND_HEADER, "tag"));
            g.append(ENTITY);
            g.append(choice(
                    literal("list"),
                    group(literal("add"), ofType(IDENTIFIER_TYPE_A)),
                    group(literal("remove"), ofType(IDENTIFIER_TYPE_A))
            ));
            COMMAND.add(g);
        }
        //endregion
        //region experience
        {
            var unit = choice("points", "levels").setName("UNIT").setOptional();

            var g = new LazyTokenGroupMatch();
            g.append(choice(matchItem(COMMAND_HEADER, "experience"), matchItem(COMMAND_HEADER, "xp")));
            g.append(choice(
                    group(literal("add"), ENTITY, integer(), unit).setName("ADD"),
                    group(literal("set"), ENTITY, integer(), unit).setName("SET"),
                    group(literal("query"), ENTITY, unit).setName("QUERY")
            ).setName("SUBCOMMAND"));
            COMMAND.add(g);
        }
        //endregion
        //region seed
        {
            COMMAND.add(matchItem(COMMAND_HEADER, "seed"));
        }
        //endregion
        //region give
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "give"),
                    ENTITY,
                    ITEM,
                    integer().setOptional().setName("AMOUNT")
            ));
        }
        //endregion
        //region clear
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "clear"),
                    group(
                            ENTITY,
                            group(
                                    ofType(LINE_GLUE),
                                    ITEM_TAGGED,
                                    integer().setOptional().setName("AMOUNT")
                            ).setOptional()
                    ).setOptional()
            ));
        }
        //endregion
        //region difficulty
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "difficulty"),
                    group(DIFFICULTY).setOptional()
            ));
        }
        //endregion
        //region effect
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "effect"),
                    choice(
                            group(literal("clear"), ENTITY, optional(EFFECT_ID)).setName("CLEAR"),
                            group(literal("give"), ENTITY, EFFECT_ID, optional(integer().setName("DURATION"), optional(integer().setName("AMPLIFIER"), ofType(TridentTokens.BOOLEAN).setName("HIDE_PARTICLES")))).setName("GIVE")
                    )
            ));
        }
        //endregion
        //region enchant
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "enchant"),
                    ENTITY,
                    ENCHANTMENT_ID,
                    integer().setOptional().setName("LEVEL")
            ));
        }
        //endregion
        //region function
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "function"),
                    sameLine(),
                    RESOURCE_LOCATION_TAGGED
            ));
        }
        //endregion
        //region gamerule
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "gamerule"),
                    choice(
                            GAMERULE,
                            GAMERULE_SETTER
                    )
            ));
        }
        //endregion
        //region help
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "help"),
                    ofType(TRAILING_STRING).setOptional()
            ));
        }
        //endregion
        //region kill
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "kill"),
                    optional(ENTITY)
            ));
        }
        //endregion
        //region list
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "list"),
                    literal("uuids").setOptional()
            ));
        }
        //endregion
        //region locate
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "locate"),
                    STRUCTURE
            ));
        }
        //endregion
        //region me
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "me"),
                    ofType(TRAILING_STRING)
            ));
        }
        //endregion
        //region msg
        {
            COMMAND.add(group(
                    choice(matchItem(COMMAND_HEADER, "msg"), matchItem(COMMAND_HEADER, "w")),
                    ENTITY,
                    ofType(TRAILING_STRING)
            ));
        }
        //endregion
        //region gamerule
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "gamerule"),
                    choice(
                            GAMERULE,
                            GAMERULE_SETTER
                    )
            ));
        }
        //endregion
        //region playsound
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "playsound"),
                    ofType(RESOURCE_LOCATION).setName("RESOURCE_LOCATION"),
                    ofType(SOUND_CHANNEL).setName("CHANNEL"),
                    ENTITY,
                    optional(
                            COORDINATE_SET,
                            real().setOptional(),
                            real().setOptional(),
                            real().setOptional()
                    )
            ));
        }
        //endregion
        //region clone
        {
            var mode = choice("force", "move", "normal").setOptional().setName("CLONE_MODE");

            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "clone"),
                    group(COORDINATE_SET).setName("FROM"),
                    group(COORDINATE_SET).setName("TO"),
                    group(COORDINATE_SET).setName("DESTINATION"),
                    choice(
                            group(literal("filtered"), ofType(LINE_GLUE), BLOCK_TAGGED, mode).setName("FILTERED"),
                            group(literal("masked"), mode).setName("MASKED"),
                            group(literal("replace"), mode).setName("REPLACE")
                    ).setOptional()
            ));
        }
        //endregion
        //region fill
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "fill"),
                    group(COORDINATE_SET).setName("FROM"),
                    group(COORDINATE_SET).setName("TO"),
                    BLOCK,
                    choice(
                            literal("destroy"),
                            literal("hollow"),
                            literal("keep"),
                            literal("outline"),
                            group(literal("replace"), optional(BLOCK_TAGGED)).setName("REPLACE")
                    ).setOptional()
            ));
        }
        //endregion
        //region particle
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "particle"),
                    PARTICLE,
                    optional(
                            COORDINATE_SET,
                            optional(
                                    group(
                                            real().setName("DX"),
                                            real().setName("DY"),
                                            real().setName("DZ")
                                    ).setName("DELTA"),
                                    real().setName("SPEED"),
                                    integer().setName("COUNT"),
                                    optional(
                                            choice("force", "normal"),
                                            optional(sameLine(), ENTITY)
                                    )
                            )
                    )
            ));
        }
        //endregion
        //region recipe
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "recipe"),
                    choice("give", "take").setName("ACTION"),
                    ENTITY,
                    choice(
                            matchItem(SYMBOL, "*"),
                            ofType(RESOURCE_LOCATION)
                    )
            ));
        }
        //endregion
        //region replaceitem
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "replaceitem"),
                    choice(
                            group(literal("block"), COORDINATE_SET),
                            group(literal("entity"), ENTITY)
                    ).setName("TARGET"),
                    SLOT_ID,
                    ITEM
            ));
        }
        //endregion
        //region schedule
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "schedule"),
                    literal("function"),
                    ofType(LINE_GLUE),
                    RESOURCE_LOCATION_TAGGED,
                    ofType(TIME).setName("TIME")
            ));
        }
        //endregion
        //region setblock
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "setblock"),
                    COORDINATE_SET,
                    BLOCK,
                    choice("destroy", "keep", "replace").setOptional().setName("OLD_BLOCK_HANDLING")
            ));
        }
        //endregion
        //region setworldspawn
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "setworldspawn"),
                    optional(COORDINATE_SET)
            ));
        }
        //endregion
        //region spawnpoint
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "spawnpoint"),
                    optional(
                            sameLine(),
                            ENTITY,
                            optional(COORDINATE_SET)
                    )
            ));
        }
        //endregion
        //region spreadplayers
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "spreadplayers"),
                    TWO_COORDINATE_SET,
                    real().setName("SPREAD_DISTANCE"),
                    real().setName("MAX_RANGE"),
                    ofType(BOOLEAN).setName("RESPECT_TEAMS"),
                    ENTITY
            ));
        }
        //endregion
        //region stopsound
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "stopsound"),
                    ENTITY,
                    choice(
                            group(ofType(SOUND_CHANNEL).setName("CHANNEL"), optional(sameLine(), ofType(RESOURCE_LOCATION)).setName("RESOURCE_LOCATION")).setName("STOP_BY_CHANNEL"),
                            group(matchItem(SYMBOL, "*"), sameLine(), ofType(RESOURCE_LOCATION).setName("RESOURCE_LOCATION")).setName("STOP_BY_EVENT")
                    ).setOptional()
            ));
        }
        //endregion
        //region summon
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "summon"),
                    ENTITY_ID,
                    optional(
                            COORDINATE_SET,
                            optional(NBT_COMPOUND)
                    )
            ));
        }
        //endregion
        //region teleport
        {
            COMMAND.add(group(
                    choice(matchItem(COMMAND_HEADER, "teleport"), matchItem(COMMAND_HEADER, "tp")),
                    choice(
                            group(
                                    ENTITY,
                                    choice(
                                            ENTITY,
                                            group(
                                                    COORDINATE_SET,
                                                    choice(
                                                            group(
                                                                    literal("facing"),
                                                                    choice(
                                                                            COORDINATE_SET,
                                                                            group(literal("entity"), ENTITY, ofType(ANCHOR).setOptional().setName("ANCHOR"))
                                                                    )
                                                            ).setName("FACING_CLAUSE"),
                                                            TWO_COORDINATE_SET
                                                    ).setOptional().setName("ROTATION_OPTION")
                                            )
                                    ).setOptional()
                            ),
                            COORDINATE_SET
                    ).setName("SUBCOMMAND")
            ));
        }
        //endregion
        //region time
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "time"),
                    choice(
                            group(literal("query"), choice("day", "daytime", "gametime")).setName("QUERY"),
                            group(literal("set"), choice(ofType(TIME).setName("TIME"), choice("day", "midnight", "night", "noon"))).setName("SET"),
                            group(literal("add"), ofType(TIME).setName("TIME")).setName("ADD")
                    )
            ));
        }
        //endregion
        //region title
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "title"),
                    ENTITY,
                    choice(
                            group(choice("title", "subtitle", "actionbar").setName("DISPLAY"), TEXT_COMPONENT).setName("SHOW"),
                            choice("clear", "reset").setName("CLEAR_RESET"),
                            group(literal("times"), integer().setName("FADEIN"), integer().setName("STAY"), integer().setName("FADEOUT")).setName("TIMES")
                    )
            ));
        }
        //endregion
        //region trigger
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "trigger"),
                    ofType(IDENTIFIER_TYPE_A).setName("OBJECTIVE_NAME"),
                    optional(choice("add", "set"), integer()).setName("INNER")
            ));
        }
        //endregion
        //region weather
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "weather"),
                    choice("clear", "rain", "thunder"),
                    integer().setOptional()
            ));
        }
        //endregion
        //region worldborder
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "worldborder"),
                    choice(
                            literal("get").setName("GET"),
                            group(choice("add", "set"), real().setName("DISTANCE"), integer().setOptional().setName("TIME")).setName("CHANGE"),
                            group(literal("center"), TWO_COORDINATE_SET).setName("CENTER"),
                            group(literal("damage"), choice("amount", "buffer"), real().setName("DAMAGE_OR_DISTANCE")).setName("DAMAGE"),
                            group(literal("warning"), choice("distance", "time"), integer().setName("DISTANCE_OR_TIME")).setName("WARNING")
                    )
            ));
        }
        //endregion
        //region team
        {
            var teamOptions = choice(
                    group(literal("collisionRule"), choice("always", "never", "pushOtherTeams", "pushOwnTeam")).setName("TEAM_COMPARISON_ARG"),
                    group(literal("color"), TEXT_COLOR).setName("COLOR_ARG"),
                    group(literal("deathMessageVisibility"), choice("always", "hideForOtherTeams", "hideForOwnTeam", "never")).setName("TEAM_COMPARISON_ARG"),
                    group(literal("displayName"), TEXT_COMPONENT).setName("TEXT_COMPONENT_ARG"),
                    group(literal("friendlyFire"), ofType(BOOLEAN).setName("BOOLEAN")).setName("BOOLEAN_ARG"),
                    group(literal("nametagVisibility"), choice("always", "hideForOtherTeams", "hideForOwnTeam", "never")).setName("TEAM_COMPARISON_ARG"),
                    group(literal("prefix"), TEXT_COMPONENT).setName("TEXT_COMPONENT_ARG"),
                    group(literal("suffix"), TEXT_COMPONENT).setName("TEXT_COMPONENT_ARG"),
                    group(literal("seeFriendlyInvisibles"), ofType(BOOLEAN).setName("BOOLEAN")).setName("BOOLEAN_ARG")
            ).setName("TEAM_OPTIONS");

            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "team"),
                    choice(
                            group(literal("add"), identifierA().setName("TEAM"), optional(TEXT_COMPONENT).setName("DISPLAY_NAME")).setName("ADD"),
                            group(literal("empty"), identifierA().setName("TEAM")).setName("EMPTY"),
                            group(literal("join"), identifierA().setName("TEAM"), optional(sameLine(), ENTITY).setName("SUBJECT")).setName("JOIN"),
                            group(literal("leave"), ENTITY).setName("LEAVE"),
                            group(literal("list"), optional(sameLine(), identifierA().setName("TEAM"))).setName("LIST"),
                            group(literal("modify"), identifierA().setName("TEAM"), teamOptions).setName("MODIFY"),
                            group(literal("remove"), identifierA().setName("TEAM")).setName("REMOVE")
                    )
            ));
        }
        //endregion
        //region scoreboard
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "scoreboard"),
                    choice(
                            group(literal("objectives"), choice(
                                    group(literal("add"), identifierA().setName("OBJECTIVE_NAME"), identifierB().setName("CRITERIA"), optional(TEXT_COMPONENT)).setName("ADD"),
                                    literal("list").setName("LIST"),
                                    group(literal("modify"), identifierA().setName("OBJECTIVE"), choice(
                                            group(literal("displayname"), TEXT_COMPONENT).setName("DISPLAYNAME"),
                                            group(literal("rendertype"), choice("integer", "hearts")).setName("RENDERTYPE")
                                    )).setName("MODIFY"),
                                    group(literal("remove"), identifierA().setName("OBJECTIVE")).setName("REMOVE"),
                                    group(literal("setdisplay"), identifierA().setName("DISPLAY_SLOT"), optional(sameLine(), identifierA().setName("OBJECTIVE")).setName("OBJECTIVE_CLAUSE")).setName("SETDISPLAY")
                            )).setName("OBJECTIVES"),
                            group(literal("players"), choice(
                                    group(choice("add", "remove", "set"), ENTITY, identifierA().setName("OBJECTIVE"), integer()).setName("CHANGE"),
                                    group(literal("enable"), ENTITY, identifierA()).setName("ENABLE"),
                                    group(literal("get"), ENTITY, identifierA()).setName("GET"),
                                    group(literal("list"), optional(sameLine(), ENTITY)).setName("LIST"),
                                    group(literal("operation"), group(ENTITY).setName("TARGET"), identifierA().setName("TARGET_OBJECTIVE"), ofType(SCOREBOARD_OPERATOR).setName("OPERATOR"), group(ENTITY).setName("SOURCE"), identifierA().setName("SOURCE_OBJECTIVE")).setName("OPERATION"),
                                    group(literal("reset"), choice(ENTITY, symbol("*")).setName("TARGET"), optional(sameLine(), identifierA().setName("OBJECTIVE")).setName("OBJECTIVE_CLAUSE")).setName("RESET")
                            )).setName("PLAYERS")
                    )
            ));
        }
        //endregion
        //region advancement
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "advancement"),
                    choice("grant", "revoke").setName("ACTION"),
                    ENTITY,
                    choice(
                            literal("everything").setName("EVERYTHING"),
                            group(choice("from", "through", "until").setName("LIMIT"), ofType(RESOURCE_LOCATION)).setName("FROM_THROUGH_UNTIL"),
                            group(literal("only"), ofType(RESOURCE_LOCATION), group(sameLine(), list(identifierC(), sameLine()).setName("CRITERIA_LIST")).setOptional().setName("CRITERIA")).setName("ONLY")
                    ).setName("INNER")
            ));
        }
        //endregion
        //region bossbar
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "bossbar"),
                    choice(
                            literal("list").setName("LIST"),
                            group(literal("add"), ofType(RESOURCE_LOCATION), TEXT_COMPONENT).setName("ADD"),
                            group(literal("get"), ofType(RESOURCE_LOCATION), choice("max", "players", "value", "visible")).setName("GET"),
                            group(literal("remove"), ofType(RESOURCE_LOCATION)).setName("REMOVE"),
                            group(literal("set"), ofType(RESOURCE_LOCATION), choice(
                                    group(literal("color"), choice("blue", "green", "pink", "purple", "red", "white", "yellow")).setName("SET_COLOR"),
                                    group(literal("max"), integer()).setName("SET_MAX"),
                                    group(literal("name"), TEXT_COMPONENT).setName("SET_NAME"),
                                    group(literal("players"), ENTITY).setName("SET_PLAYERS"),
                                    group(literal("style"), choice("progress", "notched_6", "notched_10", "notched_12", "notched_20")).setName("SET_STYLE"),
                                    group(literal("value"), integer()).setName("SET_VALUE"),
                                    group(literal("visible"), ofType(BOOLEAN)).setName("SET_VISIBLE")
                            )).setName("SET")
                    )
            ));
        }
        //endregion
        //region data
        {

            var target = choice(
                    group(literal("block"), COORDINATE_SET).setName("BLOCK_TARGET"),
                    group(literal("entity"), ENTITY).setName("ENTITY_TARGET")
            ).setName("DATA_TARGET");

            var source = choice(
                    group(literal("from"), target, optional(sameLine(), NBT_PATH).setName("PATH_CLAUSE")).setName("TARGET_SOURCE"),
                    group(literal("value"), NBT_VALUE).setName("LITERAL_SOURCE")
            ).setName("DATA_SOURCE");

            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "data"),
                    choice(
                            group(literal("get"), target, optional(NBT_PATH, real().setOptional().setName("SCALE")).setName("PATH_CLAUSE")).setName("GET"),
                            group(literal("merge"), target, NBT_COMPOUND).setName("MERGE"),
                            group(literal("modify"), target, NBT_PATH, choice(
                                    group(literal("append"), source).setName("MODIFY_APPEND"),
                                    group(literal("insert"), choice("before", "after"), integer(), source).setName("MODIFY_INSERT"),
                                    group(literal("merge"), source).setName("MODIFY_MERGE"),
                                    group(literal("prepend"), source).setName("MODIFY_PREPEND"),
                                    group(literal("set"), source).setName("MODIFY_SET")
                            )).setName("MODIFY"),
                            group(literal("remove"), target, NBT_PATH).setName("REMOVE")
                    )
            ));
        }
        //endregion
        //region loot
        {

            var tool = choice(literal("mainhand"), literal("offhand"), ITEM).setOptional().setName("TOOL");

            var destination = choice(
                    group(literal("give"), ENTITY).setName("GIVE"),
                    group(literal("insert"), COORDINATE_SET).setName("INSERT"),
                    group(literal("replace"),
                            choice(
                                    group(literal("block"), COORDINATE_SET),
                                    group(literal("entity"), ENTITY)
                            ),
                            SLOT_ID
                    ).setName("REPLACE"),
                    group(literal("spawn"), COORDINATE_SET).setName("SPAWN")
            ).setName("LOOT_DESTINATION");

            var source = choice(
                    group(literal("fish"), ofType(RESOURCE_LOCATION).setName("RESOURCE_LOCATION"), COORDINATE_SET, tool).setName("FISH"),
                    group(literal("kill"), ENTITY).setName("KILL"),
                    group(literal("loot"), ofType(RESOURCE_LOCATION).setName("RESOURCE_LOCATION")).setName("LOOT"),
                    group(literal("mine"), COORDINATE_SET, tool).setName("MINE")
            ).setName("LOOT_SOURCE");

            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "loot"),
                    destination, source
            ));
        }
        //endregion
        //region execute
        {
            COMMAND.add(group(
                    matchItem(COMMAND_HEADER, "execute"),
                    list(MODIFIER).setOptional(true).setName("MODIFIER_LIST"),
                    optional(
                            literal("run"),
                            COMMAND
                    ).setName("CHAINED_COMMAND")
            ));
        }
        //endregion
        //endregion

        //region Execute Modifiers
        //region align
        {
            MODIFIER.add(group(
                    matchItem(MODIFIER_HEADER, "align"),
                    ofType(SWIZZLE)
            ));
        }
        //endregion
        //region anchored
        {
            MODIFIER.add(group(
                    matchItem(MODIFIER_HEADER, "anchored"),
                    ofType(ANCHOR)
            ));
        }
        //endregion
        //region as
        {
            MODIFIER.add(group(
                    matchItem(MODIFIER_HEADER, "as"),
                    ENTITY
            ));
        }
        //endregion
        //region at
        {
            MODIFIER.add(group(
                    matchItem(MODIFIER_HEADER, "at"),
                    ENTITY
            ));
        }
        //endregion
        //region facing
        {
            MODIFIER.add(group(
                    matchItem(MODIFIER_HEADER, "facing"),
                    choice(
                            group(literal("entity").setOptional(), ENTITY, ofType(ANCHOR).setOptional()).setName("ENTITY_BRANCH"),
                            group(COORDINATE_SET).setName("BLOCK_BRANCH")
                    )
            ));
        }
        //endregion
        //region in
        {
            MODIFIER.add(group(
                    matchItem(MODIFIER_HEADER, "in"),
                    DIMENSION_ID
            ));
        }
        //endregion
        //region if/unless
        {
            MODIFIER.add(group(
                    choice(matchItem(MODIFIER_HEADER, "if"), matchItem(MODIFIER_HEADER, "unless")).setName("HEADER"),
                    choice(
                            group(literal("entity"), ENTITY).setName("ENTITY_CONDITION"),
                            group(literal("block"), COORDINATE_SET, BLOCK_TAGGED).setName("BLOCK_CONDITION"),
                            group(literal("score"), ENTITY, identifierA().setName("OBJECTIVE"), choice(
                                    matchItem(TridentTokens.SYNTACTIC_SUGAR, "isset").setName("ISSET"),
                                    group(choice(symbol("<"), symbol("<="), symbol("="), symbol(">="), symbol(">")).setName("OPERATOR"), ENTITY, identifierA().setName("OBJECTIVE")).setName("COMPARISON"),
                                    group(literal("matches"), INTEGER_NUMBER_RANGE).setName("MATCHES"))
                            ).setName("SCORE_CONDITION"),
                            group(literal("blocks"),
                                    group(COORDINATE_SET).setName("FROM"),
                                    group(COORDINATE_SET).setName("TO"),
                                    group(COORDINATE_SET).setName("TEMPLATE"),
                                    choice("all", "masked").setName("AIR_POLICY")
                            ).setName("REGION_CONDITION"),
                            group(literal("data"),
                                    choice(
                                            group(literal("block"), COORDINATE_SET).setName("BLOCK_SUBJECT"),
                                            group(literal("entity"), ENTITY).setName("ENTITY_SUBJECT")
                                    ),
                                    NBT_PATH
                            ).setName("DATA_CONDITION")
                    ).setName("SUBJECT")
            ));
        }
        //endregion
        //region positioned
        {
            MODIFIER.add(group(
                    matchItem(MODIFIER_HEADER, "positioned"),
                    choice(
                            group(literal("as").setOptional(), ENTITY).setName("ENTITY_BRANCH"),
                            group(COORDINATE_SET).setName("BLOCK_BRANCH")
                    )
            ));
        }
        //endregion
        //region rotated
        {
            MODIFIER.add(group(
                    matchItem(MODIFIER_HEADER, "rotated"),
                    choice(
                            group(literal("as").setOptional(), ENTITY).setName("ENTITY_BRANCH"),
                            TWO_COORDINATE_SET
                    )
            ));
        }
        //endregion
        //region store
        {
            MODIFIER.add(group(
                    matchItem(MODIFIER_HEADER, "store"),
                    choice("result", "success").setName("STORE_VALUE"),
                    choice(
                            group(literal("block"), COORDINATE_SET, NBT_PATH, ofType(NUMERIC_DATA_TYPE).setName("NUMERIC_TYPE"), real().setName("SCALE")).setName("STORE_BLOCK"),
                            group(literal("bossbar"), ofType(RESOURCE_LOCATION).setName("RESOURCE_LOCATION"), choice("max", "value").setName("BOSSBAR_VARIABLE")).setName("STORE_BOSSBAR"),
                            group(literal("entity"), ENTITY, NBT_PATH, ofType(NUMERIC_DATA_TYPE).setName("NUMERIC_TYPE"), real().setName("SCALE")).setName("STORE_ENTITY"),
                            group(literal("score"), ENTITY, identifierA().setName("OBJECTIVE")).setName("STORE_SCORE")
                    )
            ));
        }
        //endregion
        //endregion

        //region Constructs
        //region Blockstate
        {
            var g = new LazyTokenGroupMatch();
            g.append(brace("["));
            {
                var g2 = new LazyTokenGroupMatch().setName("BLOCKSTATE_PROPERTY");
                g2.append(ofType(IDENTIFIER_TYPE_A).setName("BLOCKSTATE_PROPERTY_KEY"));
                g2.append(equals());
                {
                    var s = new LazyTokenStructureMatch("BLOCKSTATE_PROPERTY_VALUE");
                    s.add(real());
                    s.add(ofType(BOOLEAN));
                    s.add(ofType(IDENTIFIER_TYPE_A));
                    g2.append(s);
                }
                g.append(new LazyTokenListMatch(g2, comma(), true).setName("BLOCKSTATE_LIST"));
            }
            g.append(brace("]"));

            BLOCKSTATE.add(g);
        }
        //endregion
        //region Block
        {
            var g = new LazyTokenGroupMatch().setName("CONCRETE_RESOURCE");
            g.append(new LazyTokenGroupMatch().append(BLOCK_ID).setName("RESOURCE_NAME"));
            g.append(new LazyTokenGroupMatch(true).append(ofType(GLUE)).append(BLOCKSTATE).setName("BLOCKSTATE_CLAUSE"));
            g.append(new LazyTokenGroupMatch(true).append(ofType(GLUE)).append(NBT_COMPOUND).setName("NBT_CLAUSE"));
            BLOCK.add(g);
            BLOCK_TAGGED.add(BLOCK);
        }

        {
            var g = new LazyTokenGroupMatch().setName("ABSTRACT_RESOURCE");
            g.append(new LazyTokenGroupMatch().append(hash().setName("TAG_HEADER")).append(ofType(GLUE)).append(ofType(RESOURCE_LOCATION).setName("RESOURCE_LOCATION")).setName("RESOURCE_NAME"));
            g.append(new LazyTokenGroupMatch(true).append(ofType(GLUE)).append(BLOCKSTATE));
            g.append(new LazyTokenGroupMatch(true).append(ofType(GLUE)).append(NBT_COMPOUND));
            BLOCK_TAGGED.add(g);
        }
        //endregion
        //region Item
        {
            var g = new LazyTokenGroupMatch().setName("CONCRETE_RESOURCE");
            g.append(new LazyTokenGroupMatch().append(ITEM_ID).setName("RESOURCE_NAME"));
            g.append(new LazyTokenGroupMatch(true).append(ofType(GLUE)).append(NBT_COMPOUND));
            ITEM.add(g);
            ITEM_TAGGED.add(ITEM);
        }

        {
            var g = new LazyTokenGroupMatch().setName("ABSTRACT_RESOURCE");
            g.append(new LazyTokenGroupMatch().append(hash().setName("TAG_HEADER")).append(ofType(GLUE)).append(ofType(RESOURCE_LOCATION).setName("RESOURCE_LOCATION")).setName("RESOURCE_NAME"));
            g.append(new LazyTokenGroupMatch(true).append(ofType(GLUE)).append(NBT_COMPOUND));
            ITEM_TAGGED.add(g);
        }
        //endregion

        //region Text Components
        {
            var JSON_ELEMENT = new LazyTokenStructureMatch("JSON_ELEMENT");

            {
                var g = new LazyTokenGroupMatch().setName("JSON_OBJECT");
                g.append(brace("{"));
                {
                    var g2 = new LazyTokenGroupMatch();
                    g2.append(string().setName("JSON_OBJECT_KEY"));
                    g2.append(colon());
                    g2.append(JSON_ELEMENT);
                    g.append(new LazyTokenListMatch(g2, comma(), true).setName("JSON_OBJECT_ENTRIES"));
                }
                g.append(brace("}"));
                JSON_ELEMENT.add(g);
            }
            {
                var g = new LazyTokenGroupMatch().setName("JSON_ARRAY");
                g.append(brace("["));
                g.append(new LazyTokenListMatch(JSON_ELEMENT, comma(), true).setName("JSON_ARRAY_ENTRIES"));
                g.append(brace("]"));
                JSON_ELEMENT.add(g);
            }
            JSON_ELEMENT.add(string().setName("STRING_LITERAL"));
            JSON_ELEMENT.add(real().setName("NUMBER"));
            JSON_ELEMENT.add(ofType(BOOLEAN).setName("BOOLEAN"));

            TEXT_COMPONENT.add(JSON_ELEMENT);
        }
        //endregion
        //region NBT
        {
            {
                var g = new LazyTokenGroupMatch();
                g.append(brace("{"));
                {
                    var g2 = new LazyTokenGroupMatch();
                    g2.append(new LazyTokenGroupMatch().append(STRING_LITERAL_OR_IDENTIFIER_A).setName("NBT_KEY"));
                    g2.append(colon());
                    g2.append(NBT_VALUE);
                    g.append(new LazyTokenListMatch(g2, comma(), true).setName("NBT_COMPOUND_ENTRIES"));
                }
                g.append(brace("}"));
                NBT_COMPOUND.add(g);
                NBT_VALUE.add(NBT_COMPOUND);
            }
            {
                var g = new LazyTokenGroupMatch();
                g.append(brace("["));
                g.append(optional().append(new LazyTokenListMatch(NBT_VALUE, comma(), true).setName("NBT_LIST_ENTRIES")));
                g.append(brace("]"));
                NBT_LIST.add(g);
                NBT_VALUE.add(NBT_LIST);
            }
            NBT_VALUE.add(string());
            NBT_VALUE.add(ofType(IDENTIFIER_TYPE_A).setName("RAW_STRING"));
            NBT_VALUE.add(ofType(TYPED_NUMBER).setName("NBT_NUMBER"));
            NBT_VALUE.add(ofType(BOOLEAN).setName("BOOLEAN"));
        }

        {
            var NBT_PATH_NODE = new LazyTokenStructureMatch("NBT_PATH_NODE");

            NBT_PATH_NODE.add(
                    group(
                            dot().setName("NBT_PATH_SEPARATOR"),
                            glue(),
                            group(STRING_LITERAL_OR_IDENTIFIER_A).setName("NBT_PATH_KEY_LABEL")
                    ).setName("NBT_PATH_KEY"));

            NBT_PATH_NODE.add(group(brace("["), integer(), brace("]")).setName("NBT_PATH_INDEX"));

            NBT_PATH_NODE.add(group(brace("["), NBT_COMPOUND, brace("]")).setName("NBT_PATH_LIST_MATCH"));

            NBT_PATH_NODE.add(group(NBT_COMPOUND).setName("NBT_PATH_COMPOUND_MATCH"));

            NBT_PATH.add(group(choice(group(group(STRING_LITERAL_OR_IDENTIFIER_A).setName("NBT_PATH_KEY_LABEL")).setName("NBT_PATH_KEY")).setName("NBT_PATH_NODE"),
                    list(NBT_PATH_NODE, glue()).setOptional().setName("OTHER_NODES")));
        }
        //endregion
        //region Selector
        {
            SELECTOR.add(
                    group(
                            ofType(SELECTOR_HEADER).setName("SELECTOR_HEADER"),
                            optional(
                                    glue(),
                                    brace("["),
                                    list(SELECTOR_ARGUMENT, comma()).setOptional().setName("SELECTOR_ARGUMENT_LIST"),
                                    brace("]")
                            )
                    )
            );
        }
        //endregion
        //region Number Ranges
        {
            INTEGER_NUMBER_RANGE.add(integer().setName("EXACT"));
            {
                var g = new LazyTokenGroupMatch();
                g.append(integer().setName("MIN"));
                g.append(glue());
                g.append(dot());
                g.append(glue());
                g.append(dot());
                g.append(optional(glue(), integer().setName("MAX")));
                INTEGER_NUMBER_RANGE.add(g);
            }
            {
                var g = new LazyTokenGroupMatch();
                g.append(dot());
                g.append(glue());
                g.append(dot());
                g.append(glue());
                g.append(integer().setName("MAX"));
                INTEGER_NUMBER_RANGE.add(g);
            }

            REAL_NUMBER_RANGE.add(real().setName("EXACT"));
            {
                var g = new LazyTokenGroupMatch();
                g.append(real().setName("MIN"));
                g.append(glue());
                g.append(dot());
                g.append(glue());
                g.append(dot());
                g.append(optional(glue(), real().setName("MAX")));
                REAL_NUMBER_RANGE.add(g);
            }
            {
                var g = new LazyTokenGroupMatch();
                g.append(dot());
                g.append(glue());
                g.append(dot());
                g.append(glue());
                g.append(real().setName("MAX"));
                REAL_NUMBER_RANGE.add(g);
            }
        }
        //endregion
        //region Selector Arguments
        {
            //Integer Range Arguments
            SELECTOR_ARGUMENT.add(group(
                    choice("level").setName("SELECTOR_ARGUMENT_KEY"),
                    equals(),
                    choice(INTEGER_NUMBER_RANGE).setName("SELECTOR_ARGUMENT_VALUE")
            ));
        }

        {
            //Real Number Range Arguments
            SELECTOR_ARGUMENT.add(group(
                    choice("distance", "x_rotation", "y_rotation").setName("SELECTOR_ARGUMENT_KEY"),
                    equals(),
                    choice(REAL_NUMBER_RANGE).setName("SELECTOR_ARGUMENT_VALUE")
            ));
        }

        {
            //Integer Number Arguments
            SELECTOR_ARGUMENT.add(group(
                    choice("limit").setName("SELECTOR_ARGUMENT_KEY"),
                    equals(),
                    choice(integer()).setName("SELECTOR_ARGUMENT_VALUE")
            ));
        }

        {
            //Real Number Arguments
            SELECTOR_ARGUMENT.add(group(
                    choice("x", "y", "z", "dx", "dy", "dz").setName("SELECTOR_ARGUMENT_KEY"),
                    equals(),
                    choice(real()).setName("SELECTOR_ARGUMENT_VALUE")
            ));
        }

        {
            //Identifier Arguments
            SELECTOR_ARGUMENT.add(group(
                    choice("tag", "team").setName("SELECTOR_ARGUMENT_KEY"),
                    equals(),
                    choice(
                            group(not().setOptional(), identifierA().setOptional())
                    ).setName("SELECTOR_ARGUMENT_VALUE")
            ));
        }

        {
            //String Arguments
            SELECTOR_ARGUMENT.add(group(
                    choice("name").setName("SELECTOR_ARGUMENT_KEY"),
                    equals(),
                    choice(
                            group(not().setOptional(), group(STRING_LITERAL_OR_IDENTIFIER_A).setOptional())
                    ).setName("SELECTOR_ARGUMENT_VALUE")
            ));
        }

        {
            //Gamemode argument
            SELECTOR_ARGUMENT.add(group(
                    choice("gamemode").setName("SELECTOR_ARGUMENT_KEY"),
                    equals(),
                    choice(group(not().setOptional(), GAMEMODE)).setName("SELECTOR_ARGUMENT_VALUE")
            ));
        }

        {
            //Type argument
            SELECTOR_ARGUMENT.add(group(
                    choice("type").setName("SELECTOR_ARGUMENT_KEY"),
                    equals(),
                    choice(group(not().setOptional(), ENTITY_ID)).setName("SELECTOR_ARGUMENT_VALUE")
            ));
        }

        {
            //Sort argument
            SELECTOR_ARGUMENT.add(group(
                    choice("sort").setName("SELECTOR_ARGUMENT_KEY"),
                    equals(),
                    choice(ofType(SORTING)).setName("SELECTOR_ARGUMENT_VALUE")
            ));
        }

        {
            //Advancements argument

            LazyTokenPatternMatch advancementArgumentBlock = group(
                    brace("{"),
                    list(group(
                            ofType(RESOURCE_LOCATION).setName("ADVANCEMENT_ENTRY_KEY"),
                            equals(),
                            choice(
                                    ofType(BOOLEAN).setName("BOOLEAN"),
                                    group(
                                            brace("{"),
                                            list(group(identifierA().setName("CRITERION_NAME"), equals(), ofType(BOOLEAN).setName("BOOLEAN")).setName("CRITERION_ENTRY"), comma()).setOptional().setName("CRITERION_LIST"),
                                            brace("}")
                                    ).setName("CRITERION_GROUP")
                            ).setName("ADVANCEMENT_ENTRY_VALUE")
                    ).setName("ADVANCEMENT_ENTRY"), comma()).setOptional().setName("ADVANCEMENT_LIST"),
                    brace("}")
            ).setName("ADVANCEMENT_ARGUMENT_BLOCK");

            SELECTOR_ARGUMENT.add(group(
                    choice("advancements").setName("SELECTOR_ARGUMENT_KEY"),
                    equals(),
                    choice(advancementArgumentBlock).setName("SELECTOR_ARGUMENT_VALUE")
            ));
        }

        {
            //Scores argument

            LazyTokenPatternMatch scoreArgumentBlock = group(
                    brace("{"),
                    list(group(
                            identifierA().setName("OBJECTIVE_NAME"),
                            equals(),
                            choice(matchItem(SYNTACTIC_SUGAR, "isset").setName("ISSET"), INTEGER_NUMBER_RANGE).setName("SCORE_VALUE")
                    ).setName("SCORE_ENTRY"), comma()).setOptional().setName("SCORE_LIST"),
                    brace("}")
            ).setName("SCORE_ARGUMENT_BLOCK");

            SELECTOR_ARGUMENT.add(group(
                    choice("scores").setName("SELECTOR_ARGUMENT_KEY"),
                    equals(),
                    choice(scoreArgumentBlock).setName("SELECTOR_ARGUMENT_VALUE")
            ));
        }

        {
            //NBT argument

            SELECTOR_ARGUMENT.add(group(
                    choice("nbt").setName("SELECTOR_ARGUMENT_KEY"),
                    equals(),
                    choice(group(not().setOptional(), NBT_COMPOUND)).setName("SELECTOR_ARGUMENT_VALUE")
            ));
        }
        //endregion
        //region Coordinates
        {
            LOCAL_COORDINATE.add(new LazyTokenGroupMatch().append(caret()).append(new LazyTokenGroupMatch(true).append(ofType(GLUE)).append(ofType(SHORT_REAL_NUMBER))));

            ABSOLUTE_COORDINATE.add(ofType(SHORT_REAL_NUMBER));

            RELATIVE_COORDINATE.add(new LazyTokenGroupMatch().append(tilde()).append(new LazyTokenGroupMatch(true).append(ofType(GLUE)).append(ofType(SHORT_REAL_NUMBER))));

            MIXABLE_COORDINATE.add(ABSOLUTE_COORDINATE);
            MIXABLE_COORDINATE.add(RELATIVE_COORDINATE);

            SINGLE_COORDINATE.add(MIXABLE_COORDINATE);
            SINGLE_COORDINATE.add(LOCAL_COORDINATE);

            {
                var g = new LazyTokenGroupMatch().setName("MIXED_COORDINATE_SET");
                g.append(MIXABLE_COORDINATE);
                g.append(MIXABLE_COORDINATE);
                g.append(MIXABLE_COORDINATE);
                COORDINATE_SET.add(g);
            }
            {
                var g = new LazyTokenGroupMatch().setName("LOCAL_COORDINATE_SET");
                g.append(LOCAL_COORDINATE);
                g.append(LOCAL_COORDINATE);
                g.append(LOCAL_COORDINATE);
                COORDINATE_SET.add(g);
            }

            {
                var g = new LazyTokenGroupMatch().setName("MIXED_TWO_COORDINATE_SET");
                g.append(MIXABLE_COORDINATE);
                g.append(MIXABLE_COORDINATE);
                TWO_COORDINATE_SET.add(g);
            }

            COORDINATE_SET.add(VARIABLE_MARKER);
            TWO_COORDINATE_SET.add(VARIABLE_MARKER);
        }
        //endregion
        //endregion

        //region Definition Pack grammar
        try {
            var defpack = StandardDefinitionPacks.MINECRAFT_JAVA_LATEST_SNAPSHOT;
            defpack.load();

            HashMap<String, LazyTokenStructureMatch> namespaceGroups = new HashMap<>();

            for (var def : defpack.getBlueprints("structure")) {
                STRUCTURE.add(literal(def.getName()));
            }

            for (var def : defpack.getBlueprints("difficulty")) {
                DIFFICULTY.add(literal(def.getName()));
            }

            for (var def : defpack.getBlueprints("gamemode")) {
                GAMEMODE.add(literal(def.getName()));
            }

            for (var def : defpack.getBlueprints("dimension")) {
                var s = namespaceGroups.get(def.getNamespace());

                if (s == null) {
                    var g = new LazyTokenGroupMatch().setName("DIMENSION_ID");

                    var ns = new LazyTokenGroupMatch(def.getNamespace().equals("minecraft")).setName("NAMESPACE");
                    ns.append(literal(def.getNamespace()));
                    ns.append(colon());

                    g.append(ns);

                    s = new LazyTokenStructureMatch("TYPE_NAME");
                    g.append(s);

                    namespaceGroups.put(def.getNamespace(), s);

                    DIMENSION_ID.add(g);
                }

                s.add(literal(def.getName()));
            }

            namespaceGroups.clear();

            for (var def : defpack.getBlueprints("slot")) {
                var parts = def.getName().split("\\.");

                var g = new LazyTokenGroupMatch();

                for (var i = 0; i < parts.length; i++) {
                    g.append(literal(parts[i]));
                    if (i < parts.length - 1) g.append(dot());
                }

                SLOT_ID.add(g);
            }


            for (var def : defpack.getBlueprints("block")) {

                var s = namespaceGroups.get(def.getNamespace());

                if (s == null) {
                    var g = new LazyTokenGroupMatch().setName("BLOCK_ID");

                    var ns = new LazyTokenGroupMatch(def.getNamespace().equals("minecraft")).setName("NAMESPACE");
                    ns.append(literal(def.getNamespace()));
                    ns.append(colon());

                    g.append(ns);

                    s = new LazyTokenStructureMatch("TYPE_NAME");
                    g.append(s);

                    namespaceGroups.put(def.getNamespace(), s);

                    BLOCK_ID.add(g);
                }

                s.add(literal(def.getName()));
            }

            namespaceGroups.clear();

            for (var def : defpack.getBlueprints("item")) {

                var s = namespaceGroups.get(def.getNamespace());

                if (s == null) {
                    var g = new LazyTokenGroupMatch().setName("ITEM_ID");

                    var ns = new LazyTokenGroupMatch(def.getNamespace().equals("minecraft")).setName("NAMESPACE");
                    ns.append(literal(def.getNamespace()));
                    ns.append(colon());

                    g.append(ns);

                    s = new LazyTokenStructureMatch("TYPE_NAME");
                    g.append(s);

                    namespaceGroups.put(def.getNamespace(), s);

                    ITEM_ID.add(g);
                }

                s.add(literal(def.getName()));
            }

            namespaceGroups.clear();

            for (var def : defpack.getBlueprints("entity")) {

                var s = namespaceGroups.get(def.getNamespace());

                if (s == null) {
                    var g = new LazyTokenGroupMatch().setName("ENTITY_ID_DEFAULT");

                    var ns = new LazyTokenGroupMatch(def.getNamespace().equals("minecraft")).setName("NAMESPACE");
                    ns.append(literal(def.getNamespace()));
                    ns.append(colon());

                    g.append(ns);

                    s = new LazyTokenStructureMatch("TYPE_NAME");
                    g.append(s);

                    namespaceGroups.put(def.getNamespace(), s);

                    ENTITY_ID.add(g);
                }

                s.add(literal(def.getName()));
            }

            ENTITY_ID.add(VARIABLE_MARKER);

            namespaceGroups.clear();

            for (var def : defpack.getBlueprints("effect")) {

                var s = namespaceGroups.get(def.getNamespace());

                if (s == null) {
                    var g = new LazyTokenGroupMatch().setName("EFFECT_ID");

                    var ns = new LazyTokenGroupMatch(def.getNamespace().equals("minecraft")).setName("NAMESPACE");
                    ns.append(literal(def.getNamespace()));
                    ns.append(colon());

                    g.append(ns);

                    s = new LazyTokenStructureMatch("TYPE_NAME");
                    g.append(s);

                    namespaceGroups.put(def.getNamespace(), s);

                    EFFECT_ID.add(g);
                }

                s.add(literal(def.getName()));
            }

            namespaceGroups.clear();

            for (var def : defpack.getBlueprints("enchantment")) {

                var s = namespaceGroups.get(def.getNamespace());

                if (s == null) {
                    var g = new LazyTokenGroupMatch().setName("ENCHANTMENT_ID");

                    var ns = new LazyTokenGroupMatch(def.getNamespace().equals("minecraft")).setName("NAMESPACE");
                    ns.append(literal(def.getNamespace()));
                    ns.append(colon());

                    g.append(ns);

                    s = new LazyTokenStructureMatch("TYPE_NAME");
                    g.append(s);

                    namespaceGroups.put(def.getNamespace(), s);

                    ENCHANTMENT_ID.add(g);
                }

                s.add(literal(def.getName()));
            }

            namespaceGroups.clear();

            var COLOR = new LazyTokenGroupMatch().setName("COLOR")
                    .append(real().setName("RED_COMPONENT"))
                    .append(real().setName("GREEN_COMPONENT"))
                    .append(real().setName("BLUE_COMPONENT"));


            for (var def : defpack.getBlueprints("particle")) {
                var g = new LazyTokenGroupMatch().setName("PARTICLE_ID");

                var ns = new LazyTokenGroupMatch(def.getNamespace().equals("minecraft")).setName("NAMESPACE");
                ns.append(literal(def.getNamespace()));
                ns.append(colon());

                g.append(ns);

                g.append(literal(def.getName()).setName("TYPE_NAME"));

                PARTICLE_ID.add(g);

                var g2 = new LazyTokenGroupMatch();

                g2.append(g);

                var argsGroup = new LazyTokenGroupMatch().setName("PARTICLE_ARGUMENTS");

                var allArgs = def.getProperties().get("argument");
                if (!allArgs.equals("none")) {
                    var args = allArgs.split("-");
                    for (var arg : args) {
                        switch (arg) {
                            case "int": {
                                argsGroup.append(integer());
                                break;
                            }
                            case "double": {
                                argsGroup.append(real());
                                break;
                            }
                            case "color": {
                                argsGroup.append(COLOR);
                                break;
                            }
                            case "block": {
                                argsGroup.append(BLOCK);
                                break;
                            }
                            case "item": {
                                argsGroup.append(ITEM);
                                break;
                            }
                            default: {
                                Debug.log("Invalid particle argument type '" + arg + "', could not be added to .mcfunction particle production", Debug.MessageType.ERROR);
                            }
                        }
                    }
                }

                g2.append(argsGroup);

                PARTICLE.add(g2);
            }

            namespaceGroups.clear();

            for (var def : defpack.getBlueprints("gamerule")) {
                var g = new LazyTokenGroupMatch().setName("GAMERULE_ID");

                g.append(literal(def.getName()).setName("GAMERULE"));

                GAMERULE.add(g);

                var g2 = new LazyTokenGroupMatch();

                g2.append(g);

                var argsGroup = new LazyTokenGroupMatch().setName("GAMERULE_ARGUMENT");

                var arg = def.getProperties().get("argument");

                switch (arg) {
                    case "boolean": {
                        argsGroup.append(ofType(BOOLEAN).setName("BOOLEAN"));
                        break;
                    }
                    case "int": {
                        argsGroup.append(integer());
                        break;
                    }
                    case "double": {
                        argsGroup.append(real());
                        break;
                    }
                    case "color": {
                        argsGroup.append(COLOR);
                        break;
                    }
                    case "block": {
                        argsGroup.append(BLOCK);
                        break;
                    }
                    case "item": {
                        argsGroup.append(ITEM);
                        break;
                    }
                    default: {
                        Debug.log("Invalid gamerule argument type '" + arg + "', could not be added to .mcfunction gamerule setter production", Debug.MessageType.ERROR);
                    }
                }

                g2.append(sameLine());
                g2.append(argsGroup);

                GAMERULE_SETTER.add(g2);
            }

            namespaceGroups.clear();

        } catch (IOException x) {
            Debug.log("Error in loading standard definition pack for Minecraft Java Edition 1.13: " + x.getMessage(), Debug.MessageType.ERROR);
        }
        //endregion

        //region Instructions


        var scale = group(symbol("*"), real()).setOptional();
        var typeCast = group(brace("("), ofType(NUMERIC_DATA_TYPE), brace(")")).setOptional();

        var scoreHead = group(ofType(ARROW), identifierA(), scale);
        var nbtHead = group(dot(), NBT_PATH, scale, typeCast);

        var anyHead = choice(scoreHead, nbtHead);

        var varPointer = group(VARIABLE_MARKER, anyHead);
        var entityPointer = group(ENTITY, anyHead);
        var blockPointer = group(brace("("), COORDINATE_SET, brace(")"), nbtHead);

        var nbtPointer = group(choice(ENTITY, group(brace("("), COORDINATE_SET, brace(")"))), nbtHead);

        POINTER = choice(varPointer, entityPointer, blockPointer);

        INSTRUCTION.add(
                group(literal("expr").setName("INSTRUCTION_KEYWORD"),
                        POINTER, equals(), POINTER)
        );

        {
            var entityBodyEntry = choice(
                    group(literal("default"), literal("nbt"), NBT_COMPOUND).setName("DEFAULT_NBT")
            );

            var entityBody = group(
                    brace("{"),
                    list(entityBodyEntry).setOptional().setName("ENTITY_BODY_ENTRIES"),
                    brace("}")
            ).setOptional().setName("ENTITY_DECLARATION_BODY");


            INSTRUCTION.add(
                    group(literal("register").setName("INSTRUCTION_KEYWORD"),
                            choice(
                                    group(literal("objective"), identifierA().setName("OBJECTIVE_NAME"), optional(sameLine(), identifierB().setName("CRITERIA"), optional(TEXT_COMPONENT))).setName("REGISTER_OBJECTIVE"),
                                    group(literal("databank"), identifierA().setName("DATABANK_NAME"), nbtPointer).setName("REGISTER_DATABANK"),
                                    group(literal("entity"), ofType(CASE_INSENSITIVE_RESOURCE_LOCATION).setName("ENTITY_NAME"), ENTITY_ID, entityBody).setName("REGISTER_ENTITY")
                            )
                    )
            );
        }
        {
            INSTRUCTION.add(
                    group(literal("var").setName("INSTRUCTION_KEYWORD"),
                            ofType(CASE_INSENSITIVE_RESOURCE_LOCATION).setName("VARIABLE_NAME"),
                            choice(
                                    group(optional(brace("<"), literal("integer"), brace(">")), equals(), group(integer()).setName("VARIABLE_VALUE")),
                                    group(optional(brace("<"), literal("real"), brace(">")), equals(), group(real()).setName("VARIABLE_VALUE")),
                                    group(optional(brace("<"), literal("string"), brace(">")), equals(), group(string()).setName("VARIABLE_VALUE")),
                                    group(optional(brace("<"), literal("bool"), brace(">")), equals(), group(ofType(BOOLEAN).setName("BOOLEAN")).setName("VARIABLE_VALUE")),
                                    group(optional(brace("<"), literal("entity"), brace(">")), equals(), group(ENTITY).setName("VARIABLE_VALUE")),
                                    group(optional(brace("<"), literal("block"), brace(">")), equals(), group(BLOCK_TAGGED).setName("VARIABLE_VALUE")),
                                    group(optional(brace("<"), literal("item"), brace(">")), equals(), group(ITEM_TAGGED).setName("VARIABLE_VALUE")),
                                    group(optional(brace("<"), literal("coordinates"), brace(">")), equals(), group(COORDINATE_SET).setName("VARIABLE_VALUE")),
                                    group(optional(brace("<"), literal("nbt_compound"), brace(">")), equals(), group(NBT_COMPOUND).setName("VARIABLE_VALUE")),
                                    group(optional(brace("<"), literal("nbt_path"), brace(">")), equals(), group(NBT_PATH).setName("VARIABLE_VALUE")),
                                    group(optional(brace("<"), literal("text_component"), brace(">")), equals(), group(TEXT_COMPONENT).setName("VARIABLE_VALUE")),
                                    group(equals(), choice(integer(), real(), string(), ofType(BOOLEAN).setName("BOOLEAN"), ENTITY, BLOCK_TAGGED, ITEM_TAGGED, COORDINATE_SET, NBT_COMPOUND, NBT_PATH, TEXT_COMPONENT).setName("VARIABLE_VALUE"))
                            )
                    )
            );
        }
        //endregion




    }

    private static LazyTokenItemMatch literal(String text) {
        return new LazyTokenItemMatch(TokenType.UNKNOWN, text).setName("LITERAL_" + text.toUpperCase());
    }

    private static LazyTokenItemMatch symbol(String text) {
        return new LazyTokenItemMatch(SYMBOL, text);
    }

    private static LazyTokenItemMatch matchItem(TokenType type, String text) {
        return new LazyTokenItemMatch(type, text).setName("ITEM_MATCH");
    }

    private static LazyTokenItemMatch brace(String brace) {
        return matchItem(BRACE, brace);
    }

    private static LazyTokenItemMatch colon() {
        return ofType(COLON);
    }

    private static LazyTokenItemMatch comma() {
        return ofType(COMMA).setName("COMMA");
    }

    private static LazyTokenItemMatch dot() {
        return ofType(DOT);
    }

    private static LazyTokenItemMatch equals() {
        return ofType(EQUALS);
    }

    private static LazyTokenItemMatch caret() {
        return ofType(CARET);
    }

    private static LazyTokenItemMatch tilde() {
        return ofType(TILDE);
    }

    private static LazyTokenItemMatch not() {
        return ofType(NOT).setName("NEGATED");
    }

    private static LazyTokenItemMatch hash() {
        return ofType(HASH);
    }

    private static LazyTokenItemMatch string() {
        return ofType(STRING_LITERAL).setName("STRING_LITERAL");
    }

    private static LazyTokenItemMatch integer() {
        return ofType(INTEGER_NUMBER).setName("INTEGER");
    }

    private static LazyTokenItemMatch real() {
        return ofType(REAL_NUMBER).setName("REAL");
    }

    private static LazyTokenItemMatch glue() {
        return ofType(GLUE).setName("GLUE");
    }

    private static LazyTokenItemMatch sameLine() {
        return ofType(LINE_GLUE).setName("LINE_GLUE");
    }

    private static LazyTokenItemMatch identifierA() {
        return ofType(IDENTIFIER_TYPE_A).setName("IDENTIFIER_A");
    }

    private static LazyTokenPatternMatch identifierB() {
        return ofType(IDENTIFIER_TYPE_B).setName("IDENTIFIER_B");
    }

    private static LazyTokenPatternMatch identifierC() {
        return ofType(IDENTIFIER_TYPE_C).setName("IDENTIFIER_C");
    }

    private static LazyTokenItemMatch ofType(TokenType type) {
        return new LazyTokenItemMatch(type);
    }

    private static LazyTokenStructureMatch struct(String name) {
        return new LazyTokenStructureMatch(name);
    }

    private static LazyTokenStructureMatch choice(LazyTokenPatternMatch... options) {
        if(options.length == 0) throw new IllegalArgumentException("Need one or more options for choice");
        var s = struct("CHOICE");
        for(var option : options) {
            s.add(option);
        }
        return s;
    }

    private static LazyTokenStructureMatch choice(String... options) {
        if(options.length == 0) throw new IllegalArgumentException("Need one or more options for choice");
        var s = struct("CHOICE");
        for(var option : options) {
            s.add(literal(option));
        }
        return s;
    }

    private static LazyTokenGroupMatch optional() {
        return new LazyTokenGroupMatch(true);
    }

    private static LazyTokenGroupMatch group(LazyTokenPatternMatch... items) {
        var g = new LazyTokenGroupMatch();
        for(var item : items) {
            g.append(item);
        }
        return g;
    }

    private static LazyTokenListMatch list(LazyTokenPatternMatch pattern) {
        return list(pattern, null);
    }

    private static LazyTokenListMatch list(LazyTokenPatternMatch pattern, LazyTokenPatternMatch separator) {
        return new LazyTokenListMatch(pattern, separator);
    }

    private static LazyTokenGroupMatch optional(LazyTokenPatternMatch... items) {
        var g = group(items);
        g.setOptional();
        return g;
    }
}
