package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class VibrationParticleOption implements ParticleOptions {
	public static final Codec<VibrationParticleOption> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					PositionSource.CODEC.fieldOf("destination").forGetter(VibrationParticleOption::getDestination),
					Codec.INT.fieldOf("arrival_in_ticks").forGetter(VibrationParticleOption::getArrivalInTicks)
				)
				.apply(instance, VibrationParticleOption::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, VibrationParticleOption> STREAM_CODEC = StreamCodec.composite(
		PositionSource.STREAM_CODEC,
		VibrationParticleOption::getDestination,
		ByteBufCodecs.VAR_INT,
		VibrationParticleOption::getArrivalInTicks,
		VibrationParticleOption::new
	);
	public static final ParticleOptions.Deserializer<VibrationParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<VibrationParticleOption>() {
		public VibrationParticleOption fromCommand(ParticleType<VibrationParticleOption> particleType, StringReader stringReader, HolderLookup.Provider provider) throws CommandSyntaxException {
			stringReader.expect(' ');
			float f = (float)stringReader.readDouble();
			stringReader.expect(' ');
			float g = (float)stringReader.readDouble();
			stringReader.expect(' ');
			float h = (float)stringReader.readDouble();
			stringReader.expect(' ');
			int i = stringReader.readInt();
			BlockPos blockPos = BlockPos.containing((double)f, (double)g, (double)h);
			return new VibrationParticleOption(new BlockPositionSource(blockPos), i);
		}
	};
	private final PositionSource destination;
	private final int arrivalInTicks;

	public VibrationParticleOption(PositionSource positionSource, int i) {
		this.destination = positionSource;
		this.arrivalInTicks = i;
	}

	@Override
	public String writeToString(HolderLookup.Provider provider) {
		Vec3 vec3 = (Vec3)this.destination.getPosition(null).get();
		double d = vec3.x();
		double e = vec3.y();
		double f = vec3.z();
		return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), d, e, f, this.arrivalInTicks);
	}

	@Override
	public ParticleType<VibrationParticleOption> getType() {
		return ParticleTypes.VIBRATION;
	}

	public PositionSource getDestination() {
		return this.destination;
	}

	public int getArrivalInTicks() {
		return this.arrivalInTicks;
	}
}
