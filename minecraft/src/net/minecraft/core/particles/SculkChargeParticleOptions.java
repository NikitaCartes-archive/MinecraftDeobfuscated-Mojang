package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SculkChargeParticleOptions(float roll) implements ParticleOptions {
	public static final MapCodec<SculkChargeParticleOptions> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(Codec.FLOAT.fieldOf("roll").forGetter(sculkChargeParticleOptions -> sculkChargeParticleOptions.roll))
				.apply(instance, SculkChargeParticleOptions::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, SculkChargeParticleOptions> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, sculkChargeParticleOptions -> sculkChargeParticleOptions.roll, SculkChargeParticleOptions::new
	);
	public static final ParticleOptions.Deserializer<SculkChargeParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<SculkChargeParticleOptions>() {
		public SculkChargeParticleOptions fromCommand(
			ParticleType<SculkChargeParticleOptions> particleType, StringReader stringReader, HolderLookup.Provider provider
		) throws CommandSyntaxException {
			stringReader.expect(' ');
			float f = stringReader.readFloat();
			return new SculkChargeParticleOptions(f);
		}
	};

	@Override
	public ParticleType<SculkChargeParticleOptions> getType() {
		return ParticleTypes.SCULK_CHARGE;
	}

	@Override
	public String writeToString(HolderLookup.Provider provider) {
		return String.format(Locale.ROOT, "%s %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.roll);
	}
}
