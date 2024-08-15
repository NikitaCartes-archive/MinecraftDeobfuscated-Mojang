package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.TropicalFish;

@Environment(EnvType.CLIENT)
public class TropicalFishRenderState extends LivingEntityRenderState {
	public TropicalFish.Pattern variant = TropicalFish.Pattern.FLOPPER;
	public int baseColor = -1;
	public int patternColor = -1;
}
