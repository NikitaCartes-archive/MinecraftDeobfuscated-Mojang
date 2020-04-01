package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class NoSurfaceOreFeature extends Feature<OreConfiguration> {
	NoSurfaceOreFeature(Function<Dynamic<?>, ? extends OreConfiguration> function, Function<Random, ? extends OreConfiguration> function2) {
		super(function, function2);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		OreConfiguration oreConfiguration
	) {
		int i = random.nextInt(oreConfiguration.size + 1);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int j = 0; j < i; j++) {
			this.offsetTargetPos(mutableBlockPos, random, blockPos, Math.min(j, 7));
			if (oreConfiguration.target.getPredicate().test(levelAccessor.getBlockState(mutableBlockPos)) && !this.isFacingAir(levelAccessor, mutableBlockPos)) {
				levelAccessor.setBlock(mutableBlockPos, oreConfiguration.state, 2);
			}
		}

		return true;
	}

	private void offsetTargetPos(BlockPos.MutableBlockPos mutableBlockPos, Random random, BlockPos blockPos, int i) {
		int j = this.getRandomPlacementInOneAxisRelativeToOrigin(random, i);
		int k = this.getRandomPlacementInOneAxisRelativeToOrigin(random, i);
		int l = this.getRandomPlacementInOneAxisRelativeToOrigin(random, i);
		mutableBlockPos.setWithOffset(blockPos, j, k, l);
	}

	private int getRandomPlacementInOneAxisRelativeToOrigin(Random random, int i) {
		return Math.round((random.nextFloat() - random.nextFloat()) * (float)i);
	}

	private boolean isFacingAir(LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.values()) {
			mutableBlockPos.setWithOffset(blockPos, direction);
			if (levelAccessor.getBlockState(mutableBlockPos).isAir()) {
				return true;
			}
		}

		return false;
	}
}
