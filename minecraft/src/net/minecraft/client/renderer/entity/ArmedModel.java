package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.HumanoidArm;

@Environment(EnvType.CLIENT)
public interface ArmedModel {
	void translateToHand(float f, HumanoidArm humanoidArm);
}
