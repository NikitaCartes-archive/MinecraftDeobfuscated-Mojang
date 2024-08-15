package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SlimeRenderState extends LivingEntityRenderState {
	public float squish;
	public int size = 1;
}
