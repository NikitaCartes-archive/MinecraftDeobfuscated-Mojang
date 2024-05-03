package net.minecraft.world.level.storage.loot;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public interface ContainerComponentManipulator<T> {
	DataComponentType<T> type();

	T empty();

	T setContents(T object, Stream<ItemStack> stream);

	Stream<ItemStack> getContents(T object);

	default void setContents(ItemStack itemStack, T object, Stream<ItemStack> stream) {
		T object2 = itemStack.getOrDefault(this.type(), object);
		T object3 = this.setContents(object2, stream);
		itemStack.set(this.type(), object3);
	}

	default void setContents(ItemStack itemStack, Stream<ItemStack> stream) {
		this.setContents(itemStack, this.empty(), stream);
	}

	default void modifyItems(ItemStack itemStack, UnaryOperator<ItemStack> unaryOperator) {
		T object = itemStack.get(this.type());
		if (object != null) {
			UnaryOperator<ItemStack> unaryOperator2 = itemStackx -> {
				if (itemStackx.isEmpty()) {
					return itemStackx;
				} else {
					ItemStack itemStack2 = (ItemStack)unaryOperator.apply(itemStackx);
					itemStack2.limitSize(itemStack2.getMaxStackSize());
					return itemStack2;
				}
			};
			this.setContents(itemStack, this.getContents(object).map(unaryOperator2));
		}
	}
}
