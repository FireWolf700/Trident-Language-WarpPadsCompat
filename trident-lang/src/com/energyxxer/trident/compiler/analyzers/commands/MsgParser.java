package com.energyxxer.trident.compiler.analyzers.commands;

import com.energyxxer.commodore.CommodoreException;
import com.energyxxer.commodore.functionlogic.commands.Command;
import com.energyxxer.commodore.functionlogic.commands.msg.MessageCommand;
import com.energyxxer.commodore.functionlogic.entity.Entity;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.trident.compiler.analyzers.constructs.EntityParser;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerMember;
import com.energyxxer.trident.compiler.lexer.TridentTokens;
import com.energyxxer.trident.compiler.semantics.TridentException;
import com.energyxxer.trident.compiler.semantics.TridentFile;

@AnalyzerMember(key = "msg")
public class MsgParser implements CommandParser {
    @Override
    public Command parse(TokenPattern<?> pattern, TridentFile file) {
        Entity entity = EntityParser.parseEntity(pattern.find("ENTITY"), file);
        String message = pattern.search(TridentTokens.TRAILING_STRING).get(0).value;

        try {
            return new MessageCommand(entity, message);
        } catch(CommodoreException x) {
            TridentException.handleCommodoreException(x, pattern, file)
                    .map(CommodoreException.Source.ENTITY_ERROR, pattern.find("ENTITY"))
                    .invokeThrow();
            throw new TridentException(TridentException.Source.IMPOSSIBLE, "Impossible code reached", pattern, file);
        }
    }

    @AnalyzerMember(key = "w")
    public static class MsgParserAlias0 extends MsgParser implements CommandParser {}

    @AnalyzerMember(key = "tell")
    public static class MsgParserAlias1 extends MsgParser implements CommandParser {}
}