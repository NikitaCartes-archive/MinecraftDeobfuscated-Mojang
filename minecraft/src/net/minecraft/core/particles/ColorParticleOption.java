package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class ColorParticleOption implements ParticleOptions {
	public static final ParticleOptions.Deserializer<ColorParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ColorParticleOption>() {
		public ColorParticleOption fromCommand(ParticleType<ColorParticleOption> particleType, StringReader stringReader, HolderLookup.Provider provider) throws CommandSyntaxException {
			Vector3f vector3f = DustParticleOptionsBase.readVector3f(stringReader);
			stringReader.expect(' ');
			float f = stringReader.readFloat();
			int i = FastColor.ARGB32.color(
				ColorParticleOption.as32BitChannel(f),
				ColorParticleOption.as32BitChannel(vector3f.x),
				ColorParticleOption.as32BitChannel(vector3f.y),
				ColorParticleOption.as32BitChannel(vector3f.z)
			);
			return new ColorParticleOption(particleType, i);
		}
	};
	private final ParticleType<? extends ColorParticleOption> type;
	private final int color;

	public static Codec<ColorParticleOption> codec(ParticleType<ColorParticleOption> particleType) {
		return Codec.INT.xmap(integer -> new ColorParticleOption(particleType, integer), colorParticleOption -> colorParticleOption.color);
	}

	public static StreamCodec<? super ByteBuf, ColorParticleOption> streamCodec(ParticleType<ColorParticleOption> particleType) {
		return ByteBufCodecs.INT.map(integer -> new ColorParticleOption(particleType, integer), colorParticleOption -> colorParticleOption.color);
	}

	ColorParticleOption(ParticleType<? extends ColorParticleOption> particleType, int i) {
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

	static int as32BitChannel(float f) {
		return Mth.floor(f * 255.0F);
	}
}
