package com.energyxxer.trident.compiler.semantics.custom.entities;

import com.energyxxer.commodore.functionlogic.commands.execute.ExecuteAsEntity;
import com.energyxxer.commodore.functionlogic.commands.execute.ExecuteAtEntity;
import com.energyxxer.commodore.functionlogic.commands.execute.ExecuteCommand;
import com.energyxxer.commodore.functionlogic.commands.execute.ExecuteModifier;
import com.energyxxer.commodore.functionlogic.commands.function.FunctionCommand;
import com.energyxxer.commodore.functionlogic.entity.Entity;
import com.energyxxer.commodore.functionlogic.nbt.*;
import com.energyxxer.commodore.functionlogic.selector.Selector;
import com.energyxxer.commodore.functionlogic.selector.arguments.TypeArgument;
import com.energyxxer.commodore.types.Type;
import com.energyxxer.commodore.util.attributes.Attribute;
import com.energyxxer.enxlex.pattern_matching.structures.TokenList;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.pattern_matching.structures.TokenStructure;
import com.energyxxer.nbtmapper.PathContext;
import com.energyxxer.trident.compiler.analyzers.constructs.CommonParsers;
import com.energyxxer.trident.compiler.analyzers.constructs.NBTParser;
import com.energyxxer.trident.compiler.analyzers.constructs.selectors.TypeArgumentParser;
import com.energyxxer.trident.compiler.analyzers.general.AnalyzerManager;
import com.energyxxer.trident.compiler.analyzers.modifiers.ModifierParser;
import com.energyxxer.trident.compiler.analyzers.type_handlers.MemberNotFoundException;
import com.energyxxer.trident.compiler.analyzers.type_handlers.VariableMethod;
import com.energyxxer.trident.compiler.analyzers.type_handlers.VariableTypeHandler;
import com.energyxxer.trident.compiler.semantics.ExceptionCollector;
import com.energyxxer.trident.compiler.semantics.Symbol;
import com.energyxxer.trident.compiler.semantics.TridentException;
import com.energyxxer.trident.compiler.semantics.TridentFile;
import com.energyxxer.trident.compiler.semantics.symbols.ISymbolContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import static com.energyxxer.commodore.functionlogic.selector.Selector.BaseSelector.ALL_ENTITIES;
import static com.energyxxer.commodore.functionlogic.selector.Selector.BaseSelector.SENDER;
import static com.energyxxer.nbtmapper.tags.PathProtocol.ENTITY;

public class CustomEntity implements VariableTypeHandler<CustomEntity> {
    private final String id;
    private final Type defaultType;
    @NotNull
    private TagCompound defaultNBT;
    private String idTag;
    private boolean fullyDeclared = false;

    public CustomEntity(String id, Type defaultType) {
        this.id = id;
        this.defaultType = defaultType;
        this.idTag = "trident-entity." + id.replace(':', '.').replace('/','.');
        this.defaultNBT = getBaseNBT();
    }

    public String getId() {
        return id;
    }

    public Type getDefaultType() {
        return defaultType;
    }

    @NotNull
    public TagCompound getDefaultNBT() {
        return defaultNBT;
    }

    public void mergeNBT(TagCompound newNBT) {
        this.defaultNBT = this.defaultNBT.merge(newNBT);
    }

    public void overrideNBT(TagCompound newNBT) {
        this.defaultNBT = getBaseNBT().merge(newNBT);
    }

    public String getIdTag() {
        return idTag;
    }

    private TagCompound getBaseNBT() {
        return new TagCompound(new TagList("Tags", new TagString(idTag)));
    }

    public boolean isFullyDeclared() {
        return fullyDeclared;
    }

    public void endDeclaration() {
        fullyDeclared = true;
    }


    @Override
    public Object getMember(CustomEntity object, String member, TokenPattern<?> pattern, ISymbolContext ctx, boolean keepSymbol) {
        if(member.equals("getSettingNBT")) {
            return (VariableMethod) (params, patterns, pattern1, file1) -> {
                TagCompound nbt = new TagCompound(new TagString("id", ((CustomEntity) this).getDefaultType().toString()));
                nbt = ((CustomEntity) this).getDefaultNBT().merge(nbt);
                return nbt;
            };
        }
        else if(member.equals("getMatchingNBT")) {
            return (VariableMethod) (params, patterns, pattern1, file1) -> new TagCompound(new TagList("Tags", new TagString(idTag)));
        }
        throw new MemberNotFoundException();
    }

    @Override
    public Object getIndexer(CustomEntity object, Object index, TokenPattern<?> pattern, ISymbolContext file, boolean keepSymbol) {
        throw new MemberNotFoundException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F> F cast(CustomEntity object, Class<F> targetType, TokenPattern<?> pattern, ISymbolContext file) {
        throw new ClassCastException();
    }

    public static void defineEntity(TokenPattern<?> pattern, ISymbolContext ctx) {
        Symbol.SymbolVisibility visibility = CommonParsers.parseVisibility(pattern.find("SYMBOL_VISIBILITY"), ctx, Symbol.SymbolVisibility.GLOBAL);

        String entityName = pattern.find("ENTITY_NAME").flatten(false);
        Type defaultType = null;
        if (pattern.find("ENTITY_BASE.ENTITY_ID_TAGGED") != null) {
            defaultType = CommonParsers.parseEntityType(pattern.find("ENTITY_BASE.ENTITY_ID_TAGGED"), ctx);
        }

        CustomEntity entityDecl = null;
        if (!entityName.equals("default")) {
            if (defaultType == null || !defaultType.isStandalone()) {
                throw new TridentException(TridentException.Source.STRUCTURAL_ERROR, "Cannot create a non-default entity with this type", pattern.find("ENTITY_BASE"), ctx);
            }
            entityDecl = new CustomEntity(entityName, defaultType);
            ctx.getContextForVisibility(visibility).put(new Symbol(entityName, visibility, entityDecl));
        }


        ExceptionCollector collector = new ExceptionCollector(ctx);
        collector.begin();

        try {
            TokenList bodyEntries = (TokenList) pattern.find("ENTITY_DECLARATION_BODY.ENTITY_BODY_ENTRIES");

            if (bodyEntries != null) {
                for (TokenPattern<?> rawEntry : bodyEntries.getContents()) {
                    TokenPattern<?> entry = ((TokenStructure) rawEntry).getContents();
                    switch (entry.getName()) {
                        case "DEFAULT_NBT": {
                            if (entityDecl == null) {
                                collector.log(new TridentException(TridentException.Source.STRUCTURAL_ERROR, "Default NBT isn't allowed for default entities", entry, ctx));
                                break;
                            }

                            TagCompound newNBT = NBTParser.parseCompound(entry.find("NBT_COMPOUND"), ctx);
                            if (newNBT != null) {
                                PathContext context = new PathContext().setIsSetting(true).setProtocol(ENTITY);
                                NBTParser.analyzeTag(newNBT, context, entry.find("NBT_COMPOUND"), ctx);
                            }

                            entityDecl.mergeNBT(newNBT);
                            break;
                        }
                        case "DEFAULT_PASSENGERS": {
                            if (entityDecl == null) {
                                collector.log(new TridentException(TridentException.Source.STRUCTURAL_ERROR, "Default passengers aren't allowed for default entities", entry, ctx));
                                break;
                            }

                            TagList passengersTag = new TagList("Passengers");

                            for (TokenPattern<?> rawPassenger : ((TokenList) entry.find("PASSENGER_LIST")).getContents()) {
                                if (rawPassenger.getName().equals("PASSENGER")) {

                                    TagCompound passengerCompound;

                                    Object reference = CommonParsers.parseEntityReference(rawPassenger.find("ENTITY_ID"), ctx);

                                    if (reference instanceof Type) {
                                        passengerCompound = new TagCompound(new TagString("id", reference.toString()));
                                    } else if (reference instanceof CustomEntity) {
                                        passengerCompound = ((CustomEntity) reference).getDefaultNBT().merge(new TagCompound(new TagString("id", ((CustomEntity) reference).getDefaultType().toString())));
                                    } else {
                                        collector.log(new TridentException(TridentException.Source.IMPOSSIBLE, "Unknown entity reference return type: " + reference.getClass().getSimpleName(), pattern.find("ENTITY_ID"), ctx));
                                        break;
                                    }
                                    TokenPattern<?> auxNBT = rawPassenger.find("PASSENGER_NBT.NBT_COMPOUND");
                                    if (auxNBT != null) {
                                        TagCompound tag = NBTParser.parseCompound(auxNBT, ctx);
                                        PathContext context = new PathContext().setIsSetting(true).setProtocol(ENTITY);
                                        NBTParser.analyzeTag(tag, context, auxNBT, ctx);
                                        passengerCompound = passengerCompound.merge(tag);
                                    }

                                    passengersTag.add(passengerCompound);
                                }
                            }

                            entityDecl.mergeNBT(new TagCompound(passengersTag));
                            break;
                        }
                        case "DEFAULT_HEALTH": {
                            if (entityDecl == null) {
                                collector.log(new TridentException(TridentException.Source.STRUCTURAL_ERROR, "Default health isn't allowed for default entities", entry, ctx));
                                break;
                            }

                            double health = CommonParsers.parseDouble(entry.find("HEALTH"), ctx);
                            if (health < 0) {
                                collector.log(new TridentException(TridentException.Source.COMMAND_ERROR, "Health must be non-negative", entry.find("HEALTH"), ctx));
                                break;
                            }

                            TagCompound healthNBT = new TagCompound();
                            healthNBT.add(new TagFloat("Health", (float) health));
                            healthNBT.add(new TagList("Attributes", new TagCompound(new TagString("Name", Attribute.MAX_HEALTH), new TagDouble("Base", health))));

                            entityDecl.mergeNBT(healthNBT);
                            break;
                        }
                        case "ENTITY_INNER_FUNCTION": {
                            TridentFile innerFile = TridentFile.createInnerFile(entry.find("OPTIONAL_NAME_INNER_FUNCTION"), ctx);

                            TokenPattern<?> functionModifier = entry.find("ENTITY_FUNCTION_MODIFIER");
                            if(functionModifier != null) {
                                functionModifier = ((TokenStructure) functionModifier).getContents();
                                switch(functionModifier.getName()) {
                                    case "TICKING_ENTITY_FUNCTION": {

                                        TokenList rawModifiers = (TokenList) functionModifier.find("TICKING_MODIFIERS");
                                        ArrayList<ExecuteModifier> modifiers = new ArrayList<>();


                                        Entity selector = entityDecl != null ?
                                                TypeArgumentParser.getSelectorForCustomEntity(entityDecl) :
                                                defaultType != null ?
                                                        new Selector(ALL_ENTITIES, new TypeArgument(defaultType)) :
                                                        new Selector(ALL_ENTITIES);

                                        modifiers.add(new ExecuteAsEntity(selector));
                                        modifiers.add(new ExecuteAtEntity(new Selector(SENDER)));


                                        if(rawModifiers != null) {
                                            for(TokenPattern<?> rawModifier : rawModifiers.getContents()) {
                                                ModifierParser parser = AnalyzerManager.getAnalyzer(ModifierParser.class, rawModifier.flattenTokens().get(0).value);
                                                if(parser != null) {
                                                    Collection<ExecuteModifier> modifier = parser.parse(rawModifier, ctx);
                                                    modifiers.addAll(modifier);
                                                } else {
                                                    collector.log(new TridentException(TridentException.Source.IMPOSSIBLE, "Unknown modifier analyzer for '" + rawModifier.flattenTokens().get(0).value + "'", rawModifier, ctx));
                                                }
                                            }
                                        }


                                        ctx.getWritingFile().getTickFunction().append(new ExecuteCommand(new FunctionCommand(innerFile.getFunction()), modifiers));
                                    }
                                }
                            }
                            break;
                        }
                        default: {
                            collector.log(new TridentException(TridentException.Source.IMPOSSIBLE, "Unknown grammar branch name '" + entry.getName() + "'", entry, ctx));
                            break;
                        }
                    }
                }
            }
        } catch(TridentException | TridentException.Grouped x) {
            collector.log(x);
        } finally {
            collector.end();
            if (entityDecl != null) entityDecl.endDeclaration();
        }
    }

    @Override
    public String toString() {
        return "[Custom Entity: " + id + "]";
    }
}
