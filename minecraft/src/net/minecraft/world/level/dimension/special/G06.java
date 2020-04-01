package net.minecraft.world.level.dimension.special;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.ChanceRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShapeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.RainbowBlockProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.phys.Vec3;

public class G06 extends SpecialDimensionBase {
	public G06(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 1.0F);
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		return new G06.Generator(this.level, fixedBiome(Biomes.SHAPES), NoneGeneratorSettings.INSTANCE);
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return NormalDimension.getTimeOfDayI(l, 3000.0);
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
		public int getSpawnHeight() {
			return 30;
		}

		@Override
		public void fillFromNoise(LevelAccessor levelAccessor, ChunkAccess chunkAccess) {
		}

		@Override
		public void applyCarvers(BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
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
			return ChunkGeneratorType.T06;
		}
	}

	public static class ShapesBiome extends Biome {
		public ShapesBiome() {
			super(
				new Biome.BiomeBuilder()
					.surfaceBuilder(SurfaceBuilder.NOPE, SurfaceBuilder.CONFIG_STONE)
					.precipitation(Biome.Precipitation.NONE)
					.biomeCategory(Biome.BiomeCategory.NONE)
					.depth(0.1F)
					.scale(0.2F)
					.temperature(0.5F)
					.downfall(0.5F)
					.specialEffects(
						new BiomeSpecialEffects.Builder()
							.waterColor(52713007)
							.waterFogColor(1876255554)
							.fogColor(12638463)
							.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
							.build()
					)
					.parent(null)
			);
			Random random = new Random(12461L);
			List<ConfiguredFeature<?, ?>> list = Lists.<ConfiguredFeature<?, ?>>newArrayList();
			Stream.of(ColoredBlocks.COLORED_BLOCKS)
				.flatMap(Stream::of)
				.forEach(
					block -> {
						float fx = 1.0F + random.nextFloat() * 5.0F;
						float gx = Math.min(fx + random.nextFloat() * 10.0F, 15.0F);
						list.add(
							Feature.SHAPE
								.configured(
									new ShapeConfiguration(new SimpleStateProvider(block.defaultBlockState()), Util.randomObject(random, ShapeConfiguration.Metric.values()), fx, gx)
								)
						);
					}
				);

			for (Block[] blocks : ColoredBlocks.COLORED_BLOCKS) {
				ImmutableList<BlockState> immutableList = (ImmutableList<BlockState>)Stream.of(blocks)
					.map(Block::defaultBlockState)
					.collect(ImmutableList.toImmutableList());

				for (ShapeConfiguration.Metric metric : ShapeConfiguration.Metric.values()) {
					float f = 1.0F + random.nextFloat() * 5.0F;
					float g = Math.min(f + random.nextFloat() * 10.0F, 15.0F);
					list.add(Feature.SHAPE.configured(new ShapeConfiguration(new RainbowBlockProvider(immutableList), metric, f, g)));
				}
			}

			float h = 1.0F / (float)list.size();
			this.addFeature(
				GenerationStep.Decoration.SURFACE_STRUCTURES,
				Feature.RANDOM_SELECTOR
					.configured(
						new RandomFeatureConfiguration(
							(List<WeightedConfiguredFeature<?>>)list.stream()
								.map(configuredFeature -> new WeightedConfiguredFeature(configuredFeature, h))
								.collect(Collectors.toList()),
							Util.randomObject(random, list)
						)
					)
					.decorated(FeatureDecorator.CHANCE_RANGE.configured(new ChanceRangeDecoratorConfiguration(1.0F, 16, 16, 128)))
			);
		}
	}
}
