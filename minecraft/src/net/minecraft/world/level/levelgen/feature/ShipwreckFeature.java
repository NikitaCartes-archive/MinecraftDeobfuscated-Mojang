package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ShipwreckPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ShipwreckFeature extends RandomScatteredFeature<ShipwreckConfiguration> {
	public ShipwreckFeature(Function<Dynamic<?>, ? extends ShipwreckConfiguration> function) {
		super(function);
	}

	@Override
	public String getFeatureName() {
		return "Shipwreck";
	}

	@Override
	public int getLookupRange() {
		return 3;
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return ShipwreckFeature.FeatureStart::new;
	}

	@Override
	protected int getRandomSalt(ChunkGeneratorSettings chunkGeneratorSettings) {
		return 165745295;
	}

	@Override
	protected int getSpacing(ChunkGeneratorSettings chunkGeneratorSettings) {
		return chunkGeneratorSettings.getShipwreckSpacing();
	}

	@Override
	protected int getSeparation(ChunkGeneratorSettings chunkGeneratorSettings) {
		return chunkGeneratorSettings.getShipwreckSeparation();
	}

	public static class FeatureStart extends StructureStart {
		public FeatureStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			ShipwreckConfiguration shipwreckConfiguration = chunkGenerator.getStructureConfiguration(biome, Feature.SHIPWRECK);
			Rotation rotation = Rotation.getRandom(this.random);
			BlockPos blockPos = new BlockPos(i * 16, 90, j * 16);
			ShipwreckPieces.addPieces(structureManager, blockPos, rotation, this.pieces, this.random, shipwreckConfiguration);
			this.calculateBoundingBox();
		}
	}
}
