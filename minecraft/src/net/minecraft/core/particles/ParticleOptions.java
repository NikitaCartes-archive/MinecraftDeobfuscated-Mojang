package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.FriendlyByteBuf;

public interface ParticleOptions {
	ParticleType<?> getType();

	void writeToNetwork(FriendlyByteBuf friendlyByteBuf);

	String writeToString();

	@Deprecated
	public interface Deserializer<T extends ParticleOptions> {
		T fromCommand(ParticleType<T> particleType, StringReader stringReader) throws CommandSyntaxException;

		T fromNetwork(ParticleType<T> particleType, FriendlyByteBuf friendlyByteBuf);
	}
}
