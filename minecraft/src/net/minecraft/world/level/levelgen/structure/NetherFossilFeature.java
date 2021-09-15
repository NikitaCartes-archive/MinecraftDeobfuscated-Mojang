package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFossilFeature extends StructureFeature<RangeDecoratorConfiguration> {
	public NetherFossilFeature(Codec<RangeDecoratorConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureFeature.StructureStartFactory<RangeDecoratorConfiguration> getStartFactory() {
		return NetherFossilFeature.FeatureStart::new;
	}

	public static class FeatureStart extends NoiseAffectingStructureStart<RangeDecoratorConfiguration> {
		public FeatureStart(StructureFeature<RangeDecoratorConfiguration> structureFeature, ChunkPos chunkPos, int i, long l) {
			super(structureFeature, chunkPos, i, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			RangeDecoratorConfiguration rangeDecoratorConfiguration,
			LevelHeightAccessor levelHeightAccessor,
			Predicate<Biome> predicate
		) {
			int i = chunkPos.getMinBlockX() + this.random.nextInt(16);
			int j = chunkPos.getMinBlockZ() + this.random.nextInt(16);
			int k = chunkGenerator.getSeaLevel();
			WorldGenerationContext worldGenerationContext = new WorldGenerationContext(chunkGenerator, levelHeightAccessor);
			int l = rangeDecoratorConfiguration.height.sample(this.random, worldGenerationContext);
			NoiseColumn noiseColumn = chunkGenerator.getBaseColumn(i, j, levelHeightAccessor);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, l, j);

			while (l > k) {
				BlockState blockState = noiseColumn.getBlock(l);
				BlockState blockState2 = noiseColumn.getBlock(--l);
				if (blockState.isAir() && (blockState2.is(Blocks.SOUL_SAND) || blockState2.isFaceSturdy(EmptyBlockGetter.INSTANCE, mutableBlockPos.setY(l), Direction.UP))) {
					break;
				}
			}

			if (l > k) {
				if (predicate.test(chunkGenerator.getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(l), QuartPos.fromBlock(j)))) {
					NetherFossilPieces.addPieces(structureManager, this, this.random, new BlockPos(i, l, j));
				}
			}
		}
	}
}
