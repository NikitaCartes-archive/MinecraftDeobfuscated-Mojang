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
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class ForkingTrunkPlacer extends TrunkPlacer {
	public static final Codec<ForkingTrunkPlacer> CODEC = RecordCodecBuilder.create(
		instance -> trunkPlacerParts(instance).apply(instance, ForkingTrunkPlacer::new)
	);

	public ForkingTrunkPlacer(int i, int j, int k) {
		super(i, j, k);
	}

	@Override
	protected TrunkPlacerType<?> type() {
		return TrunkPlacerType.FORKING_TRUNK_PLACER;
	}

	@Override
	public List<FoliagePlacer.FoliageAttachment> placeTrunk(
		LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	) {
		setDirtAt(levelSimulatedRW, blockPos.below());
		List<FoliagePlacer.FoliageAttachment> list = Lists.<FoliagePlacer.FoliageAttachment>newArrayList();
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		int j = i - random.nextInt(4) - 1;
		int k = 3 - random.nextInt(3);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int l = blockPos.getX();
		int m = blockPos.getZ();
		int n = 0;

		for (int o = 0; o < i; o++) {
			int p = blockPos.getY() + o;
			if (o >= j && k > 0) {
				l += direction.getStepX();
				m += direction.getStepZ();
				k--;
			}

			if (placeLog(levelSimulatedRW, random, mutableBlockPos.set(l, p, m), set, boundingBox, treeConfiguration)) {
				n = p + 1;
			}
		}

		list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(l, n, m), 1, false));
		l = blockPos.getX();
		m = blockPos.getZ();
		Direction direction2 = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		if (direction2 != direction) {
			int px = j - random.nextInt(2) - 1;
			int q = 1 + random.nextInt(3);
			n = 0;

			for (int r = px; r < i && q > 0; q--) {
				if (r >= 1) {
					int s = blockPos.getY() + r;
					l += direction2.getStepX();
					m += direction2.getStepZ();
					if (placeLog(levelSimulatedRW, random, mutableBlockPos.set(l, s, m), set, boundingBox, treeConfiguration)) {
						n = s + 1;
					}
				}

				r++;
			}

			if (n > 1) {
				list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(l, n, m), 0, false));
			}
		}

		return list;
	}
}
