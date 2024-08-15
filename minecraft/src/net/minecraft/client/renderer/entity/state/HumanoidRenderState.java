package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class HumanoidRenderState extends LivingEntityRenderState {
	public float swimAmount;
	public float attackTime;
	public float speedValue = 1.0F;
	public float maxCrossbowChargeDuration;
	public int ticksUsingItem;
	public HumanoidArm attackArm = HumanoidArm.RIGHT;
	public InteractionHand useItemHand = InteractionHand.MAIN_HAND;
	public boolean isCrouching;
	public boolean isFallFlying;
	public boolean isSwimming;
	public boolean isPassenger;
	public boolean isUsingItem;
	public float elytraRotX;
	public float elytraRotY;
	public float elytraRotZ;
	public ItemStack chestItem = ItemStack.EMPTY;
	public ItemStack legsItem = ItemStack.EMPTY;
	public ItemStack feetItem = ItemStack.EMPTY;
}
