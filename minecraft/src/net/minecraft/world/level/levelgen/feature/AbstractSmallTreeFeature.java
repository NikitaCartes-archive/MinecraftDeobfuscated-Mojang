package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public abstract class AbstractSmallTreeFeature<T extends SmallTreeConfiguration> extends AbstractTreeFeature<T> {
	public AbstractSmallTreeFeature(Function<Dynamic<?>, ? extends T> function) {
		super(function);
	}

	public Optional<BlockPos> getProjectedOrigin(LevelSimulatedRW levelSimulatedRW, int i, int j, BlockPos blockPos, SmallTreeConfiguration smallTreeConfiguration) {
		BlockPos blockPos2;
		if (!smallTreeConfiguration.fromSapling) {
			int k = levelSimulatedRW.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, blockPos).getY();
			int l = levelSimulatedRW.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY();
			blockPos2 = new BlockPos(blockPos.getX(), k, blockPos.getZ());
			if (l - k > smallTreeConfiguration.maxWaterDepth) {
				return Optional.empty();
			}
		} else {
			blockPos2 = blockPos;
		}

		if (blockPos2.getY() >= 1 && blockPos2.getY() + i + 1 <= 256) {
			for (int k = 0; k <= i + 1; k++) {
				int l = smallTreeConfiguration.foliagePlacer.getTreeRadiusForHeight(i, j, k);
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int m = -l; m <= l; m++) {
					int n = -l;

					while (n <= l) {
						if (k + blockPos2.getY() >= 0 && k + blockPos2.getY() < 256) {
							mutableBlockPos.set(m + blockPos2.getX(), k + blockPos2.getY(), n + blockPos2.getZ());
							if (isFree(levelSimulatedRW, mutableBlockPos) && (smallTreeConfiguration.ignoreVines || !isVine(levelSimulatedRW, mutableBlockPos))) {
								n++;
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
