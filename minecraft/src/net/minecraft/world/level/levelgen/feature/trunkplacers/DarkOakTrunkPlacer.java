package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class DarkOakTrunkPlacer extends TrunkPlacer {
	public static final Codec<DarkOakTrunkPlacer> CODEC = RecordCodecBuilder.create(
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
		LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	) {
		List<FoliagePlacer.FoliageAttachment> list = Lists.<FoliagePlacer.FoliageAttachment>newArrayList();
		BlockPos blockPos2 = blockPos.below();
		setDirtAt(levelSimulatedRW, blockPos2);
		setDirtAt(levelSimulatedRW, blockPos2.east());
		setDirtAt(levelSimulatedRW, blockPos2.south());
		setDirtAt(levelSimulatedRW, blockPos2.south().east());
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		int j = i - random.nextInt(4);
		int k = 2 - random.nextInt(3);
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
			if (TreeFeature.isAirOrLeaves(levelSimulatedRW, blockPos3)) {
				placeLog(levelSimulatedRW, random, blockPos3, set, boundingBox, treeConfiguration);
				placeLog(levelSimulatedRW, random, blockPos3.east(), set, boundingBox, treeConfiguration);
				placeLog(levelSimulatedRW, random, blockPos3.south(), set, boundingBox, treeConfiguration);
				placeLog(levelSimulatedRW, random, blockPos3.east().south(), set, boundingBox, treeConfiguration);
			}
		}

		list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(o, q, p), 0, true));

		for (int r = -1; r <= 2; r++) {
			for (int s = -1; s <= 2; s++) {
				if ((r < 0 || r > 1 || s < 0 || s > 1) && random.nextInt(3) <= 0) {
					int t = random.nextInt(3) + 2;

					for (int u = 0; u < t; u++) {
						placeLog(levelSimulatedRW, random, new BlockPos(l + r, q - u - 1, n + s), set, boundingBox, treeConfiguration);
					}

					list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(o + r, q, p + s), 0, false));
				}
			}
		}

		return list;
	}
}
