package net.minecraft.world.level.levelgen.structure.pieces;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

@FunctionalInterface
public interface PieceGeneratorSupplier<C extends FeatureConfiguration> {
	Optional<PieceGenerator<C>> createGenerator(PieceGeneratorSupplier.Context<C> context);

	static <C extends FeatureConfiguration> PieceGeneratorSupplier<C> simple(
		Predicate<PieceGeneratorSupplier.Context<C>> predicate, PieceGenerator<C> pieceGenerator
	) {
		Optional<PieceGenerator<C>> optional = Optional.of(pieceGenerator);
		return context -> predicate.test(context) ? optional : Optional.empty();
	}

	static <C extends FeatureConfiguration> Predicate<PieceGeneratorSupplier.Context<C>> checkForBiomeOnTop(Heightmap.Types types) {
		return context -> context.validBiomeOnTop(types);
	}

	public static record Context() {
		private final ChunkGenerator chunkGenerator;
		private final BiomeSource biomeSource;
		private final long seed;
		private final ChunkPos chunkPos;
		private final C config;
		private final LevelHeightAccessor heightAccessor;
		private final Predicate<Biome> validBiome;
		private final StructureManager structureManager;
		private final RegistryAccess registryAccess;

		public Context(
			ChunkGenerator chunkGenerator,
			BiomeSource biomeSource,
			long l,
			ChunkPos chunkPos,
			C featureConfiguration,
			LevelHeightAccessor levelHeightAccessor,
			Predicate<Biome> predicate,
			StructureManager structureManager,
			RegistryAccess registryAccess
		) {
			this.chunkGenerator = chunkGenerator;
			this.biomeSource = biomeSource;
			this.seed = l;
			this.chunkPos = chunkPos;
			this.config = featureConfiguration;
			this.heightAccessor = levelHeightAccessor;
			this.validBiome = predicate;
			this.structureManager = structureManager;
			this.registryAccess = registryAccess;
		}

		public boolean validBiomeOnTop(Heightmap.Types types) {
			int i = this.chunkPos.getMiddleBlockX();
			int j = this.chunkPos.getMiddleBlockZ();
			int k = this.chunkGenerator.getFirstOccupiedHeight(i, j, types, this.heightAccessor);
			Biome biome = this.chunkGenerator.getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(k), QuartPos.fromBlock(j));
			return this.validBiome.test(biome);
		}

		public int[] getCornerHeights(int i, int j, int k, int l) {
			return new int[]{
				this.chunkGenerator.getFirstOccupiedHeight(i, k, Heightmap.Types.WORLD_SURFACE_WG, this.heightAccessor),
				this.chunkGenerator.getFirstOccupiedHeight(i, k + l, Heightmap.Types.WORLD_SURFACE_WG, this.heightAccessor),
				this.chunkGenerator.getFirstOccupiedHeight(i + j, k, Heightmap.Types.WORLD_SURFACE_WG, this.heightAccessor),
				this.chunkGenerator.getFirstOccupiedHeight(i + j, k + l, Heightmap.Types.WORLD_SURFACE_WG, this.heightAccessor)
			};
		}

		public int getLowestY(int i, int j) {
			int k = this.chunkPos.getMinBlockX();
			int l = this.chunkPos.getMinBlockZ();
			int[] is = this.getCornerHeights(k, i, l, j);
			return Math.min(Math.min(is[0], is[1]), Math.min(is[2], is[3]));
		}
	}
}
