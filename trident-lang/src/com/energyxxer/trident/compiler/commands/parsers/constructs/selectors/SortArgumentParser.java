package com.energyxxer.trident.compiler.commands.parsers.constructs.selectors;

import com.energyxxer.commodore.functionlogic.selector.arguments.SelectorArgument;
import com.energyxxer.commodore.functionlogic.selector.arguments.SortArgument;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.trident.compiler.TridentCompiler;
import com.energyxxer.trident.compiler.commands.parsers.general.ParserMember;

@ParserMember(key = "sort")
public class SortArgumentParser implements SelectorArgumentParser {
    @Override
    public SelectorArgument parse(TokenPattern<?> pattern, TridentCompiler compiler) {
        return new SortArgument(SortArgument.SortMode.valueOf(pattern.flatten(false).toUpperCase()));
    }
}
