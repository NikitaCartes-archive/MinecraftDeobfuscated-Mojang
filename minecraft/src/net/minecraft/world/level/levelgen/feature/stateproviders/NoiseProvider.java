package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Products.P4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseProvider extends NoiseBasedStateProvider {
	public static final Codec<NoiseProvider> CODEC = RecordCodecBuilder.create(instance -> noiseProviderCodec(instance).apply(instance, NoiseProvider::new));
	protected final List<BlockState> states;

	protected static <P extends NoiseProvider> P4<Mu<P>, Long, NormalNoise.NoiseParameters, Float, List<BlockState>> noiseProviderCodec(Instance<P> instance) {
		return noiseCodec(instance).and(Codec.list(BlockState.CODEC).fieldOf("states").forGetter(noiseProvider -> noiseProvider.states));
	}

	public NoiseProvider(long l, NormalNoise.NoiseParameters noiseParameters, float f, List<BlockState> list) {
		super(l, noiseParameters, f);
		this.states = list;
	}

	@Override
	protected BlockStateProviderType<?> type() {
		return BlockStateProviderType.NOISE_PROVIDER;
	}

	@Override
	public BlockState getState(Random random, BlockPos blockPos) {
		return this.getRandomState(this.states, blockPos, (double)this.scale);
	}

	protected BlockState getRandomState(List<BlockState> list, BlockPos blockPos, double d) {
		double e = this.getNoiseValue(blockPos, d);
		return this.getRandomState(list, e);
	}

	protected BlockState getRandomState(List<BlockState> list, double d) {
		double e = Mth.clamp((1.0 + d) / 2.0, 0.0, 0.9999);
		return (BlockState)list.get((int)(e * (double)list.size()));
	}
}
