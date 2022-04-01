package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.GenericItemBlock;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class CarriedBlockLayer<T extends LivingEntity> extends RenderLayer<T, HumanoidModel<T>> {
	private final float offsetX;
	private final float offsetY;
	private final float offsetZ;

	public CarriedBlockLayer(RenderLayerParent<T, HumanoidModel<T>> renderLayerParent, float f, float g, float h) {
		super(renderLayerParent);
		this.offsetX = f;
		this.offsetY = g;
		this.offsetZ = h;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		if (livingEntity.getCarried() == LivingEntity.Carried.BLOCK) {
			BlockState blockState = livingEntity.getCarriedBlock();
			poseStack.pushPose();
			poseStack.mulPose(Vector3f.YP.rotationDegrees(k));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(l));
			Item item = GenericItemBlock.itemFromGenericBlock(blockState);
			if (item != null) {
				poseStack.translate(0.0, 0.25, -1.0);
				poseStack.translate((double)this.offsetX, (double)this.offsetY, (double)this.offsetZ);
				float m = 1.5F;
				poseStack.scale(-1.5F, -1.5F, 1.5F);
				ItemStack itemStack = item.getDefaultInstance();
				Minecraft.getInstance()
					.getItemInHandRenderer()
					.renderItem(livingEntity, itemStack, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false, poseStack, multiBufferSource, i);
			} else {
				poseStack.translate(0.0, 0.375, -0.75);
				poseStack.mulPose(Vector3f.XP.rotationDegrees(20.0F));
				poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
				poseStack.translate((double)this.offsetX, (double)this.offsetY, (double)this.offsetZ);
				float m = 0.5F;
				poseStack.scale(-0.5F, -0.5F, 0.5F);
				poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
				Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
			}

			poseStack.popPose();
		}
	}
}
