package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;

public class VibrationParticleOption implements ParticleOptions {
	public static final Codec<VibrationParticleOption> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(VibrationPath.CODEC.fieldOf("vibration").forGetter(vibrationParticleOption -> vibrationParticleOption.vibrationPath))
				.apply(instance, VibrationParticleOption::new)
	);
	public static final ParticleOptions.Deserializer<VibrationParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<VibrationParticleOption>() {
		public VibrationParticleOption fromCommand(ParticleType<VibrationParticleOption> particleType, StringReader stringReader) throws CommandSyntaxException {
			stringReader.expect(' ');
			float f = (float)stringReader.readDouble();
			stringReader.expect(' ');
			float g = (float)stringReader.readDouble();
			stringReader.expect(' ');
			float h = (float)stringReader.readDouble();
			stringReader.expect(' ');
			float i = (float)stringReader.readDouble();
			stringReader.expect(' ');
			float j = (float)stringReader.readDouble();
			stringReader.expect(' ');
			float k = (float)stringReader.readDouble();
			stringReader.expect(' ');
			int l = stringReader.readInt();
			BlockPos blockPos = new BlockPos((double)f, (double)g, (double)h);
			BlockPos blockPos2 = new BlockPos((double)i, (double)j, (double)k);
			return new VibrationParticleOption(new VibrationPath(blockPos, new BlockPositionSource(blockPos2), l));
		}

		public VibrationParticleOption fromNetwork(ParticleType<VibrationParticleOption> particleType, FriendlyByteBuf friendlyByteBuf) {
			VibrationPath vibrationPath = VibrationPath.read(friendlyByteBuf);
			return new VibrationParticleOption(vibrationPath);
		}
	};
	private final VibrationPath vibrationPath;

	public VibrationParticleOption(VibrationPath vibrationPath) {
		this.vibrationPath = vibrationPath;
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		VibrationPath.write(friendlyByteBuf, this.vibrationPath);
	}

	@Override
	public String writeToString() {
		BlockPos blockPos = this.vibrationPath.getOrigin();
		double d = (double)blockPos.getX();
		double e = (double)blockPos.getY();
		double f = (double)blockPos.getZ();
		return String.format(
			Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f %d", Registry.PARTICLE_TYPE.getKey(this.getType()), d, e, f, d, e, f, this.vibrationPath.getArrivalInTicks()
		);
	}

	@Override
	public ParticleType<VibrationParticleOption> getType() {
		return ParticleTypes.VIBRATION;
	}

	public VibrationPath getVibrationPath() {
		return this.vibrationPath;
	}
}
