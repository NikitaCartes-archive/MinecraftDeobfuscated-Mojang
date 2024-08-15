package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PigRenderState extends LivingEntityRenderState implements SaddleableRenderState {
	public boolean isSaddled;

	@Override
	public boolean isSaddled() {
		return this.isSaddled;
	}
}
