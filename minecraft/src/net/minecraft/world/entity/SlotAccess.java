package net.minecraft.world.entity;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface SlotAccess {
	SlotAccess NULL = new SlotAccess() {
		@Override
		public ItemStack get() {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean set(ItemStack itemStack) {
			return false;
		}
	};

	static SlotAccess of(Supplier<ItemStack> supplier, Consumer<ItemStack> consumer) {
		return new SlotAccess() {
			@Override
			public ItemStack get() {
				return (ItemStack)supplier.get();
			}

			@Override
			public boolean set(ItemStack itemStack) {
				consumer.accept(itemStack);
				return true;
			}
		};
	}

	static SlotAccess forContainer(Container container, int i, Predicate<ItemStack> predicate) {
		return new SlotAccess() {
			@Override
			public ItemStack get() {
				return container.getItem(i);
			}

			@Override
			public boolean set(ItemStack itemStack) {
				if (!predicate.test(itemStack)) {
					return false;
				} else {
					container.setItem(i, itemStack);
					return true;
				}
			}
		};
	}

	static SlotAccess forContainer(Container container, int i) {
		return forContainer(container, i, itemStack -> true);
	}

	static SlotAccess forEquipmentSlot(LivingEntity livingEntity, EquipmentSlot equipmentSlot, Predicate<ItemStack> predicate) {
		return new SlotAccess() {
			@Override
			public ItemStack get() {
				return livingEntity.getItemBySlot(equipmentSlot);
			}

			@Override
			public boolean set(ItemStack itemStack) {
				if (!predicate.test(itemStack)) {
					return false;
				} else {
					livingEntity.setItemSlot(equipmentSlot, itemStack);
					return true;
				}
			}
		};
	}

	static SlotAccess forEquipmentSlot(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
		return forEquipmentSlot(livingEntity, equipmentSlot, itemStack -> true);
	}

	ItemStack get();

	boolean set(ItemStack itemStack);
}
