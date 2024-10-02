package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record TargetColorParticleOption(Vec3 target, int color) implements ParticleOptions {
	public static final MapCodec<TargetColorParticleOption> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Vec3.CODEC.fieldOf("target").forGetter(TargetColorParticleOption::target), Codec.INT.fieldOf("color").forGetter(TargetColorParticleOption::color)
				)
				.apply(instance, TargetColorParticleOption::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, TargetColorParticleOption> STREAM_CODEC = StreamCodec.composite(
		Vec3.STREAM_CODEC, TargetColorParticleOption::target, ByteBufCodecs.INT, TargetColorParticleOption::color, TargetColorParticleOption::new
	);

	@Override
	public ParticleType<TargetColorParticleOption> getType() {
		return ParticleTypes.TRAIL;
	}
}
