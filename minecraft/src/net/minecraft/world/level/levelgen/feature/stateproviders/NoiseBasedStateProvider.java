package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public abstract class NoiseBasedStateProvider extends BlockStateProvider {
	protected final long seed;
	protected final NormalNoise.NoiseParameters parameters;
	protected final float scale;
	protected final NormalNoise noise;

	protected static <P extends NoiseBasedStateProvider> P3<Mu<P>, Long, NormalNoise.NoiseParameters, Float> noiseCodec(Instance<P> instance) {
		return instance.group(
			Codec.LONG.fieldOf("seed").forGetter(noiseBasedStateProvider -> noiseBasedStateProvider.seed),
			NormalNoise.NoiseParameters.CODEC.fieldOf("noise").forGetter(noiseBasedStateProvider -> noiseBasedStateProvider.parameters),
			ExtraCodecs.POSITIVE_FLOAT.fieldOf("scale").forGetter(noiseBasedStateProvider -> noiseBasedStateProvider.scale)
		);
	}

	protected NoiseBasedStateProvider(long l, NormalNoise.NoiseParameters noiseParameters, float f) {
		this.seed = l;
		this.parameters = noiseParameters;
		this.scale = f;
		this.noise = NormalNoise.create(new WorldgenRandom(new LegacyRandomSource(l)), noiseParameters);
	}

	protected double getNoiseValue(BlockPos blockPos, double d) {
		return this.noise.getValue((double)blockPos.getX() * d, (double)blockPos.getY() * d, (double)blockPos.getZ() * d);
	}
}
