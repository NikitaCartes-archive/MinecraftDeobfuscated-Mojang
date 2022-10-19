package net.minecraft.world.item;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ItemLike;

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

	protected abstract void generateDisplayItems(FeatureFlagSet featureFlagSet, CreativeModeTab.Output output);

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

	private ItemStackLinkedSet lazyBuildDisplayItems(FeatureFlagSet featureFlagSet, boolean bl) {
		if (this.displayItems == null || this.displayItemsSearchTab == null) {
			CreativeModeTab.ItemDisplayBuilder itemDisplayBuilder = new CreativeModeTab.ItemDisplayBuilder(this, featureFlagSet);
			this.generateDisplayItems(featureFlagSet, itemDisplayBuilder);
			this.displayItems = itemDisplayBuilder.getTabContents();
			this.displayItemsSearchTab = itemDisplayBuilder.getSearchTabContents();
		}

		return bl ? this.displayItemsSearchTab : this.displayItems;
	}

	public ItemStackLinkedSet getDisplayItems(FeatureFlagSet featureFlagSet) {
		return this.lazyBuildDisplayItems(featureFlagSet, false);
	}

	public ItemStackLinkedSet getSearchTabDisplayItems(FeatureFlagSet featureFlagSet) {
		return this.lazyBuildDisplayItems(featureFlagSet, true);
	}

	public boolean contains(FeatureFlagSet featureFlagSet, ItemStack itemStack) {
		return this.getSearchTabDisplayItems(featureFlagSet).contains(itemStack);
	}

	public void invalidateDisplayListCache() {
		this.displayItems = null;
		this.displayItemsSearchTab = null;
	}

	static class ItemDisplayBuilder implements CreativeModeTab.Output {
		private final ItemStackLinkedSet tabContents = new ItemStackLinkedSet();
		private final ItemStackLinkedSet searchTabContents = new ItemStackLinkedSet();
		private final CreativeModeTab tab;
		private final FeatureFlagSet featureFlagSet;

		public ItemDisplayBuilder(CreativeModeTab creativeModeTab, FeatureFlagSet featureFlagSet) {
			this.tab = creativeModeTab;
			this.featureFlagSet = featureFlagSet;
		}

		@Override
		public void accept(ItemStack itemStack, CreativeModeTab.TabVisibility tabVisibility) {
			boolean bl = this.tabContents.contains(itemStack) && tabVisibility != CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY;
			if (bl) {
				throw new IllegalStateException(
					"Accidentally adding the same item stack twice "
						+ itemStack.getDisplayName().getString()
						+ " to a Creative Mode Tab: "
						+ this.tab.getDisplayName().getString()
				);
			} else {
				if (itemStack.getItem().isEnabled(this.featureFlagSet)) {
					switch (tabVisibility) {
						case PARENT_AND_SEARCH_TABS:
							this.tabContents.add(itemStack);
							this.searchTabContents.add(itemStack);
							break;
						case PARENT_TAB_ONLY:
							this.tabContents.add(itemStack);
							break;
						case SEARCH_TAB_ONLY:
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

	protected interface Output {
		void accept(ItemStack itemStack, CreativeModeTab.TabVisibility tabVisibility);

		default void accept(ItemStack itemStack) {
			this.accept(itemStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}

		default void accept(ItemLike itemLike, CreativeModeTab.TabVisibility tabVisibility) {
			this.accept(new ItemStack(itemLike), tabVisibility);
		}

		default void accept(ItemLike itemLike) {
			this.accept(new ItemStack(itemLike), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}

		default void acceptAll(Collection<ItemStack> collection, CreativeModeTab.TabVisibility tabVisibility) {
			collection.forEach(itemStack -> this.accept(itemStack, tabVisibility));
		}

		default void acceptAll(Collection<ItemStack> collection) {
			this.acceptAll(collection, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}
	}

	protected static enum TabVisibility {
		PARENT_AND_SEARCH_TABS,
		PARENT_TAB_ONLY,
		SEARCH_TAB_ONLY;
	}
}
