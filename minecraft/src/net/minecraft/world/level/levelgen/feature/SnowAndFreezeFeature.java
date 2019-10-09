package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SnowAndFreezeFeature extends Feature<NoneFeatureConfiguration> {
	public SnowAndFreezeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				int k = blockPos.getX() + i;
				int l = blockPos.getZ() + j;
				int m = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
				mutableBlockPos.set(k, m, l);
				mutableBlockPos2.set(mutableBlockPos).move(Direction.DOWN, 1);
				Biome biome = levelAccessor.getBiome(mutableBlockPos);
				if (biome.shouldFreeze(levelAccessor, mutableBlockPos2, false)) {
					levelAccessor.setBlock(mutableBlockPos2, Blocks.ICE.defaultBlockState(), 2);
				}

				if (biome.shouldSnow(levelAccessor, mutableBlockPos)) {
					levelAccessor.setBlock(mutableBlockPos, Blocks.SNOW.defaultBlockState(), 2);
					BlockState blockState = levelAccessor.getBlockState(mutableBlockPos2);
					if (blockState.hasProperty(SnowyDirtBlock.SNOWY)) {
						levelAccessor.setBlock(mutableBlockPos2, blockState.setValue(SnowyDirtBlock.SNOWY, Boolean.valueOf(true)), 2);
					}
				}
			}
		}

		return true;
	}
}
