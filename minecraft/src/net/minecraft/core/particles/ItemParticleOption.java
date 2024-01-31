package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class ItemParticleOption implements ParticleOptions {
	public static final ParticleOptions.Deserializer<ItemParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ItemParticleOption>() {
		public ItemParticleOption fromCommand(ParticleType<ItemParticleOption> particleType, StringReader stringReader, HolderLookup.Provider provider) throws CommandSyntaxException {
			stringReader.expect(' ');
			ItemParser.ItemResult itemResult = new ItemParser(provider).parse(stringReader);
			ItemStack itemStack = new ItemInput(itemResult.item(), itemResult.nbt()).createItemStack(1, false);
			return new ItemParticleOption(particleType, itemStack);
		}
	};
	private final ParticleType<ItemParticleOption> type;
	private final ItemStack itemStack;

	public static Codec<ItemParticleOption> codec(ParticleType<ItemParticleOption> particleType) {
		return ItemStack.CODEC.xmap(itemStack -> new ItemParticleOption(particleType, itemStack), itemParticleOption -> itemParticleOption.itemStack);
	}

	public static StreamCodec<? super RegistryFriendlyByteBuf, ItemParticleOption> streamCodec(ParticleType<ItemParticleOption> particleType) {
		return ItemStack.STREAM_CODEC.map(itemStack -> new ItemParticleOption(particleType, itemStack), itemParticleOption -> itemParticleOption.itemStack);
	}

	public ItemParticleOption(ParticleType<ItemParticleOption> particleType, ItemStack itemStack) {
		this.type = particleType;
		this.itemStack = itemStack;
	}

	@Override
	public String writeToString(HolderLookup.Provider provider) {
		return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + new ItemInput(this.itemStack.getItemHolder(), this.itemStack.getTag()).serialize();
	}

	@Override
	public ParticleType<ItemParticleOption> getType() {
		return this.type;
	}

	public ItemStack getItem() {
		return this.itemStack;
	}
}
