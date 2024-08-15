package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RavagerRenderState extends LivingEntityRenderState {
	public float stunnedTicksRemaining;
	public float attackTicksRemaining;
	public float roarAnimation;
}
