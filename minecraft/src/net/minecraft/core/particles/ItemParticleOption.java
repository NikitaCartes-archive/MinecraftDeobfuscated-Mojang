package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemParticleOption implements ParticleOptions {
	private static final Codec<ItemStack> ITEM_CODEC = Codec.withAlternative(ItemStack.SINGLE_ITEM_CODEC, Item.CODEC, ItemStack::new);
	private final ParticleType<ItemParticleOption> type;
	private final ItemStack itemStack;

	public static MapCodec<ItemParticleOption> codec(ParticleType<ItemParticleOption> particleType) {
		return ITEM_CODEC.<ItemParticleOption>xmap(itemStack -> new ItemParticleOption(particleType, itemStack), itemParticleOption -> itemParticleOption.itemStack)
			.fieldOf("item");
	}

	public static StreamCodec<? super RegistryFriendlyByteBuf, ItemParticleOption> streamCodec(ParticleType<ItemParticleOption> particleType) {
		return ItemStack.STREAM_CODEC.map(itemStack -> new ItemParticleOption(particleType, itemStack), itemParticleOption -> itemParticleOption.itemStack);
	}

	public ItemParticleOption(ParticleType<ItemParticleOption> particleType, ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			throw new IllegalArgumentException("Empty stacks are not allowed");
		} else {
			this.type = particleType;
			this.itemStack = itemStack;
		}
	}

	@Override
	public ParticleType<ItemParticleOption> getType() {
		return this.type;
	}

	public ItemStack getItem() {
		return this.itemStack;
	}
}
