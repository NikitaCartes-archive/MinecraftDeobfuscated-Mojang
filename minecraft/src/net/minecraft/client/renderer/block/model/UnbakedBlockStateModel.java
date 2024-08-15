package net.minecraft.client.renderer.block.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public interface UnbakedBlockStateModel extends UnbakedModel {
	Object visualEqualityGroup(BlockState blockState);
}
