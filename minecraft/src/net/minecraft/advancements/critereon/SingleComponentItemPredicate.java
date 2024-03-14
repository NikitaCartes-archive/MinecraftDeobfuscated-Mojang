package net.minecraft.advancements.critereon;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public interface SingleComponentItemPredicate<T> extends ItemSubPredicate {
	@Override
	default boolean matches(ItemStack itemStack) {
		T object = itemStack.get(this.componentType());
		return object != null && this.matches(itemStack, object);
	}

	DataComponentType<T> componentType();

	boolean matches(ItemStack itemStack, T object);
}
