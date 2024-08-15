package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class TntRenderState extends EntityRenderState {
	public float fuseRemainingInTicks;
	@Nullable
	public BlockState blockState;
}
