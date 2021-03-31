package net.minecraft.world.level.levelgen.surfacebuilders;

import net.minecraft.world.level.block.state.BlockState;

public interface SurfaceBuilderConfiguration {
	BlockState getTopMaterial();

	BlockState getUnderMaterial();

	BlockState getUnderwaterMaterial();
}
