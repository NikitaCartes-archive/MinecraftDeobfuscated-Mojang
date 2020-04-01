package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class TreeFeature extends AbstractSmallTreeFeature<SmallTreeConfiguration> {
	public TreeFeature(Function<Dynamic<?>, ? extends SmallTreeConfiguration> function, Function<Random, ? extends SmallTreeConfiguration> function2) {
		super(function, function2);
	}

	public boolean doPlace(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos blockPos,
		Set<BlockPos> set,
		Set<BlockPos> set2,
		BoundingBox boundingBox,
		SmallTreeConfiguration smallTreeConfiguration
	) {
		int i = smallTreeConfiguration.baseHeight + random.nextInt(smallTreeConfiguration.heightRandA + 1) + random.nextInt(smallTreeConfiguration.heightRandB + 1);
		int j = smallTreeConfiguration.trunkHeight >= 0
			? smallTreeConfiguration.trunkHeight + random.nextInt(smallTreeConfiguration.trunkHeightRandom + 1)
			: i - (smallTreeConfiguration.foliageHeight + random.nextInt(smallTreeConfiguration.foliageHeightRandom + 1));
		int k = smallTreeConfiguration.foliagePlacer.foliageRadius(random, j, i, smallTreeConfiguration);
		Optional<BlockPos> optional = this.getProjectedOrigin(levelSimulatedRW, i, j, k, blockPos, smallTreeConfiguration);
		if (!optional.isPresent()) {
			return false;
		} else {
			BlockPos blockPos2 = (BlockPos)optional.get();
			this.setDirtAt(levelSimulatedRW, blockPos2.below());
			smallTreeConfiguration.foliagePlacer.createFoliage(levelSimulatedRW, random, smallTreeConfiguration, i, j, k, blockPos2, set2);
			this.placeTrunk(
				levelSimulatedRW,
				random,
				i,
				blockPos2,
				smallTreeConfiguration.trunkTopOffset + random.nextInt(smallTreeConfiguration.trunkTopOffsetRandom + 1),
				set,
				boundingBox,
				smallTreeConfiguration
			);
			return true;
		}
	}
}
