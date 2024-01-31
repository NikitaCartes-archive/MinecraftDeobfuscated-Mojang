package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.HolderLookup;

public interface ParticleOptions {
	ParticleType<?> getType();

	String writeToString(HolderLookup.Provider provider);

	@Deprecated
	public interface Deserializer<T extends ParticleOptions> {
		T fromCommand(ParticleType<T> particleType, StringReader stringReader, HolderLookup.Provider provider) throws CommandSyntaxException;
	}
}
