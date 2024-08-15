package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class StriderRenderState extends LivingEntityRenderState implements SaddleableRenderState {
	public boolean isSaddled;
	public boolean isSuffocating;
	public boolean isRidden;

	@Override
	public boolean isSaddled() {
		return this.isSaddled;
	}
}
