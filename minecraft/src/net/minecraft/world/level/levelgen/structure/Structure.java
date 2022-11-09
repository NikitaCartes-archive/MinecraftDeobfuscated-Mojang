package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class Structure {
	public static final Codec<Structure> DIRECT_CODEC = BuiltInRegistries.STRUCTURE_TYPE.byNameCodec().dispatch(Structure::type, StructureType::codec);
	public static final Codec<Holder<Structure>> CODEC = RegistryFileCodec.create(Registries.STRUCTURE, DIRECT_CODEC);
	protected final Structure.StructureSettings settings;

	public static <S extends Structure> RecordCodecBuilder<S, Structure.StructureSettings> settingsCodec(Instance<S> instance) {
		return Structure.StructureSettings.CODEC.forGetter(structure -> structure.settings);
	}

	public static <S extends Structure> Codec<S> simpleCodec(Function<Structure.StructureSettings, S> function) {
		return RecordCodecBuilder.create(instance -> instance.group(settingsCodec(instance)).apply(instance, function));
	}

	protected Structure(Structure.StructureSettings structureSettings) {
		this.settings = structureSettings;
	}

	public HolderSet<Biome> biomes() {
		return this.settings.biomes;
	}

	public Map<MobCategory, StructureSpawnOverride> spawnOverrides() {
		return this.settings.spawnOverrides;
	}

	public GenerationStep.Decoration step() {
		return this.settings.step;
	}

	public TerrainAdjustment terrainAdaptation() {
		return this.settings.terrainAdaptation;
	}

	public BoundingBox adjustBoundingBox(BoundingBox boundingBox) {
		return this.terrainAdaptation() != TerrainAdjustment.NONE ? boundingBox.inflatedBy(12) : boundingBox;
	}

	public StructureStart generate(
		RegistryAccess registryAccess,
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		RandomState randomState,
		StructureTemplateManager structureTemplateManager,
		long l,
		ChunkPos chunkPos,
		int i,
		LevelHeightAccessor levelHeightAccessor,
		Predicate<Holder<Biome>> predicate
	) {
		Optional<Structure.GenerationStub> optional = this.findGenerationPoint(
			new Structure.GenerationContext(
				registryAccess, chunkGenerator, biomeSource, randomState, structureTemplateManager, l, chunkPos, levelHeightAccessor, predicate
			)
		);
		if (optional.isPresent() && isValidBiome((Structure.GenerationStub)optional.get(), chunkGenerator, randomState, predicate)) {
			StructurePiecesBuilder structurePiecesBuilder = ((Structure.GenerationStub)optional.get()).getPiecesBuilder();
			StructureStart structureStart = new StructureStart(this, chunkPos, i, structurePiecesBuilder.build());
			if (structureStart.isValid()) {
				return structureStart;
			}
		}

		return StructureStart.INVALID_START;
	}

	protected static Optional<Structure.GenerationStub> onTopOfChunkCenter(
		Structure.GenerationContext generationContext, Heightmap.Types types, Consumer<StructurePiecesBuilder> consumer
	) {
		ChunkPos chunkPos = generationContext.chunkPos();
		int i = chunkPos.getMiddleBlockX();
		int j = chunkPos.getMiddleBlockZ();
		int k = generationContext.chunkGenerator().getFirstOccupiedHeight(i, j, types, generationContext.heightAccessor(), generationContext.randomState());
		return Optional.of(new Structure.GenerationStub(new BlockPos(i, k, j), consumer));
	}

	private static boolean isValidBiome(
		Structure.GenerationStub generationStub, ChunkGenerator chunkGenerator, RandomState randomState, Predicate<Holder<Biome>> predicate
	) {
		BlockPos blockPos = generationStub.position();
		return predicate.test(
			chunkGenerator.getBiomeSource()
				.getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getY()), QuartPos.fromBlock(blockPos.getZ()), randomState.sampler())
		);
	}

	public void afterPlace(
		WorldGenLevel worldGenLevel,
		StructureManager structureManager,
		ChunkGenerator chunkGenerator,
		RandomSource randomSource,
		BoundingBox boundingBox,
		ChunkPos chunkPos,
		PiecesContainer piecesContainer
	) {
	}

	private static int[] getCornerHeights(Structure.GenerationContext generationContext, int i, int j, int k, int l) {
		ChunkGenerator chunkGenerator = generationContext.chunkGenerator();
		LevelHeightAccessor levelHeightAccessor = generationContext.heightAccessor();
		RandomState randomState = generationContext.randomState();
		return new int[]{
			chunkGenerator.getFirstOccupiedHeight(i, k, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState),
			chunkGenerator.getFirstOccupiedHeight(i, k + l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState),
			chunkGenerator.getFirstOccupiedHeight(i + j, k, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState),
			chunkGenerator.getFirstOccupiedHeight(i + j, k + l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState)
		};
	}

	protected static int getLowestY(Structure.GenerationContext generationContext, int i, int j) {
		ChunkPos chunkPos = generationContext.chunkPos();
		int k = chunkPos.getMinBlockX();
		int l = chunkPos.getMinBlockZ();
		return getLowestY(generationContext, k, l, i, j);
	}

	protected static int getLowestY(Structure.GenerationContext generationContext, int i, int j, int k, int l) {
		int[] is = getCornerHeights(generationContext, i, k, j, l);
		return Math.min(Math.min(is[0], is[1]), Math.min(is[2], is[3]));
	}

	@Deprecated
	protected BlockPos getLowestYIn5by5BoxOffset7Blocks(Structure.GenerationContext generationContext, Rotation rotation) {
		int i = 5;
		int j = 5;
		if (rotation == Rotation.CLOCKWISE_90) {
			i = -5;
		} else if (rotation == Rotation.CLOCKWISE_180) {
			i = -5;
			j = -5;
		} else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
			j = -5;
		}

		ChunkPos chunkPos = generationContext.chunkPos();
		int k = chunkPos.getBlockX(7);
		int l = chunkPos.getBlockZ(7);
		return new BlockPos(k, getLowestY(generationContext, k, l, i, j), l);
	}

	public abstract Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext);

	public abstract StructureType<?> type();

	public static record GenerationContext(
		RegistryAccess registryAccess,
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		RandomState randomState,
		StructureTemplateManager structureTemplateManager,
		WorldgenRandom random,
		long seed,
		ChunkPos chunkPos,
		LevelHeightAccessor heightAccessor,
		Predicate<Holder<Biome>> validBiome
	) {
		public GenerationContext(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			BiomeSource biomeSource,
			RandomState randomState,
			StructureTemplateManager structureTemplateManager,
			long l,
			ChunkPos chunkPos,
			LevelHeightAccessor levelHeightAccessor,
			Predicate<Holder<Biome>> predicate
		) {
			this(
				registryAccess, chunkGenerator, biomeSource, randomState, structureTemplateManager, makeRandom(l, chunkPos), l, chunkPos, levelHeightAccessor, predicate
			);
		}

		private static WorldgenRandom makeRandom(long l, ChunkPos chunkPos) {
			WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
			worldgenRandom.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
			return worldgenRandom;
		}
	}

	public static record GenerationStub(BlockPos position, Either<Consumer<StructurePiecesBuilder>, StructurePiecesBuilder> generator) {
		public GenerationStub(BlockPos blockPos, Consumer<StructurePiecesBuilder> consumer) {
			this(blockPos, Either.left(consumer));
		}

		public StructurePiecesBuilder getPiecesBuilder() {
			return this.generator.map(consumer -> {
				StructurePiecesBuilder structurePiecesBuilder = new StructurePiecesBuilder();
				consumer.accept(structurePiecesBuilder);
				return structurePiecesBuilder;
			}, structurePiecesBuilder -> structurePiecesBuilder);
		}
	}

	public static record StructureSettings(
		HolderSet<Biome> biomes, Map<MobCategory, StructureSpawnOverride> spawnOverrides, GenerationStep.Decoration step, TerrainAdjustment terrainAdaptation
	) {
		public static final MapCodec<Structure.StructureSettings> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(Structure.StructureSettings::biomes),
						Codec.simpleMap(MobCategory.CODEC, StructureSpawnOverride.CODEC, StringRepresentable.keys(MobCategory.values()))
							.fieldOf("spawn_overrides")
							.forGetter(Structure.StructureSettings::spawnOverrides),
						GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(Structure.StructureSettings::step),
						TerrainAdjustment.CODEC.optionalFieldOf("terrain_adaptation", TerrainAdjustment.NONE).forGetter(Structure.StructureSettings::terrainAdaptation)
					)
					.apply(instance, Structure.StructureSettings::new)
		);
	}
}
