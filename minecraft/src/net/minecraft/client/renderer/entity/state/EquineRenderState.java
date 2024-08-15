package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EquineRenderState extends LivingEntityRenderState {
	public boolean isSaddled;
	public boolean isRidden;
	public boolean animateTail;
	public float eatAnimation;
	public float standAnimation;
	public float feedingAnimation;
}
