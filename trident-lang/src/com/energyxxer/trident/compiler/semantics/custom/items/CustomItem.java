package com.energyxxer.trident.compiler.semantics.custom.items;

import com.energyxxer.commodore.functionlogic.nbt.*;
import com.energyxxer.commodore.functionlogic.nbt.path.NBTPath;
import com.energyxxer.commodore.item.Item;
import com.energyxxer.commodore.types.Type;
import com.energyxxer.commodore.types.defaults.FunctionReference;
import com.energyxxer.enxlex.pattern_matching.structures.TokenList;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.pattern_matching.structures.TokenStructure;
import com.energyxxer.nbtmapper.PathContext;
import com.energyxxer.trident.compiler.analyzers.constructs.CommonParsers;
import com.energyxxer.trident.compiler.analyzers.constructs.NBTParser;
import com.energyxxer.trident.compiler.analyzers.constructs.TextParser;
import com.energyxxer.trident.compiler.analyzers.type_handlers.MemberNotFoundException;
import com.energyxxer.trident.compiler.analyzers.type_handlers.VariableMethod;
import com.energyxxer.trident.compiler.analyzers.type_handlers.VariableTypeHandler;
import com.energyxxer.trident.compiler.semantics.*;
import com.energyxxer.trident.compiler.semantics.custom.special.item_events.ItemEvent;

import static com.energyxxer.nbtmapper.tags.PathProtocol.DEFAULT;
import static com.energyxxer.trident.compiler.semantics.custom.items.NBTMode.SETTING;

public class CustomItem implements VariableTypeHandler<CustomItem> {
    private final String id;
    private final Type defaultType;
    private TagCompound defaultNBT;
    private boolean useModelData = false;
    private int customModelData = 0;
    private boolean fullyDeclared = false;

    public CustomItem(String id, Type defaultType) {
        this.id = id;
        this.defaultType = defaultType;
        this.defaultNBT = new TagCompound(new TagInt("TridentCustomItem", getItemIdHash()));
    }

    public String getId() {
        return id;
    }

    public Type getDefaultType() {
        return defaultType;
    }

    public TagCompound getDefaultNBT() {
        return defaultNBT;
    }

    public void setDefaultNBT(TagCompound defaultNBT) {
        this.defaultNBT = defaultNBT;
    }

    public boolean isUseModelData() {
        return useModelData;
    }

    public void setUseModelData(boolean useModelData) {
        this.useModelData = useModelData;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
        defaultNBT = defaultNBT.merge(new TagCompound(new TagInt("CustomModelData", customModelData)));
        setUseModelData(true);
    }

    public boolean isFullyDeclared() {
        return fullyDeclared;
    }

    public void endDeclaration() {
        fullyDeclared = true;
    }

    public Item constructItem(NBTMode mode) {
        return mode == SETTING ? new Item(defaultType, getDefaultNBT()) : new Item(defaultType, new TagCompound(new TagInt("TridentCustomItem", getItemIdHash())));
    }







    @Override
    public Object getMember(CustomItem object, String member, TokenPattern<?> pattern, TridentFile file, boolean keepSymbol) {
        if(member.equals("getSettingNBT")) {
            return (VariableMethod) (params, patterns, pattern1, file1) -> {
                TagCompound nbt = new TagCompound(
                        new TagString("id", ((CustomItem) this).getDefaultType().toString()),
                        new TagByte("Count", 1));
                if(((CustomItem) this).getDefaultNBT() != null) {
                    TagCompound tag = ((CustomItem) this).getDefaultNBT().clone();
                    tag.setName("tag");
                    nbt = new TagCompound(tag).merge(nbt);
                }
                return nbt;
            };
        }
        else if(member.equals("getMatchingNBT")) {
            return (VariableMethod) (params, patterns, pattern1, file1) -> new TagCompound(new TagInt("TridentCustomItem", getItemIdHash()));
        } else if(member.equals("getItem")) {
            return (VariableMethod) (params, patterns, pattern1, file1) -> new Item(defaultType, defaultNBT);
        }
        throw new MemberNotFoundException();
    }

    @Override
    public Object getIndexer(CustomItem object, Object index, TokenPattern<?> pattern, TridentFile file, boolean keepSymbol) {
        throw new MemberNotFoundException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F> F cast(CustomItem object, Class<F> targetType, TokenPattern<?> pattern, TridentFile file) {
        throw new ClassCastException();
    }

    @Override
    public String toString() {
        return "[Custom Item: " + id + "]";
    }


















    public static void defineItem(TokenPattern<?> pattern, TridentFile file) {
        Symbol.SymbolVisibility visibility = CommonParsers.parseVisibility(pattern.find("SYMBOL_VISIBILITY"), file, Symbol.SymbolVisibility.GLOBAL);

        String entityName = pattern.find("ITEM_NAME").flatten(false);
        Type defaultType = CommonParsers.parseItemType(pattern.find("ITEM_ID"), file);

        CustomItem itemDecl = null;
        TokenPattern<?> rawCustomModelData = pattern.find("CUSTOM_MODEL_DATA.INTEGER");

        if(!entityName.equals("default")) {
            itemDecl = new CustomItem(entityName, defaultType);
            if(rawCustomModelData != null) itemDecl.setCustomModelData(CommonParsers.parseInt(rawCustomModelData, file));

            SymbolTable table = file.getCompiler().getSymbolStack().getTableForVisibility(visibility);
            table.put(new Symbol(entityName, visibility, itemDecl));
        } else if(rawCustomModelData != null) {
            throw new TridentException(TridentException.Source.STRUCTURAL_ERROR, "Default items don't support custom model data specifiers", rawCustomModelData, file);
        }



        ExceptionCollector collector = new ExceptionCollector(file);
        collector.begin();

        try {
            TokenList bodyEntries = (TokenList) pattern.find("ITEM_DECLARATION_BODY.ITEM_BODY_ENTRIES");

            if (bodyEntries != null) {
                for (TokenPattern<?> rawEntry : bodyEntries.getContents()) {
                    TokenPattern<?> entry = ((TokenStructure) rawEntry).getContents();
                    switch (entry.getName()) {
                        case "DEFAULT_NBT": {
                            if (itemDecl == null) {
                                collector.log(new TridentException(TridentException.Source.STRUCTURAL_ERROR, "Default NBT isn't allowed for default items", entry, file));
                                break;
                            }
                            TagCompound newNBT = NBTParser.parseCompound(entry.find("NBT_COMPOUND"), file);
                            PathContext context = new PathContext().setIsSetting(true).setProtocol(DEFAULT, "ITEM_TAG");
                            NBTParser.analyzeTag(newNBT, context, entry.find("NBT_COMPOUND"), file);
                            itemDecl.defaultNBT = itemDecl.defaultNBT.merge(newNBT);
                            break;
                        }
                        case "ITEM_INNER_FUNCTION": {
                            TridentFile innerFile = TridentFile.createInnerFile(entry.find("OPTIONAL_NAME_INNER_FUNCTION"), file);

                            TokenPattern<?> rawFunctionModifiers = entry.find("INNER_FUNCTION_MODIFIERS");
                            if (rawFunctionModifiers != null) {
                                TokenPattern<?> modifiers = ((TokenStructure) rawFunctionModifiers).getContents();
                                switch (modifiers.getName()) {
                                    case "FUNCTION_ON": {

                                        TokenPattern<?> onWhat = ((TokenStructure) modifiers.find("FUNCTION_ON_INNER")).getContents();

                                        boolean pure = false;
                                        if (modifiers.find("LITERAL_PURE") != null) {
                                            if (itemDecl != null) {
                                                collector.log(new TridentException(TridentException.Source.STRUCTURAL_ERROR, "The 'pure' modifier is only allowed for default items", modifiers.find("LITERAL_PURE"), file));
                                            } else {
                                                pure = true;
                                            }
                                        }

                                        if (onWhat.getName().equals("ITEM_CRITERIA")) {
                                            if (itemDecl != null && file.getLanguageLevel() < 2) {
                                                collector.log(new TridentException(TridentException.Source.STRUCTURAL_ERROR, "Custom non-default item events are only supported in language level 2 and up", entry, file));
                                                break;
                                            }

                                            file.getCompiler().getSpecialFileManager().itemEvents.addCustomItem(ItemEvent.ItemScoreEventType.valueOf(onWhat.find("ITEM_CRITERIA_KEY").flatten(false).toUpperCase()), defaultType, itemDecl, new ItemEvent(new FunctionReference(innerFile.getFunction()), pure));

                                        }
                                    }
                                }
                            }

                            break;
                        }
                        case "DEFAULT_NAME": {
                            if (itemDecl == null) {
                                collector.log(new TridentException(TridentException.Source.STRUCTURAL_ERROR, "Default NBT isn't allowed for default items", entry, file));
                                break;
                            }

                            NBTCompoundBuilder builder = new NBTCompoundBuilder();
                            builder.put(new NBTPath("display", new NBTPath("Name")), new TagString("Name", TextParser.parseTextComponent(entry.find("TEXT_COMPONENT"), file).toString()));

                            itemDecl.defaultNBT = itemDecl.defaultNBT.merge(builder.getCompound());
                            break;
                        }
                        case "DEFAULT_LORE": {
                            if (itemDecl == null) {
                                collector.log(new TridentException(TridentException.Source.STRUCTURAL_ERROR, "Default NBT isn't allowed for default items", entry, file));
                                break;
                            }
                            TagList loreList = new TagList("Lore");
                            TagCompound newNBT = new TagCompound("", new TagCompound("display", loreList));

                            TokenList rawLoreList = (TokenList) (entry.find("LORE_LIST"));
                            if (rawLoreList != null) {
                                for (TokenPattern<?> rawLine : rawLoreList.getContents()) {
                                    if (rawLine.getName().equals("TEXT_COMPONENT"))
                                        loreList.add(new TagString(TextParser.parseTextComponent(rawLine, file).toString()));
                                }
                            }

                            itemDecl.defaultNBT = itemDecl.defaultNBT.merge(newNBT);
                            break;
                        }
                        default: {
                            collector.log(new TridentException(TridentException.Source.IMPOSSIBLE, "Unknown grammar branch name '" + entry.getName() + "'", entry, file));
                            break;
                        }
                    }
                }
            }
        } catch(TridentException | TridentException.Grouped x) {
            collector.log(x);
        } finally {
            collector.end();
            if(itemDecl != null) itemDecl.endDeclaration();
        }
    }

    public int getItemIdHash() {
        return id.hashCode();
    }
}
