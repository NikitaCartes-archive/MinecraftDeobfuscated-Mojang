package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;

@Environment(EnvType.CLIENT)
public class IllagerRenderState extends LivingEntityRenderState {
	public boolean isRiding;
	public boolean isAggressive;
	public HumanoidArm mainArm = HumanoidArm.RIGHT;
	public AbstractIllager.IllagerArmPose armPose = AbstractIllager.IllagerArmPose.NEUTRAL;
	public int maxCrossbowChargeDuration;
	public int ticksUsingItem;
	public float attackAnim;
}
