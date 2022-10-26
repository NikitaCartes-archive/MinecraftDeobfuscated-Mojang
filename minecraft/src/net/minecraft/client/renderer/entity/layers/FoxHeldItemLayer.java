package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class FoxHeldItemLayer extends RenderLayer<Fox, FoxModel<Fox>> {
	private final ItemInHandRenderer itemInHandRenderer;

	public FoxHeldItemLayer(RenderLayerParent<Fox, FoxModel<Fox>> renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
		super(renderLayerParent);
		this.itemInHandRenderer = itemInHandRenderer;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Fox fox, float f, float g, float h, float j, float k, float l) {
		boolean bl = fox.isSleeping();
		boolean bl2 = fox.isBaby();
		poseStack.pushPose();
		if (bl2) {
			float m = 0.75F;
			poseStack.scale(0.75F, 0.75F, 0.75F);
			poseStack.translate(0.0F, 0.5F, 0.209375F);
		}

		poseStack.translate(this.getParentModel().head.x / 16.0F, this.getParentModel().head.y / 16.0F, this.getParentModel().head.z / 16.0F);
		float m = fox.getHeadRollAngle(h);
		poseStack.mulPose(Axis.ZP.rotation(m));
		poseStack.mulPose(Axis.YP.rotationDegrees(k));
		poseStack.mulPose(Axis.XP.rotationDegrees(l));
		if (fox.isBaby()) {
			if (bl) {
				poseStack.translate(0.4F, 0.26F, 0.15F);
			} else {
				poseStack.translate(0.06F, 0.26F, -0.5F);
			}
		} else if (bl) {
			poseStack.translate(0.46F, 0.26F, 0.22F);
		} else {
			poseStack.translate(0.06F, 0.27F, -0.5F);
		}

		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		if (bl) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
		}

		ItemStack itemStack = fox.getItemBySlot(EquipmentSlot.MAINHAND);
		this.itemInHandRenderer.renderItem(fox, itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, multiBufferSource, i);
		poseStack.popPose();
	}
}
