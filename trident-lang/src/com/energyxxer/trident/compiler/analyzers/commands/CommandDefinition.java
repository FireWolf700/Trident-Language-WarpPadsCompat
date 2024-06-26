package com.energyxxer.trident.compiler.analyzers.commands;

import com.energyxxer.commodore.functionlogic.commands.Command;
import com.energyxxer.commodore.functionlogic.commands.execute.ExecuteModifier;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.PrismarineProductions;
import com.energyxxer.prismarine.providers.PatternSwitchProviderUnit;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;

import java.util.Collection;

public interface CommandDefinition extends PatternSwitchProviderUnit {
    @Override
    default String[] getTargetProductionNames() {
        return null;
    }

    @Override
    default Object evaluate(TokenPattern<?> pattern, Object... data) {
        return parse(pattern, ((ISymbolContext) data[0]), ((Collection<ExecuteModifier>) data[1]));
    }

    Collection<Command> parse(TokenPattern<?> pattern, ISymbolContext ctx, Collection<ExecuteModifier> modifiers);

    TokenPatternMatch createPatternMatch(PrismarineProductions productions, PrismarineProjectWorker worker);
}
