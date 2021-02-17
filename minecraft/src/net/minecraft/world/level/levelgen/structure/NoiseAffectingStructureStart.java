package net.minecraft.world.level.levelgen.structure;

import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class NoiseAffectingStructureStart<C extends FeatureConfiguration> extends StructureStart<C> {
	public NoiseAffectingStructureStart(StructureFeature<C> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
		super(structureFeature, i, j, boundingBox, k, l);
	}

	@Override
	protected void calculateBoundingBox() {
		super.calculateBoundingBox();
		int i = 12;
		this.boundingBox.x0 -= 12;
		this.boundingBox.y0 -= 12;
		this.boundingBox.z0 -= 12;
		this.boundingBox.x1 += 12;
		this.boundingBox.y1 += 12;
		this.boundingBox.z1 += 12;
	}
}
