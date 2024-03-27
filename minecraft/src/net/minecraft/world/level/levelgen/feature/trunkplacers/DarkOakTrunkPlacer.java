package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class DarkOakTrunkPlacer extends TrunkPlacer {
	public static final MapCodec<DarkOakTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
		instance -> trunkPlacerParts(instance).apply(instance, DarkOakTrunkPlacer::new)
	);

	public DarkOakTrunkPlacer(int i, int j, int k) {
		super(i, j, k);
	}

	@Override
	protected TrunkPlacerType<?> type() {
		return TrunkPlacerType.DARK_OAK_TRUNK_PLACER;
	}

	@Override
	public List<FoliagePlacer.FoliageAttachment> placeTrunk(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		int i,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration
	) {
		List<FoliagePlacer.FoliageAttachment> list = Lists.<FoliagePlacer.FoliageAttachment>newArrayList();
		BlockPos blockPos2 = blockPos.below();
		setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2, treeConfiguration);
		setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2.east(), treeConfiguration);
		setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2.south(), treeConfiguration);
		setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos2.south().east(), treeConfiguration);
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
		int j = i - randomSource.nextInt(4);
		int k = 2 - randomSource.nextInt(3);
		int l = blockPos.getX();
		int m = blockPos.getY();
		int n = blockPos.getZ();
		int o = l;
		int p = n;
		int q = m + i - 1;

		for (int r = 0; r < i; r++) {
			if (r >= j && k > 0) {
				o += direction.getStepX();
				p += direction.getStepZ();
				k--;
			}

			int s = m + r;
			BlockPos blockPos3 = new BlockPos(o, s, p);
			if (TreeFeature.isAirOrLeaves(levelSimulatedReader, blockPos3)) {
				this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos3, treeConfiguration);
				this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos3.east(), treeConfiguration);
				this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos3.south(), treeConfiguration);
				this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos3.east().south(), treeConfiguration);
			}
		}

		list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(o, q, p), 0, true));

		for (int r = -1; r <= 2; r++) {
			for (int s = -1; s <= 2; s++) {
				if ((r < 0 || r > 1 || s < 0 || s > 1) && randomSource.nextInt(3) <= 0) {
					int t = randomSource.nextInt(3) + 2;

					for (int u = 0; u < t; u++) {
						this.placeLog(levelSimulatedReader, biConsumer, randomSource, new BlockPos(l + r, q - u - 1, n + s), treeConfiguration);
					}

					list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(o + r, q, p + s), 0, false));
				}
			}
		}

		return list;
	}
}
