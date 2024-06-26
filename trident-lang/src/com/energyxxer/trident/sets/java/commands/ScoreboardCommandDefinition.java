package com.energyxxer.trident.sets.java.commands;

import com.energyxxer.commodore.CommodoreException;
import com.energyxxer.commodore.functionlogic.commands.Command;
import com.energyxxer.commodore.functionlogic.commands.RawCommand;
import com.energyxxer.commodore.functionlogic.commands.scoreboard.*;
import com.energyxxer.commodore.functionlogic.entity.Entity;
import com.energyxxer.commodore.functionlogic.score.LocalScore;
import com.energyxxer.commodore.functionlogic.score.Objective;
import com.energyxxer.commodore.textcomponents.TextComponent;
import com.energyxxer.commodore.types.Type;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.PrismarineProductions;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import com.energyxxer.trident.compiler.TridentProductions;
import com.energyxxer.trident.compiler.analyzers.commands.SimpleCommandDefinition;
import com.energyxxer.trident.compiler.semantics.TridentExceptionUtil;
import com.energyxxer.trident.worker.tasks.SetupModuleTask;

import static com.energyxxer.prismarine.PrismarineProductions.*;
import static com.energyxxer.trident.compiler.lexer.TridentTokens.SCOREBOARD_OPERATOR;

public class ScoreboardCommandDefinition implements SimpleCommandDefinition {
    @Override
    public String[] getSwitchKeys() {
        return new String[]{"scoreboard"};
    }

    @Override
    public TokenPatternMatch createPatternMatch(PrismarineProductions productions, PrismarineProjectWorker worker) {
        return group(
                TridentProductions.commandHeader("scoreboard"),
                choice(
                        group(literal("objectives"), choice(
                                group(
                                        literal("add"),
                                        wrapper(TridentProductions.identifierA(productions)).setName("OBJECTIVE_NAME").addTags("cspn:Objective"),
                                        wrapper(TridentProductions.identifierB(productions)).setName("CRITERIA").addTags("cspn:Criteria"),
                                        wrapperOptional(productions.getOrCreateStructure("TEXT_COMPONENT")).setName("DISPLAY_NAME").addTags("cspn:Display Name")
                                ).setEvaluator((p, d) -> {
                                    ISymbolContext ctx = (ISymbolContext) d[0];
                                    String objectiveName = (String) p.find("OBJECTIVE_NAME").evaluate(ctx, String.class);
                                    String criteria = (String) p.find("CRITERIA").evaluate(ctx);
                                    TextComponent displayName = (TextComponent) p.findThenEvaluate("DISPLAY_NAME", null, ctx);

                                    try {
                                        if (ctx.get(SetupModuleTask.INSTANCE).getObjectiveManager().exists(objectiveName)) {
                                            Objective.assertNameValid(objectiveName);
                                            if (displayName != null) displayName.assertAvailable();
                                            return new RawCommand("scoreboard objectives add " + objectiveName + " " + criteria + (displayName != null ? " " + displayName.toString() : ""));
                                        } else {
                                            Objective objective = ctx.get(SetupModuleTask.INSTANCE).getObjectiveManager().create(objectiveName, criteria, displayName);
                                            return new ObjectivesAddCommand(objective);
                                        }
                                    } catch (CommodoreException x) {
                                        TridentExceptionUtil.handleCommodoreException(x, p, ctx)
                                                .map(CommodoreException.Source.FORMAT_ERROR, p.tryFind("OBJECTIVE_NAME"))
                                                .invokeThrow();
                                        throw new PrismarineException(PrismarineException.Type.IMPOSSIBLE, "Impossible code reached", p, ctx);
                                    }
                                }),
                                literal("list").setEvaluator((p, d) -> new ObjectivesListCommand()),
                                group(
                                        literal("modify"),
                                        productions.getOrCreateStructure("OBJECTIVE_NAME"),
                                        choice(
                                                group(literal("displayname"), TridentProductions.noToken().addTags("cspn:Display Name"), productions.getOrCreateStructure("TEXT_COMPONENT")).setEvaluator((p, d) -> {
                                                    ISymbolContext ctx = (ISymbolContext) d[0];
                                                    Objective objective = (Objective) d[1];

                                                    TextComponent displayName = (TextComponent) p.find("TEXT_COMPONENT").evaluate(ctx);
                                                    return new ObjectiveModifyCommand(objective, ObjectiveModifyCommand.ObjectiveModifyKey.DISPLAY_NAME, displayName);
                                                }),
                                                group(literal("rendertype"), enumChoice(ObjectiveModifyCommand.ObjectiveRenderType.class).setName("RENDER_TYPE")).setEvaluator((p, d) -> {
                                                    Objective objective = (Objective) d[1];

                                                    ObjectiveModifyCommand.ObjectiveRenderType renderType = (ObjectiveModifyCommand.ObjectiveRenderType) p.find("RENDER_TYPE").evaluate();
                                                    return new ObjectiveModifyCommand(objective, ObjectiveModifyCommand.ObjectiveModifyKey.RENDER_TYPE, renderType);
                                                })
                                        ).setName("INNER")
                                ).setSimplificationFunction(d -> {
                                    ISymbolContext ctx = (ISymbolContext) d.data[0];
                                    d.data = new Object[]{ctx, d.pattern.find("OBJECTIVE_NAME").evaluate(ctx, Objective.class)};
                                    d.pattern = d.pattern.find("INNER");
                                }),
                                group(
                                        literal("remove"),
                                        productions.getOrCreateStructure("OBJECTIVE_NAME")
                                ).setEvaluator((p, d) -> {
                                    ISymbolContext ctx = (ISymbolContext) d[0];
                                    Objective objective = (Objective) p.find("OBJECTIVE_NAME").evaluate(ctx, Objective.class);
                                    return new ObjectivesRemoveCommand(objective);
                                }),
                                group(
                                        literal("setdisplay"),
                                        productions.getOrCreateStructure("SCORE_DISPLAY_ID"),
                                        optional(TridentProductions.sameLine(), productions.getOrCreateStructure("OBJECTIVE_NAME")).setSimplificationFunctionContentIndex(1).setName("OBJECTIVE_NAME")
                                ).setEvaluator((p, d) -> {
                                    ISymbolContext ctx = (ISymbolContext) d[0];
                                    Type displaySlot = (Type) p.find("SCORE_DISPLAY_ID").evaluate(ctx);
                                    Objective objective = (Objective) p.findThenEvaluate("OBJECTIVE_NAME", null, ctx, Objective.class);
                                    return new SetObjectiveDisplayCommand(objective, displaySlot);
                                })
                        )).setSimplificationFunctionContentIndex(1),
                        group(literal("players"), choice(
                                group(
                                        choice("add", "remove", "set").setName("SCOREBOARD_PLAYERS_CHANGE_ACTION"),
                                        productions.getOrCreateStructure("SCORE"),
                                        TridentProductions.integer(productions).setName("VALUE").addTags("cspn:Value")
                                ).setEvaluator((p, d) -> {
                                    ISymbolContext ctx = (ISymbolContext) d[0];
                                    LocalScore score = (LocalScore) p.find("SCORE").evaluate(ctx);
                                    int value = (int) p.find("VALUE").evaluate(ctx);
                                    String action = p.find("SCOREBOARD_PLAYERS_CHANGE_ACTION").flatten(false);
                                    switch (action) {
                                        case "set": {
                                            return new ScoreSet(score, value);
                                        }
                                        case "remove": {
                                            value *= -1;
                                        }
                                        case "add": {
                                            return new ScoreAdd(score, value);
                                        }
                                    }
                                    return null; //can't reach this
                                }),
                                group(
                                        literal("enable"),
                                        productions.getOrCreateStructure("SCORE")
                                ).setEvaluator((p, d) -> {
                                    ISymbolContext ctx = (ISymbolContext) d[0];
                                    LocalScore score = (LocalScore) p.find("SCORE").evaluate(ctx);
                                    try {
                                        return new TriggerEnable(score.getHolder(), score.getObjective());
                                    } catch (CommodoreException x) {
                                        TridentExceptionUtil.handleCommodoreException(x, p, ctx)
                                                .map(CommodoreException.Source.ENTITY_ERROR, p.tryFind("SCORE"))
                                                .map(CommodoreException.Source.TYPE_ERROR, p.tryFind("SCORE"))
                                                .invokeThrow();
                                        throw new PrismarineException(PrismarineException.Type.IMPOSSIBLE, "Impossible code reached", p, ctx);
                                    }
                                }),
                                group(literal("get"), productions.getOrCreateStructure("SCORE")).setEvaluator((p, d) -> {
                                    ISymbolContext ctx = (ISymbolContext) d[0];
                                    LocalScore score = (LocalScore) p.find("SCORE").evaluate(ctx);
                                    try {
                                        return new ScoreGet(score);
                                    } catch (CommodoreException x) {
                                        TridentExceptionUtil.handleCommodoreException(x, p, ctx)
                                                .map(CommodoreException.Source.ENTITY_ERROR, p.tryFind("SCORE"))
                                                .invokeThrow();
                                        throw new PrismarineException(PrismarineException.Type.IMPOSSIBLE, "Impossible code reached", p, ctx);
                                    }
                                }),
                                group(literal("list"), optional(TridentProductions.sameLine(), productions.getOrCreateStructure("ENTITY")).setSimplificationFunctionContentIndex(1).setName("ENTITY")).setEvaluator((p, d) -> {
                                    ISymbolContext ctx = (ISymbolContext) d[0];
                                    Entity entity = (Entity) p.findThenEvaluate("ENTITY", null, ctx);
                                    try {
                                        return new ScoreList(entity);
                                    } catch (CommodoreException x) {
                                        TridentExceptionUtil.handleCommodoreException(x, p, ctx)
                                                .map(CommodoreException.Source.ENTITY_ERROR, p.tryFind("ENTITY"))
                                                .invokeThrow();
                                        throw new PrismarineException(PrismarineException.Type.IMPOSSIBLE, "Impossible code reached", p, ctx);
                                    }
                                }),
                                group(
                                        literal("operation"),
                                        wrapper(productions.getOrCreateStructure("SCORE")).setName("TARGET_SCORE").addTags("cspn:Target"),
                                        ofType(SCOREBOARD_OPERATOR).setName("OPERATOR"),
                                        wrapper(productions.getOrCreateStructure("SCORE")).setName("SOURCE_SCORE").addTags("cspn:Source")
                                ).setEvaluator((p, d) -> {
                                    ISymbolContext ctx = (ISymbolContext) d[0];

                                    LocalScore target = (LocalScore) p.find("TARGET_SCORE.SCORE").evaluate(ctx);
                                    LocalScore source = (LocalScore) p.find("SOURCE_SCORE.SCORE").evaluate(ctx);
                                    String rawOperator = p.find("OPERATOR").flatten(false);
                                    return new ScorePlayersOperation(target, ScorePlayersOperation.Operation.getOperationForSymbol(rawOperator), source);
                                }),
                                group(literal("reset"), productions.getOrCreateStructure("SCORE_OPTIONAL_OBJECTIVE")).setName("RESET").setEvaluator((p, d) -> {
                                    ISymbolContext ctx = (ISymbolContext) d[0];
                                    return new ScoreReset((LocalScore) p.find("SCORE_OPTIONAL_OBJECTIVE").evaluate(ctx));
                                })
                        )).setSimplificationFunctionContentIndex(1)
                ).setName("INNER")
        ).setSimplificationFunctionFind("INNER");
    }

    @Override
    public Command parseSimple(TokenPattern<?> pattern, ISymbolContext ctx) {
        throw new UnsupportedOperationException(); //this step is optimized away
    }
}
