package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceSettings;
import net.minecraft.world.level.biome.NearestNeighborBiomeZoomer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.special.G01;
import net.minecraft.world.level.dimension.special.G02;
import net.minecraft.world.level.dimension.special.G03;
import net.minecraft.world.level.dimension.special.G04;
import net.minecraft.world.level.dimension.special.G05;
import net.minecraft.world.level.dimension.special.G06;
import net.minecraft.world.level.dimension.special.G07;
import net.minecraft.world.level.dimension.special.G08;
import net.minecraft.world.level.dimension.special.G09;
import net.minecraft.world.level.dimension.special.G10;
import net.minecraft.world.level.dimension.special.G11;
import net.minecraft.world.level.dimension.special.G12;
import net.minecraft.world.level.dimension.special.G13;
import net.minecraft.world.level.dimension.special.G14;
import net.minecraft.world.level.dimension.special.G15;
import net.minecraft.world.level.dimension.special.G16;
import net.minecraft.world.level.dimension.special.G17;
import net.minecraft.world.level.dimension.special.G18;
import net.minecraft.world.level.dimension.special.G19;
import net.minecraft.world.level.dimension.special.G20;
import net.minecraft.world.level.dimension.special.G21;
import net.minecraft.world.level.dimension.special.G22;
import net.minecraft.world.level.dimension.special.G23;
import net.minecraft.world.level.dimension.special.G24;
import net.minecraft.world.level.dimension.special.G25;
import net.minecraft.world.level.dimension.special.G26;
import net.minecraft.world.level.dimension.special.G27;
import net.minecraft.world.level.dimension.special.G28;
import net.minecraft.world.level.dimension.special.G29;
import net.minecraft.world.level.dimension.special.G30;
import net.minecraft.world.level.dimension.special.G31;
import net.minecraft.world.level.dimension.special.G32;
import net.minecraft.world.level.dimension.special.G33;
import net.minecraft.world.level.dimension.special.G34;
import net.minecraft.world.level.dimension.special.G35;
import net.minecraft.world.level.dimension.special.G36;
import net.minecraft.world.level.dimension.special.G37;
import net.minecraft.world.level.dimension.special.G38;
import net.minecraft.world.level.dimension.special.G39;
import net.minecraft.world.level.dimension.special.G40;
import net.minecraft.world.level.dimension.special.LastPage;
import net.minecraft.world.level.levelgen.NetherGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.TheEndGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.phys.Vec3;

public class DimensionGenerator {
	private static final BiomeZoomer[] ZOOMERS = new BiomeZoomer[]{
		FuzzyOffsetBiomeZoomer.INSTANCE, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, NearestNeighborBiomeZoomer.INSTANCE
	};
	private static final Int2ObjectMap<IntFunction<DimensionType>> CUSTOMS = new Int2ObjectOpenHashMap<>();

	private static IntFunction<DimensionType> createStandardGenerator(
		BiFunction<Level, DimensionType, ? extends Dimension> biFunction, boolean bl, BiomeZoomer biomeZoomer
	) {
		return i -> new DimensionType(i, "_" + i, "DIM" + i, biFunction, bl, biomeZoomer);
	}

	private static IntFunction<DimensionType> createNonStandardGenerator(
		BiFunction<Level, DimensionType, ? extends Dimension> biFunction, boolean bl, BiomeZoomer biomeZoomer
	) {
		return i -> new DimensionType(i, "_" + i, "DIM" + i, biFunction, bl, biomeZoomer) {
				@Override
				public boolean requirePortalGen() {
					return true;
				}
			};
	}

	public static DimensionType perform(int i) {
		IntFunction<DimensionType> intFunction = CUSTOMS.get(i);
		if (intFunction != null) {
			return (DimensionType)intFunction.apply(i);
		} else {
			WorldgenRandom worldgenRandom = new WorldgenRandom((long)i);
			BiomeZoomer biomeZoomer = ZOOMERS[worldgenRandom.nextInt(ZOOMERS.length)];
			boolean bl = worldgenRandom.nextBoolean();
			int j = worldgenRandom.nextInt();
			return new DimensionType(
				i, "_" + i, "DIM" + i, (level, dimensionType) -> DimensionGenerator.GeneratedDimension.create(level, dimensionType, j), bl, biomeZoomer
			);
		}
	}

	static {
		CUSTOMS.put(741472677, createStandardGenerator(G01::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(236157810, createStandardGenerator(G02::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(1896587401, createStandardGenerator(G03::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(726931095, createStandardGenerator(G04::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(233542201, createStandardGenerator(G05::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(669175628, createStandardGenerator(G06::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(1929426645, createStandardGenerator(G07::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(378547252, createStandardGenerator(G08::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(94341406, createStandardGenerator(G09::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(1174283440, createStandardGenerator(G10::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(1210674279, createStandardGenerator(G11::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(344885676, createStandardGenerator(G12::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(31674686, createNonStandardGenerator(G13::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(2114493792, createStandardGenerator(G14.create(new Vector3f(1.0F, 0.0F, 0.0F)), true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(1143264807, createStandardGenerator(G14.create(new Vector3f(0.0F, 1.0F, 0.0F)), true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(1049823113, createStandardGenerator(G14.create(new Vector3f(0.0F, 0.0F, 1.0F)), true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(1011847535, createStandardGenerator(G15::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(1902968744, createStandardGenerator(G16::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(264458659, createStandardGenerator(G17::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(1201319931, createStandardGenerator(G18::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(1113696725, createStandardGenerator(G19::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(1384344230, createStandardGenerator(G20::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(214387762, createStandardGenerator(G21::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(1098962767, createStandardGenerator(G22::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(927632079, createStandardGenerator(G23::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(307219718, createStandardGenerator(G24::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(545072168, createStandardGenerator(G25::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(1834117187, createStandardGenerator(G26::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(661885389, createStandardGenerator(G27::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(1036032341, createStandardGenerator(G28::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(484336196, createStandardGenerator(G29::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(1059552697, createStandardGenerator(G30::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(907661935, createStandardGenerator(G31::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(1141490659, createStandardGenerator(G32::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(1028465021, createStandardGenerator(G33::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(2003598857, createStandardGenerator(G34::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(985130845, createStandardGenerator(G35::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(107712651, createStandardGenerator(G36::new, true, NearestNeighborBiomeZoomer.INSTANCE));
		CUSTOMS.put(251137100, createStandardGenerator(G37::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(1537997313, createStandardGenerator(G38::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(1916276638, createStandardGenerator(G39::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(894945615, createStandardGenerator(G40::new, true, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE));
		CUSTOMS.put(1791460938, createStandardGenerator(LastPage::new, true, NearestNeighborBiomeZoomer.INSTANCE));
	}

	static class GeneratedDimension extends Dimension {
		private final boolean fixedTime;
		private final float fixedTimeValue;
		private final double ticksPerDay;
		private final boolean naturalDimension;
		private final boolean endSky;
		private final float sunSize;
		private final Vector3f sunTint;
		private final float moonSize;
		private final Vector3f moonTint;
		private final Vec3 fogA;
		private final Vec3 fogB;
		private final boolean foggy;
		private final Supplier<ChunkGenerator> generatorProvider;
		@Nullable
		private final Object2FloatMap<Direction> customShade;
		private final int cloudHeight;
		@Nullable
		private final Vector3f[] lightmapNoise;
		@Nullable
		private final Vector3f[] tintVariants;

		public static Dimension create(Level level, DimensionType dimensionType, int i) {
			return new DimensionGenerator.GeneratedDimension(level, dimensionType, new WorldgenRandom((long)i));
		}

		private GeneratedDimension(Level level, DimensionType dimensionType, WorldgenRandom worldgenRandom) {
			super(level, dimensionType, worldgenRandom.nextFloat());
			this.ultraWarm = worldgenRandom.nextInt(5) == 0;
			this.hasCeiling = worldgenRandom.nextBoolean();
			this.fixedTime = worldgenRandom.nextBoolean();
			this.fixedTimeValue = worldgenRandom.nextFloat();
			this.naturalDimension = worldgenRandom.nextBoolean();
			this.endSky = worldgenRandom.nextInt(8) == 0;
			this.ticksPerDay = Math.max(100.0, worldgenRandom.nextGaussian() * 3.0 * 24000.0);
			this.fogA = new Vec3(worldgenRandom.nextDouble(), worldgenRandom.nextDouble(), worldgenRandom.nextDouble());
			this.fogB = new Vec3(worldgenRandom.nextDouble(), worldgenRandom.nextDouble(), worldgenRandom.nextDouble());
			this.foggy = worldgenRandom.nextBoolean();
			this.sunSize = (float)Math.max(5.0, 30.0 * (1.0 + 4.0 * worldgenRandom.nextGaussian()));
			this.moonSize = (float)Math.max(5.0, 20.0 * (1.0 + 4.0 * worldgenRandom.nextGaussian()));
			this.sunTint = this.generateTint(worldgenRandom);
			this.moonTint = this.generateTint(worldgenRandom);
			this.cloudHeight = worldgenRandom.nextInt(255);
			MultiNoiseBiomeSourceSettings multiNoiseBiomeSourceSettings = new MultiNoiseBiomeSourceSettings((long)worldgenRandom.nextInt());
			Map<Biome, List<Biome.ClimateParameters>> map = (Map<Biome, List<Biome.ClimateParameters>>)IntStream.range(2, worldgenRandom.nextInt(15))
				.mapToObj(i -> Math.abs(worldgenRandom.nextInt()))
				.collect(Collectors.toMap(Registry.BIOME::byId, integer -> Biome.ClimateParameters.randomList(worldgenRandom)));

			while (worldgenRandom.nextBoolean()) {
				map.put(Biomes.getRandomVanillaBiome(worldgenRandom), Biome.ClimateParameters.randomList(worldgenRandom));
			}

			multiNoiseBiomeSourceSettings.setBiomes(map);
			BiomeSource biomeSource = BiomeSourceType.MULTI_NOISE.create(multiNoiseBiomeSourceSettings);
			if (worldgenRandom.nextInt(7) == 0) {
				this.customShade = new Object2FloatOpenHashMap<>();

				for (Direction direction : Direction.values()) {
					this.customShade.put(direction, (float)Mth.clamp((double)super.getBlockShade(direction, true) + worldgenRandom.nextGaussian(), 0.0, 1.0));
				}
			} else {
				this.customShade = null;
			}

			if (worldgenRandom.nextInt(4) == 0) {
				this.lightmapNoise = this.generateLightmapNoise(worldgenRandom);
			} else {
				this.lightmapNoise = null;
			}

			if (worldgenRandom.nextInt(3) == 0) {
				this.tintVariants = this.generateTintVariants(worldgenRandom);
			} else {
				this.tintVariants = null;
			}

			this.generatorProvider = createGeneratorProvider(level, worldgenRandom, biomeSource);
		}

		private Vector3f generateTint(WorldgenRandom worldgenRandom) {
			return worldgenRandom.nextBoolean()
				? new Vector3f(worldgenRandom.nextFloat(), worldgenRandom.nextFloat(), worldgenRandom.nextFloat())
				: new Vector3f(1.0F, 1.0F, 1.0F);
		}

		private Vector3f[] generateTintVariants(Random random) {
			int i = random.nextInt(6) + 2;
			Vector3f[] vector3fs = new Vector3f[i];

			for (int j = 0; j < i; j++) {
				vector3fs[j] = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
			}

			return vector3fs;
		}

		private static float getNoise(PerlinNoise perlinNoise, int i, int j) {
			return (float)perlinNoise.getValue((double)i, (double)j, 0.0);
		}

		private Vector3f[] generateLightmapNoise(WorldgenRandom worldgenRandom) {
			Vector3f[] vector3fs = new Vector3f[256];
			PerlinNoise perlinNoise = new PerlinNoise(worldgenRandom, IntStream.rangeClosed(-3, 0));
			PerlinNoise perlinNoise2 = new PerlinNoise(worldgenRandom, IntStream.rangeClosed(-2, 4));
			PerlinNoise perlinNoise3 = new PerlinNoise(worldgenRandom, IntStream.rangeClosed(-5, 0));

			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 16; j++) {
					Vector3f vector3f = new Vector3f(getNoise(perlinNoise, i, j), getNoise(perlinNoise2, i, j), getNoise(perlinNoise3, i, j));
					vector3fs[i * 16 + j] = vector3f;
				}
			}

			return vector3fs;
		}

		private static Supplier<ChunkGenerator> createGeneratorProvider(Level level, Random random, BiomeSource biomeSource) {
			int i = random.nextInt();
			switch (random.nextInt(3)) {
				case 0:
					return () -> ChunkGeneratorType.SURFACE.create(level, biomeSource, new OverworldGeneratorSettings(new WorldgenRandom((long)i)));
				case 1:
					return () -> ChunkGeneratorType.CAVES.create(level, biomeSource, new NetherGeneratorSettings(new WorldgenRandom((long)i)));
				default:
					return () -> ChunkGeneratorType.FLOATING_ISLANDS.create(level, biomeSource, new TheEndGeneratorSettings(new WorldgenRandom((long)i)));
			}
		}

		@Override
		public ChunkGenerator<?> createRandomLevelGenerator() {
			return (ChunkGenerator<?>)this.generatorProvider.get();
		}

		@Nullable
		@Override
		public BlockPos getSpawnPosInChunk(ChunkPos chunkPos, boolean bl) {
			return NormalDimension.getSpawnPosInChunkI(this.level, chunkPos, bl);
		}

		@Nullable
		@Override
		public BlockPos getValidSpawnPosition(int i, int j, boolean bl) {
			return NormalDimension.getValidSpawnPositionI(this.level, i, j, bl);
		}

		@Override
		public float getTimeOfDay(long l, float f) {
			return this.fixedTime ? this.fixedTimeValue : NormalDimension.getTimeOfDayI(l, this.ticksPerDay);
		}

		@Override
		public boolean isNaturalDimension() {
			return this.naturalDimension;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
			return vec3.multiply((double)f * this.fogA.x + this.fogB.x, (double)f * this.fogA.y + this.fogB.y, (double)f * this.fogA.z + this.fogB.z);
		}

		@Override
		public boolean mayRespawn() {
			return false;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public boolean isFoggyAt(int i, int j) {
			return this.foggy;
		}

		@Override
		public float getBlockShade(Direction direction, boolean bl) {
			return this.customShade != null && bl ? this.customShade.getFloat(direction) : super.getBlockShade(direction, bl);
		}

		@Environment(EnvType.CLIENT)
		@Override
		public void modifyLightmapColor(int i, int j, Vector3f vector3f) {
			if (this.lightmapNoise != null) {
				vector3f.add(this.lightmapNoise[j * 16 + i]);
				vector3f.clamp(0.0F, 1.0F);
			}
		}

		@Environment(EnvType.CLIENT)
		@Override
		public Vector3f getExtraTint(BlockState blockState, BlockPos blockPos) {
			if (this.tintVariants == null) {
				return super.getExtraTint(blockState, blockPos);
			} else {
				int i = Block.BLOCK_STATE_REGISTRY.getId(blockState);
				return this.tintVariants[i % this.tintVariants.length];
			}
		}

		@Environment(EnvType.CLIENT)
		@Override
		public <T extends LivingEntity> Vector3f getEntityExtraTint(T livingEntity) {
			if (this.tintVariants == null) {
				return super.getEntityExtraTint(livingEntity);
			} else {
				int i = Registry.ENTITY_TYPE.getId(livingEntity.getType());
				return this.tintVariants[i % this.tintVariants.length];
			}
		}

		@Override
		public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
			T object = dynamicOps.createMap(
				(Map<T, T>)Stream.of(Direction.values())
					.collect(
						ImmutableMap.toImmutableMap(
							direction -> dynamicOps.createString(direction.getName()), direction -> dynamicOps.createDouble((double)this.getBlockShade(direction, true))
						)
					)
			);
			return super.serialize(dynamicOps)
				.merge(
					new Dynamic<>(
						dynamicOps,
						dynamicOps.createMap(
							ImmutableMap.<T, T>builder()
								.put(dynamicOps.createString("foggy"), dynamicOps.createBoolean(this.foggy))
								.put(dynamicOps.createString("fogA"), dynamicOps.createList(Stream.of(this.fogA.x, this.fogA.y, this.fogA.z).map(dynamicOps::createDouble)))
								.put(dynamicOps.createString("fogB"), dynamicOps.createList(Stream.of(this.fogB.x, this.fogB.y, this.fogB.z).map(dynamicOps::createDouble)))
								.put(dynamicOps.createString("tickPerDay"), dynamicOps.createDouble(this.ticksPerDay))
								.put(dynamicOps.createString("shade"), object)
								.build()
						)
					)
				);
		}

		@Environment(EnvType.CLIENT)
		@Override
		public boolean isEndSky() {
			return this.endSky;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public float getSunSize() {
			return this.sunSize;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public Vector3f getSunTint() {
			return this.sunTint;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public float getMoonSize() {
			return this.moonSize;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public Vector3f getMoonTint() {
			return this.moonTint;
		}

		@Environment(EnvType.CLIENT)
		@Override
		public float getCloudHeight() {
			return (float)this.cloudHeight;
		}
	}
}
