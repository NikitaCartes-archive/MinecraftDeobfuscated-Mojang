package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public abstract class NoiseAffectingStructureFeature<C extends FeatureConfiguration> extends StructureFeature<C> {
	public NoiseAffectingStructureFeature(Codec<C> codec, PieceGeneratorSupplier<C> pieceGeneratorSupplier) {
		super(codec, pieceGeneratorSupplier);
	}

	public NoiseAffectingStructureFeature(Codec<C> codec, PieceGeneratorSupplier<C> pieceGeneratorSupplier, PostPlacementProcessor postPlacementProcessor) {
		super(codec, pieceGeneratorSupplier, postPlacementProcessor);
	}

	@Override
	public BoundingBox adjustBoundingBox(BoundingBox boundingBox) {
		return super.adjustBoundingBox(boundingBox).inflatedBy(12);
	}
}
