package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.MushroomCow;

@Environment(EnvType.CLIENT)
public class MushroomCowRenderState extends LivingEntityRenderState {
	public MushroomCow.Variant variant = MushroomCow.Variant.RED;
}
