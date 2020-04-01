package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class AbstractSmallTreeFeature<T extends SmallTreeConfiguration> extends AbstractTreeFeature<T> {
	public AbstractSmallTreeFeature(Function<Dynamic<?>, ? extends T> function, Function<Random, ? extends T> function2) {
		super(function, function2);
	}

	protected void placeTrunk(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		int i,
		BlockPos blockPos,
		int j,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		SmallTreeConfiguration smallTreeConfiguration
	) {
		for (int k = 0; k < i - j; k++) {
			this.placeLog(levelSimulatedRW, random, blockPos.above(k), set, boundingBox, smallTreeConfiguration);
		}
	}

	public Optional<BlockPos> getProjectedOrigin(
		LevelSimulatedRW levelSimulatedRW, int i, int j, int k, BlockPos blockPos, SmallTreeConfiguration smallTreeConfiguration
	) {
		BlockPos blockPos2;
		if (!smallTreeConfiguration.fromSapling) {
			int l = levelSimulatedRW.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, blockPos).getY();
			int m = levelSimulatedRW.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY();
			blockPos2 = new BlockPos(blockPos.getX(), l, blockPos.getZ());
			if (m - l > smallTreeConfiguration.maxWaterDepth) {
				return Optional.empty();
			}
		} else {
			blockPos2 = blockPos;
		}

		if (blockPos2.getY() >= 1 && blockPos2.getY() + i + 1 <= 256) {
			for (int l = 0; l <= i + 1; l++) {
				int m = smallTreeConfiguration.foliagePlacer.getTreeRadiusForHeight(j, i, k, l);
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int n = -m; n <= m; n++) {
					int o = -m;

					while (o <= m) {
						if (l + blockPos2.getY() >= 0 && l + blockPos2.getY() < 256) {
							mutableBlockPos.set(n + blockPos2.getX(), l + blockPos2.getY(), o + blockPos2.getZ());
							if (isFree(levelSimulatedRW, mutableBlockPos) && (smallTreeConfiguration.ignoreVines || !isVine(levelSimulatedRW, mutableBlockPos))) {
								o++;
								continue;
							}

							return Optional.empty();
						}

						return Optional.empty();
					}
				}
			}

			return isGrassOrDirtOrFarmland(levelSimulatedRW, blockPos2.below()) && blockPos2.getY() < 256 - i - 1 ? Optional.of(blockPos2) : Optional.empty();
		} else {
			return Optional.empty();
		}
	}
}
