package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.AnimationState;

@Environment(EnvType.CLIENT)
public class BreezeRenderState extends LivingEntityRenderState {
	public final AnimationState idle = new AnimationState();
	public final AnimationState shoot = new AnimationState();
	public final AnimationState slide = new AnimationState();
	public final AnimationState slideBack = new AnimationState();
	public final AnimationState inhale = new AnimationState();
	public final AnimationState longJump = new AnimationState();
}
