package com.energyxxer.trident.compiler.analyzers.commands;

import com.energyxxer.commodore.CommodoreException;
import com.energyxxer.commodore.functionlogic.commands.Command;
import com.energyxxer.commodore.functionlogic.commands.enchant.EnchantCommand;
import com.energyxxer.commodore.functionlogic.entity.Entity;
import com.energyxxer.commodore.types.Type;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.trident.compiler.analyzers.constructs.CommonParsers;
import com.energyxxer.trident.compiler.analyzers.constructs.EntityParser;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerMember;
import com.energyxxer.trident.compiler.semantics.TridentException;
import com.energyxxer.trident.compiler.semantics.TridentFile;

@AnalyzerMember(key = "enchant")
public class EnchantParser implements CommandParser {
    @Override
    public Command parse(TokenPattern<?> pattern, TridentFile file) {
        Entity entity = EntityParser.parseEntity(pattern.find("ENTITY"), file);
        Type enchantment = CommonParsers.parseType(pattern.find("ENCHANTMENT_ID"), file, d -> d.enchantment);
        int level = 1;
        TokenPattern<?> rawLevel = pattern.find("LEVEL");
        if(rawLevel != null) {
            level = CommonParsers.parseInt(rawLevel, file);
        }
        try {
            return new EnchantCommand(entity, enchantment, level);
        } catch(CommodoreException x) {
            TridentException.handleCommodoreException(x, pattern, file)
                    .map(CommodoreException.Source.ENTITY_ERROR, pattern.find("ENTITY"))
                    .map(CommodoreException.Source.NUMBER_LIMIT_ERROR, pattern.find("LEVEL"))
                    .map(CommodoreException.Source.TYPE_ERROR, pattern.find("ENCHANTMENT_ID"))
                    .invokeThrow();
            throw new TridentException(TridentException.Source.IMPOSSIBLE, "Impossible code reached", pattern, file);
        }
    }
}
