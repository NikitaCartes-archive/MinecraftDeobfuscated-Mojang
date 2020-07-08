package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ShipwreckPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ShipwreckFeature extends StructureFeature<ShipwreckConfiguration> {
	public ShipwreckFeature(Codec<ShipwreckConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureFeature.StructureStartFactory<ShipwreckConfiguration> getStartFactory() {
		return ShipwreckFeature.FeatureStart::new;
	}

	public static class FeatureStart extends StructureStart<ShipwreckConfiguration> {
		public FeatureStart(StructureFeature<ShipwreckConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			int i,
			int j,
			Biome biome,
			ShipwreckConfiguration shipwreckConfiguration
		) {
			Rotation rotation = Rotation.getRandom(this.random);
			BlockPos blockPos = new BlockPos(i * 16, 90, j * 16);
			ShipwreckPieces.addPieces(structureManager, blockPos, rotation, this.pieces, this.random, shipwreckConfiguration);
			this.calculateBoundingBox();
		}
	}
}
