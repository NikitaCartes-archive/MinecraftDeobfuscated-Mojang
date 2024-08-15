package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

@Environment(EnvType.CLIENT)
public class AxolotlRenderState extends LivingEntityRenderState {
	public Axolotl.Variant variant = Axolotl.Variant.LUCY;
	public float playingDeadFactor;
	public float movingFactor;
	public float inWaterFactor = 1.0F;
	public float onGroundFactor;
}
