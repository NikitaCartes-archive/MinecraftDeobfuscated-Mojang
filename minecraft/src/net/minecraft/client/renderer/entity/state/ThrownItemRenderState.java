package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ThrownItemRenderState extends EntityRenderState {
	@Nullable
	public BakedModel itemModel;
	public ItemStack item = ItemStack.EMPTY;
}
