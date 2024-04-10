package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastColor;

public class ColorParticleOption implements ParticleOptions {
	private final ParticleType<ColorParticleOption> type;
	private final int color;

	public static MapCodec<ColorParticleOption> codec(ParticleType<ColorParticleOption> particleType) {
		return ExtraCodecs.ARGB_COLOR_CODEC
			.<ColorParticleOption>xmap(integer -> new ColorParticleOption(particleType, integer), colorParticleOption -> colorParticleOption.color)
			.fieldOf("color");
	}

	public static StreamCodec<? super ByteBuf, ColorParticleOption> streamCodec(ParticleType<ColorParticleOption> particleType) {
		return ByteBufCodecs.INT.map(integer -> new ColorParticleOption(particleType, integer), colorParticleOption -> colorParticleOption.color);
	}

	private ColorParticleOption(ParticleType<ColorParticleOption> particleType, int i) {
		this.type = particleType;
		this.color = i;
	}

	@Override
	public ParticleType<ColorParticleOption> getType() {
		return this.type;
	}

	public float getRed() {
		return (float)FastColor.ARGB32.red(this.color) / 255.0F;
	}

	public float getGreen() {
		return (float)FastColor.ARGB32.green(this.color) / 255.0F;
	}

	public float getBlue() {
		return (float)FastColor.ARGB32.blue(this.color) / 255.0F;
	}

	public float getAlpha() {
		return (float)FastColor.ARGB32.alpha(this.color) / 255.0F;
	}

	public static ColorParticleOption create(ParticleType<ColorParticleOption> particleType, int i) {
		return new ColorParticleOption(particleType, i);
	}

	public static ColorParticleOption create(ParticleType<ColorParticleOption> particleType, float f, float g, float h) {
		return create(particleType, FastColor.ARGB32.colorFromFloat(1.0F, f, g, h));
	}
}
