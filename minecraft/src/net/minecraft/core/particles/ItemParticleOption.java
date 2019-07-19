package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ItemParticleOption implements ParticleOptions {
	public static final ParticleOptions.Deserializer<ItemParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ItemParticleOption>() {
		public ItemParticleOption fromCommand(ParticleType<ItemParticleOption> particleType, StringReader stringReader) throws CommandSyntaxException {
			stringReader.expect(' ');
			ItemParser itemParser = new ItemParser(stringReader, false).parse();
			ItemStack itemStack = new ItemInput(itemParser.getItem(), itemParser.getNbt()).createItemStack(1, false);
			return new ItemParticleOption(particleType, itemStack);
		}

		public ItemParticleOption fromNetwork(ParticleType<ItemParticleOption> particleType, FriendlyByteBuf friendlyByteBuf) {
			return new ItemParticleOption(particleType, friendlyByteBuf.readItem());
		}
	};
	private final ParticleType<ItemParticleOption> type;
	private final ItemStack itemStack;

	public ItemParticleOption(ParticleType<ItemParticleOption> particleType, ItemStack itemStack) {
		this.type = particleType;
		this.itemStack = itemStack;
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeItem(this.itemStack);
	}

	@Override
	public String writeToString() {
		return Registry.PARTICLE_TYPE.getKey(this.getType()) + " " + new ItemInput(this.itemStack.getItem(), this.itemStack.getTag()).serialize();
	}

	@Override
	public ParticleType<ItemParticleOption> getType() {
		return this.type;
	}

	@Environment(EnvType.CLIENT)
	public ItemStack getItem() {
		return this.itemStack;
	}
}
