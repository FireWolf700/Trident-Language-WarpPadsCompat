package com.energyxxer.trident.compiler.analyzers.modifiers;

import com.energyxxer.commodore.CommodoreException;
import com.energyxxer.commodore.functionlogic.commands.execute.EntityAnchor;
import com.energyxxer.commodore.functionlogic.commands.execute.ExecuteFacingBlock;
import com.energyxxer.commodore.functionlogic.commands.execute.ExecuteFacingEntity;
import com.energyxxer.commodore.functionlogic.commands.execute.ExecuteModifier;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.pattern_matching.structures.TokenStructure;
import com.energyxxer.trident.compiler.analyzers.constructs.CoordinateParser;
import com.energyxxer.trident.compiler.analyzers.constructs.EntityParser;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerMember;
import com.energyxxer.trident.compiler.lexer.TridentTokens;
import com.energyxxer.trident.compiler.semantics.TridentException;
import com.energyxxer.trident.compiler.semantics.TridentFile;

import java.util.List;

@AnalyzerMember(key = "facing")
public class FacingParser implements SimpleModifierParser {
    @Override
    public ExecuteModifier parseSingle(TokenPattern<?> pattern, TridentFile file) {
        TokenPattern<?> branch = ((TokenStructure) pattern.find("CHOICE")).getContents();
        switch(branch.getName()) {
            case "ENTITY_BRANCH": {
                List<Token> anchorToken = branch.search(TridentTokens.ANCHOR);
                try {
                    return new ExecuteFacingEntity(EntityParser.parseEntity(branch.find("ENTITY"), file), (!anchorToken.isEmpty() && anchorToken.get(0).value.equals("eyes")) ? EntityAnchor.EYES : EntityAnchor.FEET);
                } catch(CommodoreException x) {
                    TridentException.handleCommodoreException(x, pattern, file)
                            .map(CommodoreException.Source.ENTITY_ERROR, branch.find(".ENTITY"))
                            .invokeThrow();
                }
            }
            case "BLOCK_BRANCH": {
                return new ExecuteFacingBlock(CoordinateParser.parse(branch.find("COORDINATE_SET"), file));
            }
            default: {
                throw new TridentException(TridentException.Source.IMPOSSIBLE, "Unknown grammar branch name '" + branch.getName() + "'", branch, file);
            }
        }
    }
}
