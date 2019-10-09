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
	public AbstractSmallTreeFeature(Function<Dynamic<?>, ? extends T> function) {
		super(function);
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
		int l = levelSimulatedRW.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, blockPos).getY();
		int m = levelSimulatedRW.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY();
		BlockPos blockPos2 = new BlockPos(blockPos.getX(), l, blockPos.getZ());
		if (m - l > smallTreeConfiguration.maxWaterDepth) {
			return Optional.empty();
		} else if (blockPos2.getY() >= 1 && blockPos2.getY() + i + 1 <= 256) {
			for (int n = 0; n <= i + 1; n++) {
				int o = smallTreeConfiguration.foliagePlacer.getTreeRadiusForHeight(j, i, k, n);
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int p = -o; p <= o; p++) {
					int q = -o;

					while (q <= o) {
						if (n + blockPos2.getY() >= 0 && n + blockPos2.getY() < 256) {
							mutableBlockPos.set(p + blockPos2.getX(), n + blockPos2.getY(), q + blockPos2.getZ());
							if (isFree(levelSimulatedRW, mutableBlockPos) && (smallTreeConfiguration.ignoreVines || !isVine(levelSimulatedRW, mutableBlockPos))) {
								q++;
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
