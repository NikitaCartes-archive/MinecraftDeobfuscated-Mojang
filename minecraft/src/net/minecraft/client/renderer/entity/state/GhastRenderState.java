package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GhastRenderState extends LivingEntityRenderState {
	public boolean isCharging;
}
