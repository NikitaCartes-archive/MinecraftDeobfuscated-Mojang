package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class DustParticleOptions extends ScalableParticleOptionsBase {
	public static final Vector3f REDSTONE_PARTICLE_COLOR = Vec3.fromRGB24(16711680).toVector3f();
	public static final DustParticleOptions REDSTONE = new DustParticleOptions(REDSTONE_PARTICLE_COLOR, 1.0F);
	public static final MapCodec<DustParticleOptions> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(dustParticleOptions -> dustParticleOptions.color),
					SCALE.fieldOf("scale").forGetter(ScalableParticleOptionsBase::getScale)
				)
				.apply(instance, DustParticleOptions::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, DustParticleOptions> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VECTOR3F,
		dustParticleOptions -> dustParticleOptions.color,
		ByteBufCodecs.FLOAT,
		ScalableParticleOptionsBase::getScale,
		DustParticleOptions::new
	);
	private final Vector3f color;

	public DustParticleOptions(Vector3f vector3f, float f) {
		super(f);
		this.color = vector3f;
	}

	@Override
	public ParticleType<DustParticleOptions> getType() {
		return ParticleTypes.DUST;
	}

	public Vector3f getColor() {
		return this.color;
	}
}
