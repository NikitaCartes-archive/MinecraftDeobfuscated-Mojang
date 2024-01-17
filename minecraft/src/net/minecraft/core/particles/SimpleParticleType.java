package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class SimpleParticleType extends ParticleType<SimpleParticleType> implements ParticleOptions {
	private static final ParticleOptions.Deserializer<SimpleParticleType> DESERIALIZER = new ParticleOptions.Deserializer<SimpleParticleType>() {
		public SimpleParticleType fromCommand(ParticleType<SimpleParticleType> particleType, StringReader stringReader) {
			return (SimpleParticleType)particleType;
		}
	};
	private final Codec<SimpleParticleType> codec = Codec.unit(this::getType);
	private final StreamCodec<RegistryFriendlyByteBuf, SimpleParticleType> streamCodec = StreamCodec.unit(this);

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
	public StreamCodec<RegistryFriendlyByteBuf, SimpleParticleType> streamCodec() {
		return this.streamCodec;
	}

	@Override
	public String writeToString() {
		return BuiltInRegistries.PARTICLE_TYPE.getKey(this).toString();
	}
}
