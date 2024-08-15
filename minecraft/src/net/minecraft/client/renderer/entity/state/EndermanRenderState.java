package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class EndermanRenderState extends HumanoidRenderState {
	public boolean isCreepy;
	@Nullable
	public BlockState carriedBlock;
}
