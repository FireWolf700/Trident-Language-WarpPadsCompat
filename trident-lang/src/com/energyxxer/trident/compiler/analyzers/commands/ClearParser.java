package com.energyxxer.trident.compiler.analyzers.commands;

import com.energyxxer.commodore.CommodoreException;
import com.energyxxer.commodore.functionlogic.commands.Command;
import com.energyxxer.commodore.functionlogic.commands.clear.ClearCommand;
import com.energyxxer.commodore.item.Item;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.trident.compiler.analyzers.constructs.CommonParsers;
import com.energyxxer.trident.compiler.analyzers.constructs.EntityParser;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerMember;
import com.energyxxer.trident.compiler.semantics.TridentException;
import com.energyxxer.trident.compiler.semantics.TridentFile;
import com.energyxxer.trident.compiler.semantics.custom.items.NBTMode;

@AnalyzerMember(key = "clear")
public class ClearParser implements CommandParser {
    @Override
    public Command parse(TokenPattern<?> pattern, TridentFile file) {
        Item item = CommonParsers.parseItem(pattern.find("..ITEM_TAGGED"), file, NBTMode.TESTING);
        TokenPattern<?> amountPattern = pattern.find("..AMOUNT");
        int amount = amountPattern != null ? CommonParsers.parseInt(amountPattern, file) : -1;
        try {
            return new ClearCommand(EntityParser.parseEntity(pattern.find(".ENTITY"), file), item, amount);
        } catch(CommodoreException x) {
            TridentException.handleCommodoreException(x, pattern, file)
                    .map(CommodoreException.Source.ENTITY_ERROR, pattern.find(".ENTITY"))
                    .map(CommodoreException.Source.NUMBER_LIMIT_ERROR, pattern.find("..AMOUNT"))
                    .map(CommodoreException.Source.TYPE_ERROR, pattern.find("..ITEM_TAGGED"))
                    .invokeThrow();
            throw new TridentException(TridentException.Source.IMPOSSIBLE, "Impossible code reached", pattern, file);
        }
    }
}