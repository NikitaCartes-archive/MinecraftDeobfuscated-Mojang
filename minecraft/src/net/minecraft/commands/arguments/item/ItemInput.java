package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemInput implements Predicate<ItemStack> {
	private static final Dynamic2CommandExceptionType ERROR_STACK_TOO_BIG = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("arguments.item.overstacked", object, object2)
	);
	private final Item item;
	@Nullable
	private final CompoundTag tag;

	public ItemInput(Item item, @Nullable CompoundTag compoundTag) {
		this.item = item;
		this.tag = compoundTag;
	}

	public Item getItem() {
		return this.item;
	}

	public boolean test(ItemStack itemStack) {
		return itemStack.getItem() == this.item && NbtUtils.compareNbt(this.tag, itemStack.getTag(), true);
	}

	public ItemStack createItemStack(int i, boolean bl) throws CommandSyntaxException {
		ItemStack itemStack = new ItemStack(this.item, i);
		if (this.tag != null) {
			itemStack.setTag(this.tag);
		}

		if (bl && i > itemStack.getMaxStackSize()) {
			throw ERROR_STACK_TOO_BIG.create(Registry.ITEM.getKey(this.item), itemStack.getMaxStackSize());
		} else {
			return itemStack;
		}
	}

	public String serialize() {
		StringBuilder stringBuilder = new StringBuilder(Registry.ITEM.getId(this.item));
		if (this.tag != null) {
			stringBuilder.append(this.tag);
		}

		return stringBuilder.toString();
	}
}
