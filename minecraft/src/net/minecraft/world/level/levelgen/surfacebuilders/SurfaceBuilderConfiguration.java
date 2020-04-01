package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.block.state.BlockState;

public interface SurfaceBuilderConfiguration {
	BlockState getTopMaterial();

	BlockState getUnderMaterial();

	<T> Dynamic<T> serialize(DynamicOps<T> dynamicOps);
}
