package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class GroundBushFeature extends AbstractTreeFeature<TreeConfiguration> {
	public GroundBushFeature(Function<Dynamic<?>, ? extends TreeConfiguration> function, Function<Random, ? extends TreeConfiguration> function2) {
		super(function, function2);
	}

	@Override
	public boolean doPlace(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos blockPos,
		Set<BlockPos> set,
		Set<BlockPos> set2,
		BoundingBox boundingBox,
		TreeConfiguration treeConfiguration
	) {
		blockPos = levelSimulatedRW.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos).below();
		if (isGrassOrDirt(levelSimulatedRW, blockPos)) {
			blockPos = blockPos.above();
			this.placeLog(levelSimulatedRW, random, blockPos, set, boundingBox, treeConfiguration);

			for (int i = 0; i <= 2; i++) {
				int j = 2 - i;

				for (int k = -j; k <= j; k++) {
					for (int l = -j; l <= j; l++) {
						if (Math.abs(k) != j || Math.abs(l) != j || random.nextInt(2) != 0) {
							this.placeLeaf(
								levelSimulatedRW, random, new BlockPos(k + blockPos.getX(), i + blockPos.getY(), l + blockPos.getZ()), set2, boundingBox, treeConfiguration
							);
						}
					}
				}
			}
		}

		return true;
	}
}
