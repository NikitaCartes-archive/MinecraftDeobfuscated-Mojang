package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class DustParticleOptions extends DustParticleOptionsBase {
	public static final Vec3 REDSTONE_PARTICLE_COLOR = Vec3.fromRGB24(16711680);
	public static final DustParticleOptions REDSTONE = new DustParticleOptions(REDSTONE_PARTICLE_COLOR, 1.0F);
	public static final Codec<DustParticleOptions> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Vec3.CODEC.fieldOf("color").forGetter(dustParticleOptions -> dustParticleOptions.color),
					Codec.FLOAT.fieldOf("scale").forGetter(dustParticleOptions -> dustParticleOptions.scale)
				)
				.apply(instance, DustParticleOptions::new)
	);
	public static final ParticleOptions.Deserializer<DustParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustParticleOptions>() {
		public DustParticleOptions fromCommand(ParticleType<DustParticleOptions> particleType, StringReader stringReader) throws CommandSyntaxException {
			Vec3 vec3 = DustParticleOptionsBase.readVec3(stringReader);
			stringReader.expect(' ');
			float f = (float)stringReader.readDouble();
			return new DustParticleOptions(vec3, f);
		}

		public DustParticleOptions fromNetwork(ParticleType<DustParticleOptions> particleType, FriendlyByteBuf friendlyByteBuf) {
			return new DustParticleOptions(
				new Vec3((double)friendlyByteBuf.readFloat(), (double)friendlyByteBuf.readFloat(), (double)friendlyByteBuf.readFloat()), friendlyByteBuf.readFloat()
			);
		}
	};

	public DustParticleOptions(Vec3 vec3, float f) {
		super(vec3, f);
	}

	@Override
	public ParticleType<DustParticleOptions> getType() {
		return ParticleTypes.DUST;
	}
}
