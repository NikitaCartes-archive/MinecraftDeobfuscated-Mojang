package net.minecraft.world.level.dimension.special;

import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.phys.Vec3;

public class G26 extends SpecialDimensionBase {
	public G26(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G26.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return 12000.0F;
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
		private final PerlinSimplexNoise toneNoise;
		private final PerlinSimplexNoise instrumentNoise;

		public Generator(LevelAccessor levelAccessor, BiomeSource biomeSource, NoneGeneratorSettings noneGeneratorSettings) {
			super(levelAccessor, biomeSource, noneGeneratorSettings);
			WorldgenRandom worldgenRandom = new WorldgenRandom(levelAccessor.getSeed());
			this.toneNoise = new PerlinSimplexNoise(worldgenRandom, IntStream.rangeClosed(-4, 1));
			this.instrumentNoise = new PerlinSimplexNoise(worldgenRandom, IntStream.rangeClosed(-5, 2));
		}

		@Override
		public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
		}

		@Override
		public int getSpawnHeight() {
			return 30;
		}

		@Override
		public void fillFromNoise(LevelAccessor levelAccessor, ChunkAccess chunkAccess) {
			ChunkPos chunkPos = chunkAccess.getPos();
			NoteBlockInstrument[] noteBlockInstruments = NoteBlockInstrument.values();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 16; j++) {
					int k = (int)((this.toneNoise.getValue((double)(chunkPos.x * 16 + i), (double)(chunkPos.z * 16 + j), false) + 1.0) / 2.0 * 24.0);
					int l = (int)(
						(this.instrumentNoise.getValue((double)((float)(chunkPos.x * 16 + i) / 4.0F), (double)((float)(chunkPos.z * 16 + j) / 4.0F), false) + 1.0)
							/ 2.0
							* (double)noteBlockInstruments.length
					);
					chunkAccess.setBlockState(
						mutableBlockPos.set(i, 0, j),
						Blocks.NOTE_BLOCK.defaultBlockState().setValue(NoteBlock.INSTRUMENT, noteBlockInstruments[l]).setValue(NoteBlock.NOTE, Integer.valueOf(k)),
						false
					);
				}
			}
		}

		@Override
		public void applyCarvers(BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
		}

		@Override
		public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
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
			return ChunkGeneratorType.T21;
		}
	}
}
