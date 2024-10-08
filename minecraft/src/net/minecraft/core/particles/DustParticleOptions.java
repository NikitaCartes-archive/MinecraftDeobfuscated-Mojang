package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

public class DustParticleOptions extends ScalableParticleOptionsBase {
	public static final int REDSTONE_PARTICLE_COLOR = 16711680;
	public static final DustParticleOptions REDSTONE = new DustParticleOptions(16711680, 1.0F);
	public static final MapCodec<DustParticleOptions> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(dustParticleOptions -> dustParticleOptions.color),
					SCALE.fieldOf("scale").forGetter(ScalableParticleOptionsBase::getScale)
				)
				.apply(instance, DustParticleOptions::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, DustParticleOptions> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, dustParticleOptions -> dustParticleOptions.color, ByteBufCodecs.FLOAT, ScalableParticleOptionsBase::getScale, DustParticleOptions::new
	);
	private final int color;

	public DustParticleOptions(int i, float f) {
		super(f);
		this.color = i;
	}

	@Override
	public ParticleType<DustParticleOptions> getType() {
		return ParticleTypes.DUST;
	}

	public Vector3f getColor() {
		return ARGB.vector3fFromRGB24(this.color);
	}
}
