package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public class EnvironmentScanPlacement extends PlacementModifier {
	private final Direction directionOfSearch;
	private final BlockPredicate targetCondition;
	private final BlockPredicate allowedSearchCondition;
	private final int maxSteps;
	public static final MapCodec<EnvironmentScanPlacement> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Direction.VERTICAL_CODEC.fieldOf("direction_of_search").forGetter(environmentScanPlacement -> environmentScanPlacement.directionOfSearch),
					BlockPredicate.CODEC.fieldOf("target_condition").forGetter(environmentScanPlacement -> environmentScanPlacement.targetCondition),
					BlockPredicate.CODEC
						.optionalFieldOf("allowed_search_condition", BlockPredicate.alwaysTrue())
						.forGetter(environmentScanPlacement -> environmentScanPlacement.allowedSearchCondition),
					Codec.intRange(1, 32).fieldOf("max_steps").forGetter(environmentScanPlacement -> environmentScanPlacement.maxSteps)
				)
				.apply(instance, EnvironmentScanPlacement::new)
	);

	private EnvironmentScanPlacement(Direction direction, BlockPredicate blockPredicate, BlockPredicate blockPredicate2, int i) {
		this.directionOfSearch = direction;
		this.targetCondition = blockPredicate;
		this.allowedSearchCondition = blockPredicate2;
		this.maxSteps = i;
	}

	public static EnvironmentScanPlacement scanningFor(Direction direction, BlockPredicate blockPredicate, BlockPredicate blockPredicate2, int i) {
		return new EnvironmentScanPlacement(direction, blockPredicate, blockPredicate2, i);
	}

	public static EnvironmentScanPlacement scanningFor(Direction direction, BlockPredicate blockPredicate, int i) {
		return scanningFor(direction, blockPredicate, BlockPredicate.alwaysTrue(), i);
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		WorldGenLevel worldGenLevel = placementContext.getLevel();
		if (!this.allowedSearchCondition.test(worldGenLevel, mutableBlockPos)) {
			return Stream.of();
		} else {
			for (int i = 0; i < this.maxSteps; i++) {
				if (this.targetCondition.test(worldGenLevel, mutableBlockPos)) {
					return Stream.of(mutableBlockPos);
				}

				mutableBlockPos.move(this.directionOfSearch);
				if (worldGenLevel.isOutsideBuildHeight(mutableBlockPos.getY())) {
					return Stream.of();
				}

				if (!this.allowedSearchCondition.test(worldGenLevel, mutableBlockPos)) {
					break;
				}
			}

			return this.targetCondition.test(worldGenLevel, mutableBlockPos) ? Stream.of(mutableBlockPos) : Stream.of();
		}
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.ENVIRONMENT_SCAN;
	}
}
