package net.minecraft.world.level.levelgen.structure;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class NoiseAffectingStructureStart<C extends FeatureConfiguration> extends StructureStart<C> {
	public NoiseAffectingStructureStart(StructureFeature<C> structureFeature, ChunkPos chunkPos, int i, long l) {
		super(structureFeature, chunkPos, i, l);
	}

	@Override
	protected BoundingBox createBoundingBox() {
		return super.createBoundingBox().inflate(12);
	}
}
