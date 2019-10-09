package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaJungleTreeFeature extends MegaTreeFeature<MegaTreeConfiguration> {
	public MegaJungleTreeFeature(Function<Dynamic<?>, ? extends MegaTreeConfiguration> function) {
		super(function);
	}

	public boolean doPlace(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos blockPos,
		Set<BlockPos> set,
		Set<BlockPos> set2,
		BoundingBox boundingBox,
		MegaTreeConfiguration megaTreeConfiguration
	) {
		int i = this.calcTreeHeigth(random, megaTreeConfiguration);
		if (!this.prepareTree(levelSimulatedRW, blockPos, i)) {
			return false;
		} else {
			this.createCrown(levelSimulatedRW, random, blockPos.above(i), 2, set2, boundingBox, megaTreeConfiguration);

			for (int j = blockPos.getY() + i - 2 - random.nextInt(4); j > blockPos.getY() + i / 2; j -= 2 + random.nextInt(4)) {
				float f = random.nextFloat() * (float) (Math.PI * 2);
				int k = blockPos.getX() + (int)(0.5F + Mth.cos(f) * 4.0F);
				int l = blockPos.getZ() + (int)(0.5F + Mth.sin(f) * 4.0F);

				for (int m = 0; m < 5; m++) {
					k = blockPos.getX() + (int)(1.5F + Mth.cos(f) * (float)m);
					l = blockPos.getZ() + (int)(1.5F + Mth.sin(f) * (float)m);
					BlockPos blockPos2 = new BlockPos(k, j - 3 + m / 2, l);
					this.placeLog(levelSimulatedRW, random, blockPos2, set, boundingBox, megaTreeConfiguration);
				}

				int m = 1 + random.nextInt(2);
				int n = j;

				for (int o = j - m; o <= n; o++) {
					int p = o - n;
					this.placeSingleTrunkLeaves(levelSimulatedRW, random, new BlockPos(k, o, l), 1 - p, set2, boundingBox, megaTreeConfiguration);
				}
			}

			this.placeTrunk(levelSimulatedRW, random, blockPos, i, set, boundingBox, megaTreeConfiguration);
			return true;
		}
	}

	private void createCrown(
		LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, int i, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	) {
		int j = 2;

		for (int k = -2; k <= 0; k++) {
			this.placeDoubleTrunkLeaves(levelSimulatedRW, random, blockPos.above(k), i + 1 - k, set, boundingBox, treeConfiguration);
		}
	}
}
