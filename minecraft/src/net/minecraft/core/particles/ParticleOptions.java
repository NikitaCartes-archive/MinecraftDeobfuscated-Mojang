package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface ParticleOptions {
	ParticleType<?> getType();

	String writeToString();

	@Deprecated
	public interface Deserializer<T extends ParticleOptions> {
		T fromCommand(ParticleType<T> particleType, StringReader stringReader) throws CommandSyntaxException;
	}
}
