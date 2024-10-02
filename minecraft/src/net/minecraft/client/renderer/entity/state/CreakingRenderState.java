package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.AnimationState;

@Environment(EnvType.CLIENT)
public class CreakingRenderState extends LivingEntityRenderState {
	public AnimationState invulnerabilityAnimationState = new AnimationState();
	public AnimationState attackAnimationState = new AnimationState();
	public boolean isActive;
	public boolean canMove;
}
