package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.Products.P4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
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
	public static final Codec<Structure> DIRECT_CODEC = Registry.STRUCTURE_TYPES.byNameCodec().dispatch(Structure::type, StructureType::codec);
	public static final Codec<Holder<Structure>> CODEC = RegistryFileCodec.create(Registry.STRUCTURE_REGISTRY, DIRECT_CODEC);
	private final HolderSet<Biome> biomes;
	private final Map<MobCategory, StructureSpawnOverride> spawnOverrides;
	private final GenerationStep.Decoration step;
	private final boolean adaptNoise;

	public static <S extends Structure> P4<Mu<S>, HolderSet<Biome>, Map<MobCategory, StructureSpawnOverride>, GenerationStep.Decoration, Boolean> codec(
		Instance<S> instance
	) {
		return instance.group(
			RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY).fieldOf("biomes").forGetter(Structure::biomes),
			Codec.simpleMap(MobCategory.CODEC, StructureSpawnOverride.CODEC, StringRepresentable.keys(MobCategory.values()))
				.fieldOf("spawn_overrides")
				.forGetter(Structure::spawnOverrides),
			GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(Structure::step),
			Codec.BOOL.optionalFieldOf("adapt_noise", Boolean.valueOf(false)).forGetter(Structure::adaptNoise)
		);
	}

	protected Structure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, boolean bl) {
		this.biomes = holderSet;
		this.spawnOverrides = map;
		this.step = decoration;
		this.adaptNoise = bl;
	}

	public HolderSet<Biome> biomes() {
		return this.biomes;
	}

	public Map<MobCategory, StructureSpawnOverride> spawnOverrides() {
		return this.spawnOverrides;
	}

	public GenerationStep.Decoration step() {
		return this.step;
	}

	public boolean adaptNoise() {
		return this.adaptNoise;
	}

	public BoundingBox adjustBoundingBox(BoundingBox boundingBox) {
		return this.adaptNoise() ? boundingBox.inflatedBy(12) : boundingBox;
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
			StructurePiecesBuilder structurePiecesBuilder = new StructurePiecesBuilder();
			((Structure.GenerationStub)optional.get()).generator().accept(structurePiecesBuilder);
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
		Random random,
		BoundingBox boundingBox,
		ChunkPos chunkPos,
		PiecesContainer piecesContainer
	) {
	}

	public static int[] getCornerHeights(Structure.GenerationContext generationContext, int i, int j, int k, int l) {
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

	public static int getLowestY(Structure.GenerationContext generationContext, int i, int j) {
		ChunkPos chunkPos = generationContext.chunkPos();
		int k = chunkPos.getMinBlockX();
		int l = chunkPos.getMinBlockZ();
		int[] is = getCornerHeights(generationContext, k, i, l, j);
		return Math.min(Math.min(is[0], is[1]), Math.min(is[2], is[3]));
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

	public static record GenerationStub(BlockPos position, Consumer<StructurePiecesBuilder> generator) {
	}
}
