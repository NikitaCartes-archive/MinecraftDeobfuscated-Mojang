package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.AnimationState;

@Environment(EnvType.CLIENT)
public class ArmadilloRenderState extends LivingEntityRenderState {
	public boolean isHidingInShell;
	public final AnimationState rollOutAnimationState = new AnimationState();
	public final AnimationState rollUpAnimationState = new AnimationState();
	public final AnimationState peekAnimationState = new AnimationState();
}
