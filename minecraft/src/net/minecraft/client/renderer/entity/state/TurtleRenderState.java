package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TurtleRenderState extends LivingEntityRenderState {
	public boolean isOnLand;
	public boolean isLayingEgg;
	public boolean hasEgg;
}
