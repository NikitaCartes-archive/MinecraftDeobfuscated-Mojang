package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFossilFeature extends StructureFeature<NoneFeatureConfiguration> {
	public NetherFossilFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return NetherFossilFeature.FeatureStart::new;
	}

	public static class FeatureStart extends NoiseAffectingStructureStart<NoneFeatureConfiguration> {
		public FeatureStart(StructureFeature<NoneFeatureConfiguration> structureFeature, ChunkPos chunkPos, int i, long l) {
			super(structureFeature, chunkPos, i, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			Biome biome,
			NoneFeatureConfiguration noneFeatureConfiguration,
			LevelHeightAccessor levelHeightAccessor
		) {
			int i = chunkPos.getMinBlockX() + this.random.nextInt(16);
			int j = chunkPos.getMinBlockZ() + this.random.nextInt(16);
			int k = chunkGenerator.getSeaLevel();
			int l = k + this.random.nextInt(chunkGenerator.getGenDepth() - 2 - k);
			NoiseColumn noiseColumn = chunkGenerator.getBaseColumn(i, j, levelHeightAccessor);

			for (BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, l, j); l > k; l--) {
				BlockState blockState = noiseColumn.getBlockState(mutableBlockPos);
				mutableBlockPos.move(Direction.DOWN);
				BlockState blockState2 = noiseColumn.getBlockState(mutableBlockPos);
				if (blockState.isAir() && (blockState2.is(Blocks.SOUL_SAND) || blockState2.isFaceSturdy(EmptyBlockGetter.INSTANCE, mutableBlockPos, Direction.UP))) {
					break;
				}
			}

			if (l > k) {
				NetherFossilPieces.addPieces(structureManager, this, this.random, new BlockPos(i, l, j));
			}
		}
	}
}
