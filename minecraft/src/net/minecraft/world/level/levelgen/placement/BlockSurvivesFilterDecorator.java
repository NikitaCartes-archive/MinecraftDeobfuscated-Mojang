package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.SingleBlockStateConfiguration;

public class BlockSurvivesFilterDecorator extends FeatureDecorator<SingleBlockStateConfiguration> {
	public BlockSurvivesFilterDecorator(Codec<SingleBlockStateConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		DecorationContext decorationContext, Random random, SingleBlockStateConfiguration singleBlockStateConfiguration, BlockPos blockPos
	) {
		return !singleBlockStateConfiguration.state().canSurvive(decorationContext.getLevel(), blockPos) ? Stream.of() : Stream.of(blockPos);
	}
}
