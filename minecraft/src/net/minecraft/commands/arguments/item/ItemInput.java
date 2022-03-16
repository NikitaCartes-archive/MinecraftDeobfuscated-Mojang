package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemInput implements Predicate<ItemStack> {
	private static final Dynamic2CommandExceptionType ERROR_STACK_TOO_BIG = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("arguments.item.overstacked", object, object2)
	);
	private final Holder<Item> item;
	@Nullable
	private final CompoundTag tag;

	public ItemInput(Holder<Item> holder, @Nullable CompoundTag compoundTag) {
		this.item = holder;
		this.tag = compoundTag;
	}

	public Item getItem() {
		return this.item.value();
	}

	public boolean test(ItemStack itemStack) {
		return itemStack.is(this.item) && NbtUtils.compareNbt(this.tag, itemStack.getTag(), true);
	}

	public ItemStack createItemStack(int i, boolean bl) throws CommandSyntaxException {
		ItemStack itemStack = new ItemStack(this.item, i);
		if (this.tag != null) {
			itemStack.setTag(this.tag);
		}

		if (bl && i > itemStack.getMaxStackSize()) {
			throw ERROR_STACK_TOO_BIG.create(this.getItemName(), itemStack.getMaxStackSize());
		} else {
			return itemStack;
		}
	}

	public String serialize() {
		StringBuilder stringBuilder = new StringBuilder(this.getItemName());
		if (this.tag != null) {
			stringBuilder.append(this.tag);
		}

		return stringBuilder.toString();
	}

	private String getItemName() {
		return this.item.unwrapKey().map(ResourceKey::location).orElseGet(() -> "unknown[" + this.item + "]").toString();
	}
}
