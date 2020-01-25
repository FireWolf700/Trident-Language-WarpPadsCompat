package com.energyxxer.trident.compiler.analyzers.commands;

import com.energyxxer.commodore.CommodoreException;
import com.energyxxer.commodore.functionlogic.commands.Command;
import com.energyxxer.commodore.functionlogic.commands.title.TitleClearCommand;
import com.energyxxer.commodore.functionlogic.commands.title.TitleResetCommand;
import com.energyxxer.commodore.functionlogic.commands.title.TitleShowCommand;
import com.energyxxer.commodore.functionlogic.commands.title.TitleTimesCommand;
import com.energyxxer.commodore.functionlogic.entity.Entity;
import com.energyxxer.commodore.textcomponents.TextComponent;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.pattern_matching.structures.TokenStructure;
import com.energyxxer.trident.compiler.analyzers.constructs.CommonParsers;
import com.energyxxer.trident.compiler.analyzers.constructs.EntityParser;
import com.energyxxer.trident.compiler.analyzers.constructs.TextParser;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerMember;
import com.energyxxer.trident.compiler.semantics.TridentException;
import com.energyxxer.trident.compiler.semantics.symbols.ISymbolContext;

@AnalyzerMember(key = "title")
public class TitleParser implements SimpleCommandParser {
    @Override
    public Command parseSimple(TokenPattern<?> pattern, ISymbolContext ctx) {
        Entity entity = EntityParser.parseEntity(pattern.find("ENTITY"), ctx);
        TokenPattern<?> inner = ((TokenStructure)pattern.find("CHOICE")).getContents();
        switch(inner.getName()) {
            case "SHOW": {
                TextComponent text = TextParser.parseTextComponent(inner.find("TEXT_COMPONENT"), ctx);
                TitleShowCommand.Display display = TitleShowCommand.Display.valueOf(inner.find("DISPLAY").flatten(false).toUpperCase());

                try {
                    return new TitleShowCommand(entity, display, text);
                } catch(CommodoreException x) {
                    TridentException.handleCommodoreException(x, pattern, ctx)
                            .map(CommodoreException.Source.ENTITY_ERROR, pattern.find("ENTITY"))
                            .invokeThrow();
                }
            }
            case "CLEAR_RESET": {
                try {
                    return inner.find("LITERAL_CLEAR") != null ? new TitleClearCommand(entity) : new TitleResetCommand(entity);
                } catch(CommodoreException x) {
                    TridentException.handleCommodoreException(x, pattern, ctx)
                            .map(CommodoreException.Source.ENTITY_ERROR, pattern.find("ENTITY"))
                            .invokeThrow();
                }
            }
            case "TIMES": {
                int fadeIn = CommonParsers.parseInt(inner.find("FADEIN"), ctx);
                int stay = CommonParsers.parseInt(inner.find("STAY"), ctx);
                int fadeOut = CommonParsers.parseInt(inner.find("FADEOUT"), ctx);
                try {
                    return new TitleTimesCommand(entity, fadeIn, stay, fadeOut);
                } catch(CommodoreException x) {
                    TridentException.handleCommodoreException(x, pattern, ctx)
                            .map(CommodoreException.Source.ENTITY_ERROR, pattern.find("ENTITY"))
                            .map("FADE_IN", inner.find("FADEIN"))
                            .map("STAY", inner.find("STAY"))
                            .map("FADE_OUT", inner.find("FADEOUT"))
                            .invokeThrow();
                }
            }
            default: {
                throw new TridentException(TridentException.Source.IMPOSSIBLE, "Unknown grammar branch name '" + inner.getName() + "'", inner, ctx);
            }
        }
    }
}