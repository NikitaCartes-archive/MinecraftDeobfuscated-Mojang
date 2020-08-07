package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
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

	public static class FeatureStart extends BeardedStructureStart<NoneFeatureConfiguration> {
		public FeatureStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			int i,
			int j,
			Biome biome,
			NoneFeatureConfiguration noneFeatureConfiguration
		) {
			ChunkPos chunkPos = new ChunkPos(i, j);
			int k = chunkPos.getMinBlockX() + this.random.nextInt(16);
			int l = chunkPos.getMinBlockZ() + this.random.nextInt(16);
			int m = chunkGenerator.getSeaLevel();
			int n = m + this.random.nextInt(chunkGenerator.getGenDepth() - 2 - m);
			BlockGetter blockGetter = chunkGenerator.getBaseColumn(k, l);

			for (BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(k, n, l); n > m; n--) {
				BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
				mutableBlockPos.move(Direction.DOWN);
				BlockState blockState2 = blockGetter.getBlockState(mutableBlockPos);
				if (blockState.isAir() && (blockState2.is(Blocks.SOUL_SAND) || blockState2.isFaceSturdy(blockGetter, mutableBlockPos, Direction.UP))) {
					break;
				}
			}

			if (n > m) {
				NetherFossilPieces.addPieces(structureManager, this.pieces, this.random, new BlockPos(k, n, l));
				this.calculateBoundingBox();
			}
		}
	}
}
