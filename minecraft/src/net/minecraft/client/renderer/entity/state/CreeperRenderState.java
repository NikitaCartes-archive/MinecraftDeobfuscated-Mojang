package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CreeperRenderState extends LivingEntityRenderState {
	public float swelling;
	public boolean isPowered;
}
