package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PhantomRenderState extends LivingEntityRenderState {
	public float flapTime;
	public int size;
}
