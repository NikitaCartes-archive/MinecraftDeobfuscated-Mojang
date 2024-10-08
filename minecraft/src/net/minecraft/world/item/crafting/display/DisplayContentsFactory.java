package net.minecraft.world.item.crafting.display;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface DisplayContentsFactory<T> {
	public interface ForRemainders<T> extends DisplayContentsFactory<T> {
		T addRemainder(T object, List<T> list);
	}

	public interface ForStacks<T> extends DisplayContentsFactory<T> {
		default T forStack(Holder<Item> holder) {
			return this.forStack(new ItemStack(holder));
		}

		default T forStack(Item item) {
			return this.forStack(new ItemStack(item));
		}

		T forStack(ItemStack itemStack);
	}
}
