package com.energyxxer.trident.compiler.analyzers.commands;

import com.energyxxer.commodore.CommodoreException;
import com.energyxxer.commodore.functionlogic.commands.Command;
import com.energyxxer.commodore.functionlogic.commands.scoreboard.*;
import com.energyxxer.commodore.functionlogic.entity.Entity;
import com.energyxxer.commodore.functionlogic.score.LocalScore;
import com.energyxxer.commodore.functionlogic.score.Objective;
import com.energyxxer.commodore.textcomponents.TextComponent;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.pattern_matching.structures.TokenStructure;
import com.energyxxer.trident.compiler.analyzers.constructs.CommonParsers;
import com.energyxxer.trident.compiler.analyzers.constructs.EntityParser;
import com.energyxxer.trident.compiler.analyzers.constructs.TextParser;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerMember;
import com.energyxxer.trident.compiler.semantics.symbols.ISymbolContext;
import com.energyxxer.trident.compiler.semantics.TridentException;

@AnalyzerMember(key = "scoreboard")
public class ScoreboardParser implements SimpleCommandParser {
    @Override
    public Command parseSimple(TokenPattern<?> pattern, ISymbolContext ctx) {
        TokenPattern<?> inner = ((TokenStructure)pattern.find("CHOICE")).getContents();
        switch(inner.getName()) {
            case "OBJECTIVES": {
                return parseObjectives(inner, ctx);
            }
            case "PLAYERS": {
                return parsePlayers(inner, ctx);
            }
            default: {
                throw new TridentException(TridentException.Source.IMPOSSIBLE, "Unknown grammar branch name '" + inner.getName() + "'", inner, ctx);
            }
        }
    }

    private Command parseObjectives(TokenPattern<?> pattern, ISymbolContext ctx) {
        TokenPattern<?> inner = ((TokenStructure)pattern.find("CHOICE")).getContents();
        switch(inner.getName()) {
            case "ADD": {
                String objectiveName = CommonParsers.parseIdentifierA(inner.find("OBJECTIVE_NAME.IDENTIFIER_A"), ctx);
                String criteria = inner.find("CRITERIA").flatten(false);
                TextComponent displayName = TextParser.parseTextComponent(inner.find(".TEXT_COMPONENT"), ctx);
                Objective objective;
                if(ctx.getCompiler().getModule().getObjectiveManager().contains(objectiveName)) {
                    objective = ctx.getCompiler().getModule().getObjectiveManager().get(objectiveName);
                } else {
                    objective = ctx.getCompiler().getModule().getObjectiveManager().create(objectiveName, criteria, displayName, true);
                }
                return new ObjectivesAddCommand(objective);
            }
            case "LIST": {
                return new ObjectivesListCommand();
            }
            case "MODIFY": {
                Objective objective = CommonParsers.parseObjective(inner.find("OBJECTIVE_NAME"), ctx);
                TokenPattern<?> sub = ((TokenStructure)inner.find("CHOICE")).getContents();
                switch(sub.getName()) {
                    case "DISPLAYNAME": {
                        return new ObjectiveModifyCommand(objective, ObjectiveModifyCommand.ObjectiveModifyKey.DISPLAY_NAME, TextParser.parseTextComponent(sub.find("TEXT_COMPONENT"), ctx));
                    }
                    case "RENDERTYPE": {
                        return new ObjectiveModifyCommand(objective, ObjectiveModifyCommand.ObjectiveModifyKey.RENDER_TYPE, ObjectiveModifyCommand.ObjectiveRenderType.valueOf(sub.find("CHOICE").flatten(false).toUpperCase()));
                    }
                    default: {
                        throw new TridentException(TridentException.Source.IMPOSSIBLE, "Unknown grammar branch name '" + sub.getName() + "'", sub, ctx);
                    }
                }
            }
            case "REMOVE": {
                Objective objective = CommonParsers.parseObjective(inner.find("OBJECTIVE_NAME"), ctx);
                return new ObjectivesRemoveCommand(objective);
            }
            case "SETDISPLAY": {
                SetObjectiveDisplayCommand.ScoreDisplay displaySlot = SetObjectiveDisplayCommand.ScoreDisplay.getValueForKey(inner.find("DISPLAY_SLOT").flatten(false));
                TokenPattern<?> objectiveClause = inner.find("OBJECTIVE_CLAUSE");
                if(objectiveClause != null) {
                    return new SetObjectiveDisplayCommand(
                            CommonParsers.parseObjective(objectiveClause.find("OBJECTIVE_NAME"), ctx),
                            displaySlot
                    );
                }
                return new SetObjectiveDisplayCommand(displaySlot);
            }
            default: {
                throw new TridentException(TridentException.Source.IMPOSSIBLE, "Unknown grammar branch name '" + inner.getName() + "'", inner, ctx);
            }
        }
    }

    private Command parsePlayers(TokenPattern<?> pattern, ISymbolContext ctx) {
        TokenPattern<?> inner = ((TokenStructure)pattern.find("CHOICE")).getContents();
        switch(inner.getName()) {
            case "CHANGE": {
                Entity entity = EntityParser.parseEntity(inner.find("ENTITY"), ctx);
                Objective objective = CommonParsers.parseObjective(inner.find("OBJECTIVE_NAME"), ctx);
                int amount = CommonParsers.parseInt(inner.find("INTEGER"), ctx);

                if(inner.find("CHOICE.LITERAL_SET") != null) return new ScoreSet(new LocalScore(entity, objective), amount);
                if(inner.find("CHOICE.LITERAL_REMOVE") != null) amount *= -1;
                return new ScoreAdd(new LocalScore(entity, objective), amount);
            }
            case "ENABLE": {
                Entity entity = EntityParser.parseEntity(inner.find("ENTITY"), ctx);
                Objective objective = CommonParsers.parseObjective(inner.find("OBJECTIVE_NAME"), ctx);
                try {
                    return new TriggerEnable(entity, objective);
                } catch(CommodoreException x) {
                    TridentException.handleCommodoreException(x, inner, ctx)
                            .map(CommodoreException.Source.ENTITY_ERROR, inner.find("ENTITY"))
                            .map(CommodoreException.Source.TYPE_ERROR, inner.find("OBJECTIVE_NAME"))
                            .invokeThrow();
                    throw new TridentException(TridentException.Source.IMPOSSIBLE, "Impossible code reached", inner, ctx);
                }
            }
            case "GET": {
                Entity entity = EntityParser.parseEntity(inner.find("ENTITY"), ctx);
                Objective objective = CommonParsers.parseObjective(inner.find("OBJECTIVE_NAME"), ctx);
                try {
                    return new ScoreGet(new LocalScore(entity, objective));
                } catch(CommodoreException x) {
                    TridentException.handleCommodoreException(x, inner, ctx)
                            .map(CommodoreException.Source.ENTITY_ERROR, inner.find("ENTITY"))
                            .invokeThrow();
                    throw new TridentException(TridentException.Source.IMPOSSIBLE, "Impossible code reached", inner, ctx);
                }
            }
            case "LIST": {
                Entity entity = EntityParser.parseEntity(inner.find(".ENTITY"), ctx);
                try {
                    return new ScoreList(entity);
                } catch(CommodoreException x) {
                    TridentException.handleCommodoreException(x, inner, ctx)
                            .map(CommodoreException.Source.ENTITY_ERROR, inner.find(".ENTITY"))
                            .invokeThrow();
                }
            }
            case "OPERATION": {
                LocalScore target = new LocalScore(
                        EntityParser.parseEntity(inner.find("TARGET.ENTITY"), ctx),
                        CommonParsers.parseObjective(inner.find("TARGET_OBJECTIVE"), ctx)
                );
                LocalScore source = new LocalScore(
                        EntityParser.parseEntity(inner.find("SOURCE.ENTITY"), ctx),
                        CommonParsers.parseObjective(inner.find("SOURCE_OBJECTIVE"), ctx)
                );
                String rawOperator = inner.find("OPERATOR").flatten(false);
                return new ScorePlayersOperation(target, ScorePlayersOperation.Operation.getOperationForSymbol(rawOperator), source);
            }
            case "RESET": {
                Entity entity = EntityParser.parseEntity(inner.find("TARGET.ENTITY"), ctx);
                TokenPattern<?> objectiveClause = inner.find("OBJECTIVE_CLAUSE");
                if(objectiveClause != null) {
                    Objective objective = CommonParsers.parseObjective(objectiveClause.find("OBJECTIVE_NAME"), ctx);
                    if(entity != null) {
                        return new ScoreReset(entity, objective);
                    } else {
                        return new ScoreReset(objective);
                    }
                }

                if(entity != null) return new ScoreReset(entity);
                else return new ScoreReset();
            }
            default: {
                throw new TridentException(TridentException.Source.IMPOSSIBLE, "Unknown grammar branch name '" + inner.getName() + "'", inner, ctx);
            }
        }
    }
}
