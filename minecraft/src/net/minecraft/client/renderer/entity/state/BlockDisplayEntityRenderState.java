package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Display;

@Environment(EnvType.CLIENT)
public class BlockDisplayEntityRenderState extends DisplayEntityRenderState {
	@Nullable
	public Display.BlockDisplay.BlockRenderState blockRenderState;

	@Override
	public boolean hasSubState() {
		return this.blockRenderState != null;
	}
}
