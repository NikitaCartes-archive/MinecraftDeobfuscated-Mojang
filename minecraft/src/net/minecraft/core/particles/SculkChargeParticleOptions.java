package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;

public record SculkChargeParticleOptions(float roll) implements ParticleOptions {
	public static final Codec<SculkChargeParticleOptions> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Codec.FLOAT.fieldOf("roll").forGetter(sculkChargeParticleOptions -> sculkChargeParticleOptions.roll))
				.apply(instance, SculkChargeParticleOptions::new)
	);
	public static final ParticleOptions.Deserializer<SculkChargeParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<SculkChargeParticleOptions>() {
		public SculkChargeParticleOptions fromCommand(ParticleType<SculkChargeParticleOptions> particleType, StringReader stringReader) throws CommandSyntaxException {
			stringReader.expect(' ');
			float f = stringReader.readFloat();
			return new SculkChargeParticleOptions(f);
		}

		public SculkChargeParticleOptions fromNetwork(ParticleType<SculkChargeParticleOptions> particleType, FriendlyByteBuf friendlyByteBuf) {
			return new SculkChargeParticleOptions(friendlyByteBuf.readFloat());
		}
	};

	@Override
	public ParticleType<SculkChargeParticleOptions> getType() {
		return ParticleTypes.SCULK_CHARGE;
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeFloat(this.roll);
	}

	@Override
	public String writeToString() {
		return String.format(Locale.ROOT, "%s %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), this.roll);
	}
}
