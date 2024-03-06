package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

public final class BundleContents implements TooltipComponent {
	public static final int MAX_WEIGHT = 64;
	public static final BundleContents EMPTY = new BundleContents(List.of());
	public static final Codec<BundleContents> CODEC = ExtraCodecs.sizeLimitedList(ItemStack.CODEC.listOf(), 64)
		.xmap(BundleContents::new, bundleContents -> bundleContents.items);
	public static final StreamCodec<RegistryFriendlyByteBuf, BundleContents> STREAM_CODEC = ItemStack.STREAM_CODEC
		.apply(ByteBufCodecs.list(64))
		.map(BundleContents::new, bundleContents -> bundleContents.items);
	private static final int BUNDLE_IN_BUNDLE_WEIGHT = 4;
	private static final int NO_STACK_INDEX = -1;
	final List<ItemStack> items;
	final int weight;

	BundleContents(List<ItemStack> list, int i) {
		this.items = list;
		this.weight = i;
	}

	public BundleContents(List<ItemStack> list) {
		this(list, computeContentWeight(list));
	}

	private static int computeContentWeight(List<ItemStack> list) {
		int i = 0;

		for (ItemStack itemStack : list) {
			i += getWeight(itemStack) * itemStack.getCount();
		}

		return i;
	}

	static int getWeight(ItemStack itemStack) {
		BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
		if (bundleContents != null) {
			return 4 + bundleContents.weight();
		} else {
			List<BeehiveBlockEntity.Occupant> list = itemStack.getOrDefault(DataComponents.BEES, List.of());
			return !list.isEmpty() ? 64 : 64 / itemStack.getMaxStackSize();
		}
	}

	public ItemStack getItemUnsafe(int i) {
		return (ItemStack)this.items.get(i);
	}

	public Stream<ItemStack> items() {
		return this.items.stream().map(ItemStack::copy);
	}

	public int size() {
		return this.items.size();
	}

	public int weight() {
		return this.weight;
	}

	public boolean isEmpty() {
		return this.items.isEmpty();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof BundleContents bundleContents)
				? false
				: this.weight == bundleContents.weight && ItemStack.listMatches(this.items, bundleContents.items);
		}
	}

	public int hashCode() {
		return ItemStack.hashStackList(this.items);
	}

	public String toString() {
		return "BundleContents" + this.items;
	}

	public static class Mutable {
		private final List<ItemStack> items;
		private int weight;

		public Mutable(BundleContents bundleContents) {
			this.items = new ArrayList(bundleContents.items);
			this.weight = bundleContents.weight;
		}

		private int findStackIndex(ItemStack itemStack) {
			if (!itemStack.isStackable()) {
				return -1;
			} else {
				for (int i = 0; i < this.items.size(); i++) {
					if (ItemStack.isSameItemSameComponents((ItemStack)this.items.get(i), itemStack)) {
						return i;
					}
				}

				return -1;
			}
		}

		private int getMaxAmountToAdd(ItemStack itemStack) {
			return Math.max(64 - this.weight, 0) / BundleContents.getWeight(itemStack);
		}

		public int tryInsert(ItemStack itemStack) {
			if (!itemStack.isEmpty() && itemStack.getItem().canFitInsideContainerItems()) {
				int i = Math.min(itemStack.getCount(), this.getMaxAmountToAdd(itemStack));
				if (i == 0) {
					return 0;
				} else {
					this.weight = this.weight + BundleContents.getWeight(itemStack) * i;
					int j = this.findStackIndex(itemStack);
					if (j != -1) {
						ItemStack itemStack2 = (ItemStack)this.items.remove(j);
						ItemStack itemStack3 = itemStack2.copyWithCount(itemStack2.getCount() + i);
						itemStack.shrink(i);
						this.items.add(0, itemStack3);
					} else {
						this.items.add(0, itemStack.split(i));
					}

					return i;
				}
			} else {
				return 0;
			}
		}

		public int tryTransfer(Slot slot, Player player) {
			ItemStack itemStack = slot.getItem();
			int i = this.getMaxAmountToAdd(itemStack);
			return this.tryInsert(slot.safeTake(itemStack.getCount(), i, player));
		}

		@Nullable
		public ItemStack removeOne() {
			if (this.items.isEmpty()) {
				return null;
			} else {
				ItemStack itemStack = ((ItemStack)this.items.remove(0)).copy();
				this.weight = this.weight - BundleContents.getWeight(itemStack) * itemStack.getCount();
				return itemStack;
			}
		}

		public int weight() {
			return this.weight;
		}

		public BundleContents toImmutable() {
			return new BundleContents(List.copyOf(this.items), this.weight);
		}
	}
}
