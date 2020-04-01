package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaPineTreeFeature extends MegaTreeFeature<MegaTreeConfiguration> {
	public MegaPineTreeFeature(Function<Dynamic<?>, ? extends MegaTreeConfiguration> function, Function<Random, ? extends MegaTreeConfiguration> function2) {
		super(function, function2);
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
			this.createCrown(levelSimulatedRW, random, blockPos.getX(), blockPos.getZ(), blockPos.getY() + i, 0, set2, boundingBox, megaTreeConfiguration);
			this.placeTrunk(levelSimulatedRW, random, blockPos, i, set, boundingBox, megaTreeConfiguration);
			return true;
		}
	}

	private void createCrown(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		int i,
		int j,
		int k,
		int l,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		MegaTreeConfiguration megaTreeConfiguration
	) {
		int m = random.nextInt(5) + megaTreeConfiguration.crownHeight;
		int n = 0;

		for (int o = k - m; o <= k; o++) {
			int p = k - o;
			int q = l + Mth.floor((float)p / (float)m * 3.5F);
			this.placeDoubleTrunkLeaves(
				levelSimulatedRW, random, new BlockPos(i, o, j), q + (p > 0 && q == n && (o & 1) == 0 ? 1 : 0), set, boundingBox, megaTreeConfiguration
			);
			n = q;
		}
	}
}
