package com.energyxxer.trident.compiler.analyzers.constructs.selectors;

import com.energyxxer.commodore.functionlogic.nbt.TagCompound;
import com.energyxxer.commodore.functionlogic.selector.arguments.NBTArgument;
import com.energyxxer.commodore.functionlogic.selector.arguments.SelectorArgument;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.nbtmapper.PathContext;
import com.energyxxer.trident.compiler.analyzers.constructs.NBTParser;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerMember;
import com.energyxxer.trident.compiler.semantics.TridentFile;

import static com.energyxxer.nbtmapper.tags.PathProtocol.ENTITY;

@AnalyzerMember(key = "nbt")
public class NBTArgumentParser implements SimpleSelectorArgumentParser {
    @Override
    public SelectorArgument parseSingle(TokenPattern<?> pattern, TridentFile file) {
        TagCompound nbt = NBTParser.parseCompound(pattern.find("NBT_COMPOUND"), file);
        PathContext context = new PathContext().setIsSetting(false).setProtocol(ENTITY);
        NBTParser.analyzeTag(nbt, context, pattern.find("NBT_COMPOUND"), file);
        return new NBTArgument(nbt, pattern.find("NEGATED") != null);
    }
}