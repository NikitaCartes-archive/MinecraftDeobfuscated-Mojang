package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import java.util.Random;
import java.util.function.BiFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public class DustParticleOptions implements ParticleOptions {
	public static final DustParticleOptions REDSTONE = new DustParticleOptions(1.0F, 0.0F, 0.0F, 1.0F);
	public static final ParticleOptions.Deserializer<DustParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<DustParticleOptions>() {
		public DustParticleOptions fromCommand(ParticleType<DustParticleOptions> particleType, StringReader stringReader) throws CommandSyntaxException {
			stringReader.expect(' ');
			float f = (float)stringReader.readDouble();
			stringReader.expect(' ');
			float g = (float)stringReader.readDouble();
			stringReader.expect(' ');
			float h = (float)stringReader.readDouble();
			stringReader.expect(' ');
			float i = (float)stringReader.readDouble();
			return new DustParticleOptions(f, g, h, i);
		}

		public DustParticleOptions fromNetwork(ParticleType<DustParticleOptions> particleType, FriendlyByteBuf friendlyByteBuf) {
			return new DustParticleOptions(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
		}
	};
	private final float r;
	private final float g;
	private final float b;
	private final float scale;
	public static final BiFunction<Random, ParticleType<DustParticleOptions>, DustParticleOptions> RANDOM_PROVIDER = (random, particleType) -> new DustParticleOptions(
			random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat() * 2.0F
		);

	public DustParticleOptions(float f, float g, float h, float i) {
		this.r = f;
		this.g = g;
		this.b = h;
		this.scale = Mth.clamp(i, 0.01F, 4.0F);
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeFloat(this.r);
		friendlyByteBuf.writeFloat(this.g);
		friendlyByteBuf.writeFloat(this.b);
		friendlyByteBuf.writeFloat(this.scale);
	}

	@Override
	public String writeToString() {
		return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), this.r, this.g, this.b, this.scale);
	}

	@Override
	public ParticleType<DustParticleOptions> getType() {
		return ParticleTypes.DUST;
	}

	@Environment(EnvType.CLIENT)
	public float getR() {
		return this.r;
	}

	@Environment(EnvType.CLIENT)
	public float getG() {
		return this.g;
	}

	@Environment(EnvType.CLIENT)
	public float getB() {
		return this.b;
	}

	@Environment(EnvType.CLIENT)
	public float getScale() {
		return this.scale;
	}
}
