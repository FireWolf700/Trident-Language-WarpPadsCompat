package com.energyxxer.trident.compiler.analyzers.commands;

import com.energyxxer.commodore.functionlogic.commands.Command;
import com.energyxxer.commodore.functionlogic.commands.tag.TagCommand;
import com.energyxxer.commodore.functionlogic.commands.tag.TagQueryCommand;
import com.energyxxer.commodore.functionlogic.entity.Entity;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.trident.compiler.analyzers.constructs.CommonParsers;
import com.energyxxer.trident.compiler.analyzers.constructs.EntityParser;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerMember;
import com.energyxxer.trident.compiler.semantics.TridentException;
import com.energyxxer.trident.compiler.semantics.symbols.ISymbolContext;

import java.util.Collection;
import java.util.Collections;

@AnalyzerMember(key = "tag")
public class TagParser implements CommandParser {
    @Override
    public Collection<Command> parse(TokenPattern<?> pattern, ISymbolContext ctx) {
        Entity entity = EntityParser.parseEntity(pattern.find("ENTITY"), ctx);
        switch(pattern.find("CHOICE").flattenTokens().get(0).value) {
            case "list": return Collections.singletonList(new TagQueryCommand(entity));
            case "add":
                return Collections.singletonList(new TagCommand(TagCommand.Action.ADD, entity, CommonParsers.parseIdentifierA(pattern.find("CHOICE.IDENTIFIER_A"), ctx)));
            case "remove":
                return Collections.singletonList(new TagCommand(TagCommand.Action.REMOVE, entity, CommonParsers.parseIdentifierA(pattern.find("CHOICE.IDENTIFIER_A"), ctx)));
            default: {
                throw new TridentException(TridentException.Source.IMPOSSIBLE, "Unknown grammar branch name '" + pattern.getName() + "'", pattern, ctx);
            }
        }
    }
}
