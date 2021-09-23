package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;

public abstract class NoiseAffectingStructureFeature<C extends FeatureConfiguration> extends StructureFeature<C> {
	public NoiseAffectingStructureFeature(Codec<C> codec, PieceGenerator<C> pieceGenerator) {
		super(codec, pieceGenerator);
	}

	public NoiseAffectingStructureFeature(Codec<C> codec, PieceGenerator<C> pieceGenerator, PostPlacementProcessor postPlacementProcessor) {
		super(codec, pieceGenerator, postPlacementProcessor);
	}

	@Override
	public BoundingBox adjustBoundingBox(BoundingBox boundingBox) {
		return super.adjustBoundingBox(boundingBox).inflatedBy(12);
	}
}
