package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;

@Environment(EnvType.CLIENT)
public class PiglinRenderState extends HumanoidRenderState {
	public boolean isBrute;
	public boolean isConverting;
	public float maxCrossbowChageDuration;
	public PiglinArmPose armPose = PiglinArmPose.DEFAULT;
}
