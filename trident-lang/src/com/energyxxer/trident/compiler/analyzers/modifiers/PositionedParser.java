package com.energyxxer.trident.compiler.analyzers.modifiers;

import com.energyxxer.commodore.CommodoreException;
import com.energyxxer.commodore.functionlogic.commands.execute.ExecuteModifier;
import com.energyxxer.commodore.functionlogic.commands.execute.ExecutePositionedAsEntity;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.pattern_matching.structures.TokenStructure;
import com.energyxxer.trident.compiler.analyzers.constructs.CoordinateParser;
import com.energyxxer.trident.compiler.analyzers.constructs.EntityParser;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerMember;
import com.energyxxer.trident.compiler.semantics.TridentException;
import com.energyxxer.trident.compiler.semantics.TridentFile;

@AnalyzerMember(key = "positioned")
public class PositionedParser implements ModifierParser {
    @Override
    public ExecuteModifier parse(TokenPattern<?> pattern, TridentFile file) {
        TokenPattern<?> branch = ((TokenStructure) pattern.find("CHOICE")).getContents();
        switch(branch.getName()) {
            case "ENTITY_BRANCH": {
                try {
                    return new ExecutePositionedAsEntity(EntityParser.parseEntity(branch.find("ENTITY"), file));
                } catch(CommodoreException x) {
                    TridentException.handleCommodoreException(x, pattern, file)
                            .map(CommodoreException.Source.ENTITY_ERROR, branch.find("ENTITY"))
                            .invokeThrow();
                }
            }
            case "BLOCK_BRANCH": {
                return CoordinateParser.parse(branch.find("COORDINATE_SET"), file);
            }
            default: {
                throw new TridentException(TridentException.Source.IMPOSSIBLE, "Unknown grammar branch name '" + branch.getName() + "'", branch, file);
            }
        }
    }
}
