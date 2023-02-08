package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class DolphinCarryingItemLayer extends RenderLayer<Dolphin, DolphinModel<Dolphin>> {
	private final ItemInHandRenderer itemInHandRenderer;

	public DolphinCarryingItemLayer(RenderLayerParent<Dolphin, DolphinModel<Dolphin>> renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
		super(renderLayerParent);
		this.itemInHandRenderer = itemInHandRenderer;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Dolphin dolphin, float f, float g, float h, float j, float k, float l) {
		boolean bl = dolphin.getMainArm() == HumanoidArm.RIGHT;
		poseStack.pushPose();
		float m = 1.0F;
		float n = -1.0F;
		float o = Mth.abs(dolphin.getXRot()) / 60.0F;
		if (dolphin.getXRot() < 0.0F) {
			poseStack.translate(0.0F, 1.0F - o * 0.5F, -1.0F + o * 0.5F);
		} else {
			poseStack.translate(0.0F, 1.0F + o * 0.8F, -1.0F + o * 0.2F);
		}

		ItemStack itemStack = bl ? dolphin.getMainHandItem() : dolphin.getOffhandItem();
		this.itemInHandRenderer.renderItem(dolphin, itemStack, ItemDisplayContext.GROUND, false, poseStack, multiBufferSource, i);
		poseStack.popPose();
	}
}
