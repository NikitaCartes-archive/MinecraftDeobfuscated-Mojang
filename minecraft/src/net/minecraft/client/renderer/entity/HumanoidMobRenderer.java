package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CrossbowItem;

@Environment(EnvType.CLIENT)
public abstract class HumanoidMobRenderer<T extends Mob, S extends HumanoidRenderState, M extends HumanoidModel<S>> extends AgeableMobRenderer<T, S, M> {
	public HumanoidMobRenderer(EntityRendererProvider.Context context, M humanoidModel, float f) {
		this(context, humanoidModel, humanoidModel, f);
	}

	public HumanoidMobRenderer(EntityRendererProvider.Context context, M humanoidModel, M humanoidModel2, float f) {
		this(context, humanoidModel, humanoidModel2, f, CustomHeadLayer.Transforms.DEFAULT);
	}

	public HumanoidMobRenderer(EntityRendererProvider.Context context, M humanoidModel, M humanoidModel2, float f, CustomHeadLayer.Transforms transforms) {
		super(context, humanoidModel, humanoidModel2, f);
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), transforms, context.getItemRenderer()));
		this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
		this.addLayer(new ItemInHandLayer<>(this, context.getItemRenderer()));
	}

	public void extractRenderState(T mob, S humanoidRenderState, float f) {
		super.extractRenderState(mob, humanoidRenderState, f);
		extractHumanoidRenderState(mob, humanoidRenderState, f);
	}

	public static void extractHumanoidRenderState(LivingEntity livingEntity, HumanoidRenderState humanoidRenderState, float f) {
		humanoidRenderState.isCrouching = livingEntity.isCrouching();
		humanoidRenderState.isFallFlying = livingEntity.isFallFlying();
		humanoidRenderState.isVisuallySwimming = livingEntity.isVisuallySwimming();
		humanoidRenderState.isPassenger = livingEntity.isPassenger();
		humanoidRenderState.speedValue = 1.0F;
		if (humanoidRenderState.isFallFlying) {
			humanoidRenderState.speedValue = (float)livingEntity.getDeltaMovement().lengthSqr();
			humanoidRenderState.speedValue /= 0.2F;
			humanoidRenderState.speedValue = humanoidRenderState.speedValue * humanoidRenderState.speedValue * humanoidRenderState.speedValue;
		}

		if (humanoidRenderState.speedValue < 1.0F) {
			humanoidRenderState.speedValue = 1.0F;
		}

		humanoidRenderState.attackTime = livingEntity.getAttackAnim(f);
		humanoidRenderState.swimAmount = livingEntity.getSwimAmount(f);
		humanoidRenderState.attackArm = getAttackArm(livingEntity);
		humanoidRenderState.useItemHand = livingEntity.getUsedItemHand();
		humanoidRenderState.maxCrossbowChargeDuration = (float)CrossbowItem.getChargeDuration(livingEntity.getUseItem(), livingEntity);
		humanoidRenderState.ticksUsingItem = livingEntity.getTicksUsingItem();
		humanoidRenderState.isUsingItem = livingEntity.isUsingItem();
		humanoidRenderState.elytraRotX = livingEntity.elytraAnimationState.getRotX(f);
		humanoidRenderState.elytraRotY = livingEntity.elytraAnimationState.getRotY(f);
		humanoidRenderState.elytraRotZ = livingEntity.elytraAnimationState.getRotZ(f);
		humanoidRenderState.chestItem = livingEntity.getItemBySlot(EquipmentSlot.CHEST).copy();
		humanoidRenderState.legsItem = livingEntity.getItemBySlot(EquipmentSlot.LEGS).copy();
		humanoidRenderState.feetItem = livingEntity.getItemBySlot(EquipmentSlot.FEET).copy();
	}

	private static HumanoidArm getAttackArm(LivingEntity livingEntity) {
		HumanoidArm humanoidArm = livingEntity.getMainArm();
		return livingEntity.swingingArm == InteractionHand.MAIN_HAND ? humanoidArm : humanoidArm.getOpposite();
	}
}
