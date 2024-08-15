package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AllayRenderState extends LivingEntityRenderState {
	public boolean isDancing;
	public boolean isSpinning;
	public float spinningProgress;
	public float holdingAnimationProgress;
}
