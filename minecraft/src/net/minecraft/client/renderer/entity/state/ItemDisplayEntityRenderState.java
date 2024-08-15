package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Display;

@Environment(EnvType.CLIENT)
public class ItemDisplayEntityRenderState extends DisplayEntityRenderState {
	@Nullable
	public Display.ItemDisplay.ItemRenderState itemRenderState;
	@Nullable
	public BakedModel itemModel;

	@Override
	public boolean hasSubState() {
		return this.itemRenderState != null && this.itemModel != null;
	}
}
