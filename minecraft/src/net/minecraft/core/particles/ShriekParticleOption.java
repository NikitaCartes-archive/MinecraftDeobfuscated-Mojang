package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Locale;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
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
	public static final ParticleOptions.Deserializer<ShriekParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ShriekParticleOption>() {
		public ShriekParticleOption fromCommand(ParticleType<ShriekParticleOption> particleType, StringReader stringReader, HolderLookup.Provider provider) throws CommandSyntaxException {
			stringReader.expect(' ');
			int i = stringReader.readInt();
			return new ShriekParticleOption(i);
		}
	};
	private final int delay;

	public ShriekParticleOption(int i) {
		this.delay = i;
	}

	@Override
	public String writeToString(HolderLookup.Provider provider) {
		return String.format(Locale.ROOT, "%s %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.delay);
	}

	@Override
	public ParticleType<ShriekParticleOption> getType() {
		return ParticleTypes.SHRIEK;
	}

	public int getDelay() {
		return this.delay;
	}
}
