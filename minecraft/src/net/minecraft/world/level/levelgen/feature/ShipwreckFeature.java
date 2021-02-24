package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
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
		public FeatureStart(StructureFeature<ShipwreckConfiguration> structureFeature, ChunkPos chunkPos, BoundingBox boundingBox, int i, long l) {
			super(structureFeature, chunkPos, boundingBox, i, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			Biome biome,
			ShipwreckConfiguration shipwreckConfiguration,
			LevelHeightAccessor levelHeightAccessor
		) {
			Rotation rotation = Rotation.getRandom(this.random);
			BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), 90, chunkPos.getMinBlockZ());
			ShipwreckPieces.addPieces(structureManager, blockPos, rotation, this.pieces, this.random, shipwreckConfiguration);
			this.calculateBoundingBox();
		}
	}
}
