package com.energyxxer.trident.compiler.commands.parsers.type_handlers;

import com.energyxxer.commodore.functionlogic.nbt.*;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.trident.compiler.commands.parsers.general.ParserMember;
import com.energyxxer.trident.compiler.semantics.TridentFile;

@ParserMember(key = "java.lang.Integer")
public class IntType implements VariableTypeHandler<Integer> {
    @Override
    public Object getMember(Integer object, String member, TokenPattern<?> pattern, TridentFile file, boolean keepSymbol) {
        throw new MemberNotFoundException();
    }

    @Override
    public Object getIndexer(Integer object, Object index, TokenPattern<?> pattern, TridentFile file, boolean keepSymbol) {
        throw new MemberNotFoundException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F> F cast(Integer object, Class<F> targetType, TokenPattern<?> pattern, TridentFile file) {
        if(targetType == Double.class || targetType == double.class) return (F)(Double)object.doubleValue();
        if(targetType == NBTTag.class || targetType == TagInt.class) return (F)new TagInt(object);
        if(targetType == TagByte.class) return (F)new TagByte(object);
        if(targetType == TagShort.class) return (F)new TagShort(object);
        if(targetType == TagFloat.class) return (F)new TagFloat(object);
        if(targetType == TagDouble.class) return (F)new TagDouble(object);
        if(targetType == TagLong.class) return (F)new TagLong(object);
        throw new ClassCastException();
    }

    @Override
    public Object coerce(Integer object, Class targetType, TokenPattern<?> pattern, TridentFile file) {
        if(targetType == Double.class || targetType == double.class) return object.doubleValue();
        if(targetType == NBTTag.class || targetType == TagInt.class) return new TagInt(object);
        if(targetType == TagByte.class) return new TagByte(object);
        if(targetType == TagShort.class) return new TagShort(object);
        if(targetType == TagFloat.class) return new TagFloat(object);
        if(targetType == TagDouble.class) return new TagDouble(object);
        if(targetType == TagLong.class) return new TagLong(object);
        throw new ClassCastException();
    }
}
