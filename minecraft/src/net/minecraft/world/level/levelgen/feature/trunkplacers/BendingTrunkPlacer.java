package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class BendingTrunkPlacer extends TrunkPlacer {
	public static final Codec<BendingTrunkPlacer> CODEC = RecordCodecBuilder.create(
		instance -> trunkPlacerParts(instance)
				.<Integer, IntProvider>and(
					instance.group(
						ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_height_for_leaves", 1).forGetter(bendingTrunkPlacer -> bendingTrunkPlacer.minHeightForLeaves),
						IntProvider.codec(1, 64).fieldOf("bend_length").forGetter(bendingTrunkPlacer -> bendingTrunkPlacer.bendLength)
					)
				)
				.apply(instance, BendingTrunkPlacer::new)
	);
	private final int minHeightForLeaves;
	private final IntProvider bendLength;

	public BendingTrunkPlacer(int i, int j, int k, int l, IntProvider intProvider) {
		super(i, j, k);
		this.minHeightForLeaves = l;
		this.bendLength = intProvider;
	}

	@Override
	protected TrunkPlacerType<?> type() {
		return TrunkPlacerType.BENDING_TRUNK_PLACER;
	}

	@Override
	public List<FoliagePlacer.FoliageAttachment> placeTrunk(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		Random random,
		int i,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration
	) {
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		int j = i - 1;
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		BlockPos blockPos2 = mutableBlockPos.below();
		setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2, treeConfiguration);
		List<FoliagePlacer.FoliageAttachment> list = Lists.<FoliagePlacer.FoliageAttachment>newArrayList();

		for (int k = 0; k <= j; k++) {
			if (k + 1 >= j + random.nextInt(2)) {
				mutableBlockPos.move(direction);
			}

			if (TreeFeature.validTreePos(levelSimulatedReader, mutableBlockPos)) {
				placeLog(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration);
			}

			if (k >= this.minHeightForLeaves) {
				list.add(new FoliagePlacer.FoliageAttachment(mutableBlockPos.immutable(), 0, false));
			}

			mutableBlockPos.move(Direction.UP);
		}

		int k = this.bendLength.sample(random);

		for (int l = 0; l <= k; l++) {
			if (TreeFeature.validTreePos(levelSimulatedReader, mutableBlockPos)) {
				placeLog(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration);
			}

			list.add(new FoliagePlacer.FoliageAttachment(mutableBlockPos.immutable(), 0, false));
			mutableBlockPos.move(direction);
		}

		return list;
	}
}
