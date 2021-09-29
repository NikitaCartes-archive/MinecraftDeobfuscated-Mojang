package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

public class EnvironmentScanDecorator extends FeatureDecorator<EnvironmentScanConfiguration> {
	public EnvironmentScanDecorator(Codec<EnvironmentScanConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		DecorationContext decorationContext, Random random, EnvironmentScanConfiguration environmentScanConfiguration, BlockPos blockPos
	) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		WorldGenLevel worldGenLevel = decorationContext.getLevel();

		for (int i = 0; i < environmentScanConfiguration.maxSteps() && !worldGenLevel.isOutsideBuildHeight(mutableBlockPos.getY()); i++) {
			if (environmentScanConfiguration.targetCondition().test(worldGenLevel, mutableBlockPos)) {
				return Stream.of(mutableBlockPos);
			}

			mutableBlockPos.move(environmentScanConfiguration.directionOfSearch());
		}

		return Stream.of();
	}
}
