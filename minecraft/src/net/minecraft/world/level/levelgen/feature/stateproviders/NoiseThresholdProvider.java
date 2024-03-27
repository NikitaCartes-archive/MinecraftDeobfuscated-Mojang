package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseThresholdProvider extends NoiseBasedStateProvider {
	public static final MapCodec<NoiseThresholdProvider> CODEC = RecordCodecBuilder.mapCodec(
		instance -> noiseCodec(instance)
				.<float, float, BlockState, List<BlockState>, List<BlockState>>and(
					instance.group(
						Codec.floatRange(-1.0F, 1.0F).fieldOf("threshold").forGetter(noiseThresholdProvider -> noiseThresholdProvider.threshold),
						Codec.floatRange(0.0F, 1.0F).fieldOf("high_chance").forGetter(noiseThresholdProvider -> noiseThresholdProvider.highChance),
						BlockState.CODEC.fieldOf("default_state").forGetter(noiseThresholdProvider -> noiseThresholdProvider.defaultState),
						Codec.list(BlockState.CODEC).fieldOf("low_states").forGetter(noiseThresholdProvider -> noiseThresholdProvider.lowStates),
						Codec.list(BlockState.CODEC).fieldOf("high_states").forGetter(noiseThresholdProvider -> noiseThresholdProvider.highStates)
					)
				)
				.apply(instance, NoiseThresholdProvider::new)
	);
	private final float threshold;
	private final float highChance;
	private final BlockState defaultState;
	private final List<BlockState> lowStates;
	private final List<BlockState> highStates;

	public NoiseThresholdProvider(
		long l, NormalNoise.NoiseParameters noiseParameters, float f, float g, float h, BlockState blockState, List<BlockState> list, List<BlockState> list2
	) {
		super(l, noiseParameters, f);
		this.threshold = g;
		this.highChance = h;
		this.defaultState = blockState;
		this.lowStates = list;
		this.highStates = list2;
	}

	@Override
	protected BlockStateProviderType<?> type() {
		return BlockStateProviderType.NOISE_THRESHOLD_PROVIDER;
	}

	@Override
	public BlockState getState(RandomSource randomSource, BlockPos blockPos) {
		double d = this.getNoiseValue(blockPos, (double)this.scale);
		if (d < (double)this.threshold) {
			return Util.getRandom(this.lowStates, randomSource);
		} else {
			return randomSource.nextFloat() < this.highChance ? Util.getRandom(this.highStates, randomSource) : this.defaultState;
		}
	}
}
