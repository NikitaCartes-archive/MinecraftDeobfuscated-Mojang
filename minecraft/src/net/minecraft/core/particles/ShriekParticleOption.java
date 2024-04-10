package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ShriekParticleOption implements ParticleOptions {
	public static final MapCodec<ShriekParticleOption> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(Codec.INT.fieldOf("delay").forGetter(shriekParticleOption -> shriekParticleOption.delay))
				.apply(instance, ShriekParticleOption::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ShriekParticleOption> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, shriekParticleOption -> shriekParticleOption.delay, ShriekParticleOption::new
	);
	private final int delay;

	public ShriekParticleOption(int i) {
		this.delay = i;
	}

	@Override
	public ParticleType<ShriekParticleOption> getType() {
		return ParticleTypes.SHRIEK;
	}

	public int getDelay() {
		return this.delay;
	}
}
