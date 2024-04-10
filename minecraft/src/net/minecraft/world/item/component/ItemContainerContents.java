package net.minecraft.world.item.component;

import com.google.common.collect.Iterables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public final class ItemContainerContents {
	private static final int NO_SLOT = -1;
	private static final int MAX_SIZE = 256;
	public static final ItemContainerContents EMPTY = new ItemContainerContents(NonNullList.create());
	public static final Codec<ItemContainerContents> CODEC = ItemContainerContents.Slot.CODEC
		.sizeLimitedListOf(256)
		.xmap(ItemContainerContents::fromSlots, ItemContainerContents::asSlots);
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemContainerContents> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC
		.apply(ByteBufCodecs.list(256))
		.map(ItemContainerContents::new, itemContainerContents -> itemContainerContents.items);
	private final NonNullList<ItemStack> items;

	private ItemContainerContents(NonNullList<ItemStack> nonNullList) {
		if (nonNullList.size() > 256) {
			throw new IllegalArgumentException("Got " + nonNullList.size() + " items, but maximum is 256");
		} else {
			this.items = nonNullList;
		}
	}

	private ItemContainerContents(int i) {
		this(NonNullList.withSize(i, ItemStack.EMPTY));
	}

	private ItemContainerContents(List<ItemStack> list) {
		this(list.size());

		for (int i = 0; i < list.size(); i++) {
			this.items.set(i, (ItemStack)list.get(i));
		}
	}

	private static ItemContainerContents fromSlots(List<ItemContainerContents.Slot> list) {
		OptionalInt optionalInt = list.stream().mapToInt(ItemContainerContents.Slot::index).max();
		if (optionalInt.isEmpty()) {
			return EMPTY;
		} else {
			ItemContainerContents itemContainerContents = new ItemContainerContents(optionalInt.getAsInt() + 1);

			for (ItemContainerContents.Slot slot : list) {
				itemContainerContents.items.set(slot.index(), slot.item());
			}

			return itemContainerContents;
		}
	}

	public static ItemContainerContents fromItems(List<ItemStack> list) {
		int i = findLastNonEmptySlot(list);
		if (i == -1) {
			return EMPTY;
		} else {
			ItemContainerContents itemContainerContents = new ItemContainerContents(i + 1);

			for (int j = 0; j <= i; j++) {
				itemContainerContents.items.set(j, ((ItemStack)list.get(j)).copy());
			}

			return itemContainerContents;
		}
	}

	private static int findLastNonEmptySlot(List<ItemStack> list) {
		for (int i = list.size() - 1; i >= 0; i--) {
			if (!((ItemStack)list.get(i)).isEmpty()) {
				return i;
			}
		}

		return -1;
	}

	private List<ItemContainerContents.Slot> asSlots() {
		List<ItemContainerContents.Slot> list = new ArrayList();

		for (int i = 0; i < this.items.size(); i++) {
			ItemStack itemStack = this.items.get(i);
			if (!itemStack.isEmpty()) {
				list.add(new ItemContainerContents.Slot(i, itemStack));
			}
		}

		return list;
	}

	public void copyInto(NonNullList<ItemStack> nonNullList) {
		for (int i = 0; i < nonNullList.size(); i++) {
			ItemStack itemStack = i < this.items.size() ? this.items.get(i) : ItemStack.EMPTY;
			nonNullList.set(i, itemStack.copy());
		}
	}

	public ItemStack copyOne() {
		return this.items.isEmpty() ? ItemStack.EMPTY : this.items.get(0).copy();
	}

	public Stream<ItemStack> stream() {
		return this.items.stream().map(ItemStack::copy);
	}

	public Stream<ItemStack> nonEmptyStream() {
		return this.items.stream().filter(itemStack -> !itemStack.isEmpty()).map(ItemStack::copy);
	}

	public Iterable<ItemStack> nonEmptyItems() {
		return Iterables.filter(this.items, itemStack -> !itemStack.isEmpty());
	}

	public Iterable<ItemStack> nonEmptyItemsCopy() {
		return Iterables.transform(this.nonEmptyItems(), ItemStack::copy);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof ItemContainerContents itemContainerContents && ItemStack.listMatches(this.items, itemContainerContents.items)) {
				return true;
			}

			return false;
		}
	}

	public int hashCode() {
		return ItemStack.hashStackList(this.items);
	}

	static record Slot(int index, ItemStack item) {
		public static final Codec<ItemContainerContents.Slot> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.intRange(0, 255).fieldOf("slot").forGetter(ItemContainerContents.Slot::index),
						ItemStack.CODEC.fieldOf("item").forGetter(ItemContainerContents.Slot::item)
					)
					.apply(instance, ItemContainerContents.Slot::new)
		);
	}
}
