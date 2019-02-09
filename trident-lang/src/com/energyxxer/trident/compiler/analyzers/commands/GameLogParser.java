package com.energyxxer.trident.compiler.analyzers.commands;

import com.energyxxer.commodore.functionlogic.commands.Command;
import com.energyxxer.commodore.functionlogic.commands.function.FunctionCommand;
import com.energyxxer.commodore.functionlogic.entity.Entity;
import com.energyxxer.commodore.textcomponents.ListTextComponent;
import com.energyxxer.commodore.textcomponents.SelectorTextComponent;
import com.energyxxer.commodore.textcomponents.StringTextComponent;
import com.energyxxer.commodore.textcomponents.TextComponent;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.trident.compiler.analyzers.constructs.InterpolationManager;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerMember;
import com.energyxxer.trident.compiler.analyzers.type_handlers.ListType;
import com.energyxxer.trident.compiler.semantics.TridentException;
import com.energyxxer.trident.compiler.semantics.custom.special.GameLogFetcherFile;
import com.energyxxer.trident.compiler.semantics.symbols.ISymbolContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

@AnalyzerMember(key = "gamelog")
public class GameLogParser implements CommandParser {
    private static Stack<Object> toStringRecursion = new Stack<>();

    @Override
    public Collection<Command> parse(TokenPattern<?> pattern, ISymbolContext ctx) {
        if(ctx.getStaticParentFile().getLanguageLevel() < 3) {
            throw new TridentException(TridentException.Source.LANGUAGE_LEVEL_ERROR, "The gamelog command is only supported in language level 3", pattern, ctx);
        }

        ArrayList<Command> commands = new ArrayList<>();

        GameLogFetcherFile fetchFile = (GameLogFetcherFile) ctx.getCompiler().getSpecialFileManager().get("debug_fetch");
        fetchFile.startCompilation();

        commands.add(new FunctionCommand(fetchFile.getFunction()));

        TextComponent message = objectToTextComponent(InterpolationManager.parse(pattern.find("LINE_SAFE_INTERPOLATION_VALUE"), ctx));
        String key = pattern.find("DEBUG_GROUP").flatten(false);

        commands.add(fetchFile.getTellrawCommandFor(key, message, pattern, ctx));

        return commands;
    }

    private static TextComponent objectToTextComponent(Object obj) {
        if(toStringRecursion.contains(obj)) {
            return new StringTextComponent("...recursive...");
        }
        if(obj instanceof TextComponent) {
            return ((TextComponent) obj);
        } else if(obj instanceof String) {
            return new StringTextComponent(((String) obj));
        } else if(obj instanceof Entity) {
            return new SelectorTextComponent(((Entity) obj));
        } else if(obj instanceof ListType) {
            toStringRecursion.push(obj);
            ListTextComponent list = new ListTextComponent();
            for(Object inner : ((ListType) obj)) {
                list.append(objectToTextComponent(inner));
            }
            return list;
        } else {
            return new StringTextComponent(InterpolationManager.castToString(obj));
        }
    }
}
