package com.energyxxer.trident.compiler.analyzers.type_handlers.extensions.tags;

import com.energyxxer.commodore.functionlogic.nbt.NumericNBTTag;
import com.energyxxer.commodore.functionlogic.nbt.TagDouble;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.trident.compiler.analyzers.type_handlers.MemberNotFoundException;
import com.energyxxer.trident.compiler.analyzers.type_handlers.MethodWrapper;
import com.energyxxer.trident.compiler.analyzers.type_handlers.TridentTypeManager;
import com.energyxxer.trident.compiler.analyzers.type_handlers.TridentMethod;
import com.energyxxer.trident.compiler.analyzers.type_handlers.extensions.TypeHandler;
import com.energyxxer.trident.compiler.semantics.symbols.ISymbolContext;

public class TagDoubleTypeHandler implements TypeHandler<TagDouble> {
    private static final TridentMethod CONSTRUCTOR = new MethodWrapper<>(
            "new tag_double",
            ((instance, params) -> new TagDouble(params[0] == null ? 0 : (int) params[0])),
            Double.class
    ).setNullable(0).createForInstance(null);

    @Override
    public Object getMember(TagDouble object, String member, TokenPattern<?> pattern, ISymbolContext ctx, boolean keepSymbol) {
        throw new MemberNotFoundException();
    }

    @Override
    public Object getIndexer(TagDouble object, Object index, TokenPattern<?> pattern, ISymbolContext ctx, boolean keepSymbol) {
        throw new MemberNotFoundException();
    }

    @Override
    public <F> F cast(TagDouble object, Class<F> targetType, TokenPattern<?> pattern, ISymbolContext ctx) {
        if(object != null && Number.class.isAssignableFrom(targetType)) {
            Number number = ((NumericNBTTag) object).getValue();
            if(targetType == Integer.class || targetType == int.class) return (F) (Integer)number.intValue();
            if(targetType == Double.class || targetType == double.class) return (F) (Double)number.doubleValue();
        }
        throw new ClassCastException();
    }

    @Override
    public Class<TagDouble> getHandledClass() {
        return TagDouble.class;
    }

    @Override
    public String getTypeIdentifier() {
        return "tag_double";
    }

    @Override
    public TypeHandler<?> getSuperType() {
        return TridentTypeManager.getHandlerForHandlerClass(NBTTagTypeHandler.class);
    }

    @Override
    public TridentMethod getConstructor(TokenPattern<?> pattern, ISymbolContext ctx) {
        return CONSTRUCTOR;
    }
}
