package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class FoxHeldItemLayer extends RenderLayer<Fox, FoxModel<Fox>> {
	public FoxHeldItemLayer(RenderLayerParent<Fox, FoxModel<Fox>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Fox fox, float f, float g, float h, float j, float k, float l) {
		boolean bl = fox.isSleeping();
		boolean bl2 = fox.isBaby();
		poseStack.pushPose();
		if (bl2) {
			float m = 0.75F;
			poseStack.scale(0.75F, 0.75F, 0.75F);
			poseStack.translate(0.0, 0.5, 0.209375F);
		}

		poseStack.translate(
			(double)(this.getParentModel().head.x / 16.0F), (double)(this.getParentModel().head.y / 16.0F), (double)(this.getParentModel().head.z / 16.0F)
		);
		float m = fox.getHeadRollAngle(h);
		poseStack.mulPose(Vector3f.ZP.rotation(m));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(k));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(l));
		if (fox.isBaby()) {
			if (bl) {
				poseStack.translate(0.4F, 0.26F, 0.15F);
			} else {
				poseStack.translate(0.06F, 0.26F, -0.5);
			}
		} else if (bl) {
			poseStack.translate(0.46F, 0.26F, 0.22F);
		} else {
			poseStack.translate(0.06F, 0.27F, -0.5);
		}

		poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
		if (bl) {
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
		}

		ItemStack itemStack = fox.getItemBySlot(EquipmentSlot.MAINHAND);
		Minecraft.getInstance().getItemInHandRenderer().renderItem(fox, itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, multiBufferSource, i);
		poseStack.popPose();
	}
}
