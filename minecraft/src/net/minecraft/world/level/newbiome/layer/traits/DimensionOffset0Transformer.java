package net.minecraft.world.level.newbiome.layer.traits;

public interface DimensionOffset0Transformer extends DimensionTransformer {
	@Override
	default int getParentX(int i) {
		return i;
	}

	@Override
	default int getParentY(int i) {
		return i;
	}
}
