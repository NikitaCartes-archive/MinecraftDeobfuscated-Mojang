package net.minecraft.world.level.dimension.special;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class G28 extends SpecialDimensionBase {
	public G28(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.5F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G28.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return 0.0F;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
		return vec3;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isFoggyAt(int i, int j) {
		return false;
	}

	public static class Generator extends ChunkGenerator<NoneGeneratorSettings> {
		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, NoneGeneratorSettings noneGeneratorSettings) {
			super(levelAccessor, biomeSource, noneGeneratorSettings);
		}

		@Override
		public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
		}

		@Override
		public void applyCarvers(BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
		}

		@Override
		public int getSpawnHeight() {
			return 0;
		}

		@Override
		public void fillFromNoise(LevelAccessor levelAccessor, ChunkAccess chunkAccess) {
		}

		@Override
		public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
			if (worldGenRegion.getCenterX() == 31415 && worldGenRegion.getCenterZ() == 92653) {
				ChunkAccess chunkAccess = worldGenRegion.getChunk(worldGenRegion.getCenterX(), worldGenRegion.getCenterZ());
				BlockPos blockPos = new BlockPos(worldGenRegion.getCenterX() * 16, 100, worldGenRegion.getCenterZ() * 16);
				chunkAccess.setBlockState(
					new BlockPos(worldGenRegion.getCenterX() * 16, 99, worldGenRegion.getCenterZ() * 16), Blocks.GRASS_BLOCK.defaultBlockState(), false
				);
				chunkAccess.setBlockState(blockPos, Blocks.ACACIA_SIGN.defaultBlockState(), false);
				SignBlockEntity signBlockEntity = new SignBlockEntity();
				signBlockEntity.setMessage(1, new TextComponent("Ha! I lied!"));
				signBlockEntity.setMessage(2, new TextComponent("This isn't nothing!"));
				chunkAccess.setBlockEntity(blockPos, signBlockEntity);
			}
		}

		@Override
		public int getBaseHeight(int i, int j, Heightmap.Types types) {
			return 0;
		}

		@Override
		public BlockGetter getBaseColumn(int i, int j) {
			return EmptyBlockGetter.INSTANCE;
		}

		@Override
		public ChunkGeneratorType<?, ?> getType() {
			return ChunkGeneratorType.T23;
		}
	}
}
