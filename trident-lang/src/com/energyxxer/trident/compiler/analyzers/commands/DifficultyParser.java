package com.energyxxer.trident.compiler.analyzers.commands;

import com.energyxxer.commodore.functionlogic.commands.Command;
import com.energyxxer.commodore.functionlogic.commands.difficulty.DifficultyQueryCommand;
import com.energyxxer.commodore.functionlogic.commands.difficulty.DifficultySetCommand;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerMember;
import com.energyxxer.trident.compiler.semantics.TridentFile;

@AnalyzerMember(key = "difficulty")
public class DifficultyParser implements CommandParser {
    @Override
    public Command parse(TokenPattern<?> pattern, TridentFile file) {
        TokenPattern<?> rawDifficulty = pattern.find(".DIFFICULTY");
        if(rawDifficulty != null) {
            return new DifficultySetCommand(file.getCompiler().getModule().minecraft.types.difficulty.get(rawDifficulty.flatten(false)));
        } else return new DifficultyQueryCommand();
    }
}