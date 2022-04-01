package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public class CrossedArmsItemLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	public CrossedArmsItemLayer(RenderLayerParent<T, M> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		poseStack.pushPose();
		poseStack.translate(0.0, 0.4F, -0.4F);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
		ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
		if (!itemStack.isEmpty()) {
			Minecraft.getInstance()
				.getItemInHandRenderer()
				.renderItem(livingEntity, itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, multiBufferSource, i);
		}

		if (livingEntity instanceof AbstractVillager abstractVillager) {
			Entity entity = abstractVillager.getOrCreateHeldEntity();
			if (entity != null) {
				AABB aABB = entity.getBoundingBox();
				double d = 0.5;
				float m = Math.min((float)(0.5 / aABB.getYsize()), 1.0F);
				poseStack.pushPose();
				poseStack.scale(m, m, m);
				Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0, 0.0, 0.0, 0.0F, h, poseStack, multiBufferSource, i);
				poseStack.popPose();
			}
		}

		poseStack.popPose();
	}
}
