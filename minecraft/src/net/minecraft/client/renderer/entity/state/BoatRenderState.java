package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BoatRenderState extends EntityRenderState {
	public float yRot;
	public int hurtDir;
	public float hurtTime;
	public float damageTime;
	public float bubbleAngle;
	public boolean isUnderWater;
	public float rowingTimeLeft;
	public float rowingTimeRight;
}
