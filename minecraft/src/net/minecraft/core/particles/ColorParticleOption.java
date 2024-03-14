package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class ColorParticleOption implements ParticleOptions {
	public static final ParticleOptions.Deserializer<ColorParticleOption> DESERIALIZER = (particleType, stringReader, provider) -> new ColorParticleOption(
			particleType, stringReader.readInt()
		);
	private final ParticleType<? extends ColorParticleOption> type;
	private final int color;

	public static Codec<ColorParticleOption> codec(ParticleType<ColorParticleOption> particleType) {
		return Codec.INT.xmap(integer -> new ColorParticleOption(particleType, integer), colorParticleOption -> colorParticleOption.color);
	}

	public static StreamCodec<? super ByteBuf, ColorParticleOption> streamCodec(ParticleType<ColorParticleOption> particleType) {
		return ByteBufCodecs.INT.map(integer -> new ColorParticleOption(particleType, integer), colorParticleOption -> colorParticleOption.color);
	}

	private ColorParticleOption(ParticleType<? extends ColorParticleOption> particleType, int i) {
		this.type = particleType;
		this.color = i;
	}

	@Override
	public ParticleType<?> getType() {
		return this.type;
	}

	@Override
	public String writeToString(HolderLookup.Provider provider) {
		return String.format(Locale.ROOT, "%s 0x%x", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.color);
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

	public static ColorParticleOption create(ParticleType<? extends ColorParticleOption> particleType, int i) {
		return new ColorParticleOption(particleType, i);
	}

	public static ColorParticleOption create(ParticleType<? extends ColorParticleOption> particleType, float f, float g, float h) {
		return create(particleType, FastColor.ARGB32.color(as32BitChannel(f), as32BitChannel(g), as32BitChannel(h)));
	}

	private static int as32BitChannel(float f) {
		return Mth.floor(f * 255.0F);
	}
}
