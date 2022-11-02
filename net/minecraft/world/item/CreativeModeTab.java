/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public abstract class CreativeModeTab {
    private final int id;
    private final Component displayName;
    private String backgroundSuffix = "items.png";
    private boolean canScroll = true;
    private boolean showTitle = true;
    private ItemStack iconItemStack;
    @Nullable
    private ItemStackLinkedSet displayItems;
    @Nullable
    private ItemStackLinkedSet displayItemsSearchTab;
    @Nullable
    private ItemDisplayParameters cachedParameters;
    private boolean searchTreeDirty;
    @Nullable
    private Consumer<List<ItemStack>> searchTreeRebuilder;

    public CreativeModeTab(int i, Component component) {
        this.id = i;
        this.displayName = component;
        this.iconItemStack = ItemStack.EMPTY;
    }

    public int getId() {
        return this.id;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public ItemStack getIconItem() {
        if (this.iconItemStack.isEmpty()) {
            this.iconItemStack = this.makeIcon();
        }
        return this.iconItemStack;
    }

    public abstract ItemStack makeIcon();

    protected abstract void generateDisplayItems(FeatureFlagSet var1, Output var2, boolean var3);

    public String getBackgroundSuffix() {
        return this.backgroundSuffix;
    }

    public CreativeModeTab setBackgroundSuffix(String string) {
        this.backgroundSuffix = string;
        return this;
    }

    public boolean showTitle() {
        return this.showTitle;
    }

    public CreativeModeTab hideTitle() {
        this.showTitle = false;
        return this;
    }

    public boolean canScroll() {
        return this.canScroll;
    }

    public CreativeModeTab hideScroll() {
        this.canScroll = false;
        return this;
    }

    public int getColumn() {
        return this.id % 6;
    }

    public boolean isTopRow() {
        return this.id < 6;
    }

    public boolean isAlignedRight() {
        return this.getColumn() == 5;
    }

    private ItemStackLinkedSet lazyBuildDisplayItems(FeatureFlagSet featureFlagSet, boolean bl, boolean bl2) {
        boolean bl3;
        ItemDisplayParameters itemDisplayParameters = new ItemDisplayParameters(featureFlagSet, bl2);
        boolean bl4 = bl3 = this.displayItems == null || this.displayItemsSearchTab == null || !Objects.equals(this.cachedParameters, itemDisplayParameters);
        if (bl3) {
            ItemDisplayBuilder itemDisplayBuilder = new ItemDisplayBuilder(this, featureFlagSet);
            this.generateDisplayItems(featureFlagSet, itemDisplayBuilder, bl2);
            this.displayItems = itemDisplayBuilder.getTabContents();
            this.displayItemsSearchTab = itemDisplayBuilder.getSearchTabContents();
            this.cachedParameters = itemDisplayParameters;
        }
        if (this.searchTreeRebuilder != null && (bl3 || this.searchTreeDirty)) {
            this.searchTreeRebuilder.accept(Lists.newArrayList(this.displayItemsSearchTab));
            this.markSearchTreeRebuilt();
        }
        return bl ? this.displayItemsSearchTab : this.displayItems;
    }

    public ItemStackLinkedSet getDisplayItems(FeatureFlagSet featureFlagSet, boolean bl) {
        return this.lazyBuildDisplayItems(featureFlagSet, false, bl);
    }

    public ItemStackLinkedSet getSearchTabDisplayItems(FeatureFlagSet featureFlagSet, boolean bl) {
        return this.lazyBuildDisplayItems(featureFlagSet, true, bl);
    }

    public boolean contains(FeatureFlagSet featureFlagSet, ItemStack itemStack, boolean bl) {
        return this.getSearchTabDisplayItems(featureFlagSet, bl).contains(itemStack);
    }

    public void setSearchTreeRebuilder(Consumer<List<ItemStack>> consumer) {
        this.searchTreeRebuilder = consumer;
    }

    public void invalidateSearchTree() {
        this.searchTreeDirty = true;
    }

    private void markSearchTreeRebuilt() {
        this.searchTreeDirty = false;
    }

    record ItemDisplayParameters(FeatureFlagSet enabledFeatures, boolean hasPermissions) {
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

