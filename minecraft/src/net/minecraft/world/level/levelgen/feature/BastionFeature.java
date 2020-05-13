package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.MultiJigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class BastionFeature extends StructureFeature<MultiJigsawConfiguration> {
	public BastionFeature(Function<Dynamic<?>, ? extends MultiJigsawConfiguration> function) {
		super(function);
	}

	@Override
	protected int getSpacing(ChunkGeneratorSettings chunkGeneratorSettings) {
		return chunkGeneratorSettings.getRareNetherStructureSpacing();
	}

	@Override
	protected int getSeparation(ChunkGeneratorSettings chunkGeneratorSettings) {
		return chunkGeneratorSettings.getRareNetherStructureSeparation();
	}

	@Override
	protected int getRandomSalt(ChunkGeneratorSettings chunkGeneratorSettings) {
		return chunkGeneratorSettings.getRareNetherStructureSalt();
	}

	@Override
	protected boolean isFeatureChunk(
		BiomeManager biomeManager, ChunkGenerator chunkGenerator, long l, WorldgenRandom worldgenRandom, int i, int j, Biome biome, ChunkPos chunkPos
	) {
		return worldgenRandom.nextInt(6) >= 2;
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return BastionFeature.FeatureStart::new;
	}

	@Override
	public String getFeatureName() {
		return "Bastion_Remnant";
	}

	@Override
	public int getLookupRange() {
		return 8;
	}

	public static class FeatureStart extends BeardedStructureStart {
		public FeatureStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			MultiJigsawConfiguration multiJigsawConfiguration = chunkGenerator.getStructureConfiguration(biome, Feature.BASTION_REMNANT);
			BlockPos blockPos = new BlockPos(i * 16, 33, j * 16);
			BastionPieces.addPieces(chunkGenerator, structureManager, blockPos, this.pieces, this.random, multiJigsawConfiguration);
			this.calculateBoundingBox();
		}
	}
}
