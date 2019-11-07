package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class DolphinCarryingItemLayer extends RenderLayer<Dolphin, DolphinModel<Dolphin>> {
	public DolphinCarryingItemLayer(RenderLayerParent<Dolphin, DolphinModel<Dolphin>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Dolphin dolphin, float f, float g, float h, float j, float k, float l) {
		boolean bl = dolphin.getMainArm() == HumanoidArm.RIGHT;
		poseStack.pushPose();
		float m = 1.0F;
		float n = -1.0F;
		float o = Mth.abs(dolphin.xRot) / 60.0F;
		if (dolphin.xRot < 0.0F) {
			poseStack.translate(0.0, (double)(1.0F - o * 0.5F), (double)(-1.0F + o * 0.5F));
		} else {
			poseStack.translate(0.0, (double)(1.0F + o * 0.8F), (double)(-1.0F + o * 0.2F));
		}

		ItemStack itemStack = bl ? dolphin.getMainHandItem() : dolphin.getOffhandItem();
		Minecraft.getInstance().getItemInHandRenderer().renderItem(dolphin, itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, multiBufferSource, i);
		poseStack.popPose();
	}
}
