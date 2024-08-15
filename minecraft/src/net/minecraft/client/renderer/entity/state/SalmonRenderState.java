package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.Salmon;

@Environment(EnvType.CLIENT)
public class SalmonRenderState extends LivingEntityRenderState {
	public Salmon.Variant variant = Salmon.Variant.MEDIUM;
}
