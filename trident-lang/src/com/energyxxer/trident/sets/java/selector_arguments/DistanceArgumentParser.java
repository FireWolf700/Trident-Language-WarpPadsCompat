package com.energyxxer.trident.sets.java.selector_arguments;

import com.energyxxer.commodore.functionlogic.selector.arguments.DistanceArgument;
import com.energyxxer.commodore.util.DoubleRange;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.prismarine.PrismarineProductions;
import com.energyxxer.prismarine.providers.PatternSwitchProviderUnit;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import com.energyxxer.trident.compiler.TridentProductions;

import static com.energyxxer.prismarine.PrismarineProductions.*;

public class DistanceArgumentParser implements PatternSwitchProviderUnit {
    @Override
    public String[] getSwitchKeys() {
        return new String[] {"distance"};
    }

    @Override
    public TokenPatternMatch createPatternMatch(PrismarineProductions productions, PrismarineProjectWorker worker) {
        return group(
                literal("distance").setName("SELECTOR_ARGUMENT_KEY"),
                TridentProductions.equals(),
                wrapper(productions.getOrCreateStructure("REAL_NUMBER_RANGE"), (v, p, d) -> new DistanceArgument((DoubleRange) v))
        ).setSimplificationFunctionContentIndex(2);
    }
}
