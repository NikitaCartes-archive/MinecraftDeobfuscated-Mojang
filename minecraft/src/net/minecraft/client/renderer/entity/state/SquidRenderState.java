package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SquidRenderState extends LivingEntityRenderState {
	public float tentacleAngle;
	public float xBodyRot;
	public float zBodyRot;
}
