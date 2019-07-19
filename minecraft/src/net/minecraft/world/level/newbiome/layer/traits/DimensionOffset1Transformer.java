package net.minecraft.world.level.newbiome.layer.traits;

public interface DimensionOffset1Transformer extends DimensionTransformer {
	@Override
	default int getParentX(int i) {
		return i - 1;
	}

	@Override
	default int getParentY(int i) {
		return i - 1;
	}
}
