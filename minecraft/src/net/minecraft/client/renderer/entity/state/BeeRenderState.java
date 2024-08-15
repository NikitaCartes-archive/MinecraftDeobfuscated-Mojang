package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BeeRenderState extends LivingEntityRenderState {
	public float rollAmount;
	public boolean hasStinger = true;
	public boolean isOnGround;
	public boolean isAngry;
	public boolean hasNectar;
}
