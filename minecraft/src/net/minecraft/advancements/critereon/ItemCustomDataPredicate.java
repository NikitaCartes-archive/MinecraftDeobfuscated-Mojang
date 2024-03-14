package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

public record ItemCustomDataPredicate(NbtPredicate value) implements ItemSubPredicate {
	public static final Codec<ItemCustomDataPredicate> CODEC = NbtPredicate.CODEC.xmap(ItemCustomDataPredicate::new, ItemCustomDataPredicate::value);

	@Override
	public boolean matches(ItemStack itemStack) {
		return this.value.matches(itemStack);
	}

	public static ItemCustomDataPredicate customData(NbtPredicate nbtPredicate) {
		return new ItemCustomDataPredicate(nbtPredicate);
	}
}
