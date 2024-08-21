package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record UseRemainder(ItemStack convertInto) {
	public static final Codec<UseRemainder> CODEC = ItemStack.CODEC.xmap(UseRemainder::new, UseRemainder::convertInto);
	public static final StreamCodec<RegistryFriendlyByteBuf, UseRemainder> STREAM_CODEC = StreamCodec.composite(
		ItemStack.STREAM_CODEC, UseRemainder::convertInto, UseRemainder::new
	);

	public ItemStack convertIntoRemainder(LivingEntity livingEntity, ItemStack itemStack, int i) {
		boolean bl = livingEntity.hasInfiniteMaterials();
		ItemStack itemStack2 = this.convertInto.copy();
		if (bl) {
			return itemStack;
		} else if (itemStack.getCount() >= i) {
			return itemStack;
		} else if (itemStack.isEmpty()) {
			return itemStack2;
		} else {
			if (!livingEntity.level().isClientSide() && livingEntity instanceof Player player && !player.getInventory().add(itemStack2)) {
				player.drop(itemStack2, false);
			}

			return itemStack;
		}
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			UseRemainder useRemainder = (UseRemainder)object;
			return ItemStack.matches(this.convertInto, useRemainder.convertInto);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return ItemStack.hashItemAndComponents(this.convertInto);
	}
}
