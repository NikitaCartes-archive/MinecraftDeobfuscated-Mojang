package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class VillagerTradeItemLayer<T extends LivingEntity> extends RenderLayer<T, VillagerModel<T>> {
	public VillagerTradeItemLayer(RenderLayerParent<T, VillagerModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m
	) {
		poseStack.pushPose();
		poseStack.translate(0.0, 0.4F, -0.4F);
		poseStack.mulPose(Vector3f.XP.rotation(180.0F, true));
		ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
		Minecraft.getInstance().getItemInHandRenderer().renderItem(livingEntity, itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, multiBufferSource);
		poseStack.popPose();
	}
}
