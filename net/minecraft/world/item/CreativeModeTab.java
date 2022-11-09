/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public class CreativeModeTab {
    private final Component displayName;
    String backgroundSuffix = "items.png";
    boolean canScroll = true;
    boolean showTitle = true;
    boolean alignedRight = false;
    private final Row row;
    private final int column;
    private final Type type;
    @Nullable
    private ItemStack iconItemStack;
    private ItemStackLinkedSet displayItems = new ItemStackLinkedSet();
    private ItemStackLinkedSet displayItemsSearchTab = new ItemStackLinkedSet();
    @Nullable
    private Consumer<List<ItemStack>> searchTreeBuilder;
    private final Supplier<ItemStack> iconGenerator;
    private final DisplayItemsGenerator displayItemsGenerator;

    CreativeModeTab(Row row, int i, Type type, Component component, Supplier<ItemStack> supplier, DisplayItemsGenerator displayItemsGenerator) {
        this.row = row;
        this.column = i;
        this.displayName = component;
        this.iconGenerator = supplier;
        this.displayItemsGenerator = displayItemsGenerator;
        this.type = type;
    }

    public static Builder builder(Row row, int i) {
        return new Builder(row, i);
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public ItemStack getIconItem() {
        if (this.iconItemStack == null) {
            this.iconItemStack = this.iconGenerator.get();
        }
        return this.iconItemStack;
    }

    public String getBackgroundSuffix() {
        return this.backgroundSuffix;
    }

    public boolean showTitle() {
        return this.showTitle;
    }

    public boolean canScroll() {
        return this.canScroll;
    }

    public int column() {
        return this.column;
    }

    public Row row() {
        return this.row;
    }

    public boolean hasAnyItems() {
        return !this.displayItems.isEmpty();
    }

    public boolean shouldDisplay() {
        return this.type != Type.CATEGORY || this.hasAnyItems();
    }

    public boolean isAlignedRight() {
        return this.alignedRight;
    }

    public Type getType() {
        return this.type;
    }

    public void buildContents(FeatureFlagSet featureFlagSet, boolean bl) {
        ItemDisplayBuilder itemDisplayBuilder = new ItemDisplayBuilder(this, featureFlagSet);
        this.displayItemsGenerator.accept(featureFlagSet, itemDisplayBuilder, bl);
        this.displayItems = itemDisplayBuilder.getTabContents();
        this.displayItemsSearchTab = itemDisplayBuilder.getSearchTabContents();
        this.rebuildSearchTree();
    }

    public ItemStackLinkedSet getDisplayItems() {
        return this.displayItems;
    }

    public ItemStackLinkedSet getSearchTabDisplayItems() {
        return this.displayItemsSearchTab;
    }

    public boolean contains(ItemStack itemStack) {
        return this.displayItemsSearchTab.contains(itemStack);
    }

    public void setSearchTreeBuilder(Consumer<List<ItemStack>> consumer) {
        this.searchTreeBuilder = consumer;
    }

    public void rebuildSearchTree() {
        if (this.searchTreeBuilder != null) {
            this.searchTreeBuilder.accept(Lists.newArrayList(this.displayItemsSearchTab));
        }
    }

    public static enum Row {
        TOP,
        BOTTOM;

    }

    public static interface DisplayItemsGenerator {
        public void accept(FeatureFlagSet var1, Output var2, boolean var3);
    }

    public static enum Type {
        CATEGORY,
        INVENTORY,
        HOTBAR,
        SEARCH;

    }

    public static class Builder {
        private static final DisplayItemsGenerator EMPTY_GENERATOR = (featureFlagSet, output, bl) -> {};
        private final Row row;
        private final int column;
        private Component displayName = Component.empty();
        private Supplier<ItemStack> iconGenerator = () -> ItemStack.EMPTY;
        private DisplayItemsGenerator displayItemsGenerator = EMPTY_GENERATOR;
        private boolean canScroll = true;
        private boolean showTitle = true;
        private boolean alignedRight = false;
        private Type type = Type.CATEGORY;
        private String backgroundSuffix = "items.png";

        public Builder(Row row, int i) {
            this.row = row;
            this.column = i;
        }

        public Builder title(Component component) {
            this.displayName = component;
            return this;
        }

        public Builder icon(Supplier<ItemStack> supplier) {
            this.iconGenerator = supplier;
            return this;
        }

        public Builder displayItems(DisplayItemsGenerator displayItemsGenerator) {
            this.displayItemsGenerator = displayItemsGenerator;
            return this;
        }

        public Builder alignedRight() {
            this.alignedRight = true;
            return this;
        }

        public Builder hideTitle() {
            this.showTitle = false;
            return this;
        }

        public Builder noScrollBar() {
            this.canScroll = false;
            return this;
        }

        protected Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder backgroundSuffix(String string) {
            this.backgroundSuffix = string;
            return this;
        }

        public CreativeModeTab build() {
            if ((this.type == Type.HOTBAR || this.type == Type.INVENTORY) && this.displayItemsGenerator != EMPTY_GENERATOR) {
                throw new IllegalStateException("Special tabs can't have display items");
            }
            CreativeModeTab creativeModeTab = new CreativeModeTab(this.row, this.column, this.type, this.displayName, this.iconGenerator, this.displayItemsGenerator);
            creativeModeTab.alignedRight = this.alignedRight;
            creativeModeTab.showTitle = this.showTitle;
            creativeModeTab.canScroll = this.canScroll;
            creativeModeTab.backgroundSuffix = this.backgroundSuffix;
            return creativeModeTab;
        }
    }

    static class ItemDisplayBuilder
    implements Output {
        private final ItemStackLinkedSet tabContents = new ItemStackLinkedSet();
        private final ItemStackLinkedSet searchTabContents = new ItemStackLinkedSet();
        private final CreativeModeTab tab;
        private final FeatureFlagSet featureFlagSet;

        public ItemDisplayBuilder(CreativeModeTab creativeModeTab, FeatureFlagSet featureFlagSet) {
            this.tab = creativeModeTab;
            this.featureFlagSet = featureFlagSet;
        }

        @Override
        public void accept(ItemStack itemStack, TabVisibility tabVisibility) {
            boolean bl;
            boolean bl2 = bl = this.tabContents.contains(itemStack) && tabVisibility != TabVisibility.SEARCH_TAB_ONLY;
            if (bl) {
                throw new IllegalStateException("Accidentally adding the same item stack twice " + itemStack.getDisplayName().getString() + " to a Creative Mode Tab: " + this.tab.getDisplayName().getString());
            }
            if (itemStack.getItem().isEnabled(this.featureFlagSet)) {
                switch (tabVisibility) {
                    case PARENT_AND_SEARCH_TABS: {
                        this.tabContents.add(itemStack);
                        this.searchTabContents.add(itemStack);
                        break;
                    }
                    case PARENT_TAB_ONLY: {
                        this.tabContents.add(itemStack);
                        break;
                    }
                    case SEARCH_TAB_ONLY: {
                        this.searchTabContents.add(itemStack);
                    }
                }
            }
        }

        public ItemStackLinkedSet getTabContents() {
            return this.tabContents;
        }

        public ItemStackLinkedSet getSearchTabContents() {
            return this.searchTabContents;
        }
    }

    protected static interface Output {
        public void accept(ItemStack var1, TabVisibility var2);

        default public void accept(ItemStack itemStack) {
            this.accept(itemStack, TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        default public void accept(ItemLike itemLike, TabVisibility tabVisibility) {
            this.accept(new ItemStack(itemLike), tabVisibility);
        }

        default public void accept(ItemLike itemLike) {
            this.accept(new ItemStack(itemLike), TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        default public void acceptAll(Collection<ItemStack> collection, TabVisibility tabVisibility) {
            collection.forEach(itemStack -> this.accept((ItemStack)itemStack, tabVisibility));
        }

        default public void acceptAll(Collection<ItemStack> collection) {
            this.acceptAll(collection, TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    protected static enum TabVisibility {
        PARENT_AND_SEARCH_TABS,
        PARENT_TAB_ONLY,
        SEARCH_TAB_ONLY;

    }
}

