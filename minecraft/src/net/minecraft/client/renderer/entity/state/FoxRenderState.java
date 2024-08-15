package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.Fox;

@Environment(EnvType.CLIENT)
public class FoxRenderState extends LivingEntityRenderState {
	public float headRollAngle;
	public float crouchAmount;
	public boolean isCrouching;
	public boolean isSleeping;
	public boolean isSitting;
	public boolean isFaceplanted;
	public boolean isPouncing;
	public Fox.Variant variant = Fox.Variant.RED;
}
