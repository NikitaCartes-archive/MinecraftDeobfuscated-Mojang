package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ItemLike;

public class CreativeModeTab {
	private final Component displayName;
	String backgroundSuffix = "items.png";
	boolean canScroll = true;
	boolean showTitle = true;
	boolean alignedRight = false;
	private final CreativeModeTab.Row row;
	private final int column;
	private final CreativeModeTab.Type type;
	@Nullable
	private ItemStack iconItemStack;
	private Collection<ItemStack> displayItems = ItemStackLinkedSet.createTypeAndComponentsSet();
	private Set<ItemStack> displayItemsSearchTab = ItemStackLinkedSet.createTypeAndComponentsSet();
	@Nullable
	private Consumer<List<ItemStack>> searchTreeBuilder;
	private final Supplier<ItemStack> iconGenerator;
	private final CreativeModeTab.DisplayItemsGenerator displayItemsGenerator;

	CreativeModeTab(
		CreativeModeTab.Row row,
		int i,
		CreativeModeTab.Type type,
		Component component,
		Supplier<ItemStack> supplier,
		CreativeModeTab.DisplayItemsGenerator displayItemsGenerator
	) {
		this.row = row;
		this.column = i;
		this.displayName = component;
		this.iconGenerator = supplier;
		this.displayItemsGenerator = displayItemsGenerator;
		this.type = type;
	}

	public static CreativeModeTab.Builder builder(CreativeModeTab.Row row, int i) {
		return new CreativeModeTab.Builder(row, i);
	}

	public Component getDisplayName() {
		return this.displayName;
	}

	public ItemStack getIconItem() {
		if (this.iconItemStack == null) {
			this.iconItemStack = (ItemStack)this.iconGenerator.get();
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

	public CreativeModeTab.Row row() {
		return this.row;
	}

	public boolean hasAnyItems() {
		return !this.displayItems.isEmpty();
	}

	public boolean shouldDisplay() {
		return this.type != CreativeModeTab.Type.CATEGORY || this.hasAnyItems();
	}

	public boolean isAlignedRight() {
		return this.alignedRight;
	}

	public CreativeModeTab.Type getType() {
		return this.type;
	}

	public void buildContents(CreativeModeTab.ItemDisplayParameters itemDisplayParameters) {
		CreativeModeTab.ItemDisplayBuilder itemDisplayBuilder = new CreativeModeTab.ItemDisplayBuilder(this, itemDisplayParameters.enabledFeatures);
		ResourceKey<CreativeModeTab> resourceKey = (ResourceKey<CreativeModeTab>)BuiltInRegistries.CREATIVE_MODE_TAB
			.getResourceKey(this)
			.orElseThrow(() -> new IllegalStateException("Unregistered creative tab: " + this));
		this.displayItemsGenerator.accept(itemDisplayParameters, itemDisplayBuilder);
		this.displayItems = itemDisplayBuilder.tabContents;
		this.displayItemsSearchTab = itemDisplayBuilder.searchTabContents;
		this.rebuildSearchTree();
	}

	public Collection<ItemStack> getDisplayItems() {
		return this.displayItems;
	}

	public Collection<ItemStack> getSearchTabDisplayItems() {
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

	public static class Builder {
		private static final CreativeModeTab.DisplayItemsGenerator EMPTY_GENERATOR = (itemDisplayParameters, output) -> {
		};
		private final CreativeModeTab.Row row;
		private final int column;
		private Component displayName = Component.empty();
		private Supplier<ItemStack> iconGenerator = () -> ItemStack.EMPTY;
		private CreativeModeTab.DisplayItemsGenerator displayItemsGenerator = EMPTY_GENERATOR;
		private boolean canScroll = true;
		private boolean showTitle = true;
		private boolean alignedRight = false;
		private CreativeModeTab.Type type = CreativeModeTab.Type.CATEGORY;
		private String backgroundSuffix = "items.png";

		public Builder(CreativeModeTab.Row row, int i) {
			this.row = row;
			this.column = i;
		}

		public CreativeModeTab.Builder title(Component component) {
			this.displayName = component;
			return this;
		}

		public CreativeModeTab.Builder icon(Supplier<ItemStack> supplier) {
			this.iconGenerator = supplier;
			return this;
		}

		public CreativeModeTab.Builder displayItems(CreativeModeTab.DisplayItemsGenerator displayItemsGenerator) {
			this.displayItemsGenerator = displayItemsGenerator;
			return this;
		}

		public CreativeModeTab.Builder alignedRight() {
			this.alignedRight = true;
			return this;
		}

		public CreativeModeTab.Builder hideTitle() {
			this.showTitle = false;
			return this;
		}

		public CreativeModeTab.Builder noScrollBar() {
			this.canScroll = false;
			return this;
		}

		protected CreativeModeTab.Builder type(CreativeModeTab.Type type) {
			this.type = type;
			return this;
		}

		public CreativeModeTab.Builder backgroundSuffix(String string) {
			this.backgroundSuffix = string;
			return this;
		}

		public CreativeModeTab build() {
			if ((this.type == CreativeModeTab.Type.HOTBAR || this.type == CreativeModeTab.Type.INVENTORY) && this.displayItemsGenerator != EMPTY_GENERATOR) {
				throw new IllegalStateException("Special tabs can't have display items");
			} else {
				CreativeModeTab creativeModeTab = new CreativeModeTab(this.row, this.column, this.type, this.displayName, this.iconGenerator, this.displayItemsGenerator);
				creativeModeTab.alignedRight = this.alignedRight;
				creativeModeTab.showTitle = this.showTitle;
				creativeModeTab.canScroll = this.canScroll;
				creativeModeTab.backgroundSuffix = this.backgroundSuffix;
				return creativeModeTab;
			}
		}
	}

	@FunctionalInterface
	public interface DisplayItemsGenerator {
		void accept(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output);
	}

	static class ItemDisplayBuilder implements CreativeModeTab.Output {
		public final Collection<ItemStack> tabContents = ItemStackLinkedSet.createTypeAndComponentsSet();
		public final Set<ItemStack> searchTabContents = ItemStackLinkedSet.createTypeAndComponentsSet();
		private final CreativeModeTab tab;
		private final FeatureFlagSet featureFlagSet;

		public ItemDisplayBuilder(CreativeModeTab creativeModeTab, FeatureFlagSet featureFlagSet) {
			this.tab = creativeModeTab;
			this.featureFlagSet = featureFlagSet;
		}

		@Override
		public void accept(ItemStack itemStack, CreativeModeTab.TabVisibility tabVisibility) {
			if (itemStack.getCount() != 1) {
				throw new IllegalArgumentException("Stack size must be exactly 1");
			} else {
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
		}
	}

	public static record ItemDisplayParameters(FeatureFlagSet enabledFeatures, boolean hasPermissions, HolderLookup.Provider holders) {

		public boolean needsUpdate(FeatureFlagSet featureFlagSet, boolean bl, HolderLookup.Provider provider) {
			return !this.enabledFeatures.equals(featureFlagSet) || this.hasPermissions != bl || this.holders != provider;
		}
	}

	public interface Output {
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

	public static enum Row {
		TOP,
		BOTTOM;
	}

	protected static enum TabVisibility {
		PARENT_AND_SEARCH_TABS,
		PARENT_TAB_ONLY,
		SEARCH_TAB_ONLY;
	}

	public static enum Type {
		CATEGORY,
		INVENTORY,
		HOTBAR,
		SEARCH;
	}
}
