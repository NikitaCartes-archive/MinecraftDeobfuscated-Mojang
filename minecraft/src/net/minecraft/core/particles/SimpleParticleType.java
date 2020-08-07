package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;

public class SimpleParticleType extends ParticleType<SimpleParticleType> implements ParticleOptions {
	private static final ParticleOptions.Deserializer<SimpleParticleType> DESERIALIZER = new ParticleOptions.Deserializer<SimpleParticleType>() {
		public SimpleParticleType fromCommand(ParticleType<SimpleParticleType> particleType, StringReader stringReader) throws CommandSyntaxException {
			return (SimpleParticleType)particleType;
		}

		public SimpleParticleType fromNetwork(ParticleType<SimpleParticleType> particleType, FriendlyByteBuf friendlyByteBuf) {
			return (SimpleParticleType)particleType;
		}
	};
	private final Codec<SimpleParticleType> codec = Codec.unit(this::getType);

	protected SimpleParticleType(boolean bl) {
		super(bl, DESERIALIZER);
	}

	public SimpleParticleType getType() {
		return this;
	}

	@Override
	public Codec<SimpleParticleType> codec() {
		return this.codec;
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
	}

	@Override
	public String writeToString() {
		return Registry.PARTICLE_TYPE.getKey(this).toString();
	}
}
