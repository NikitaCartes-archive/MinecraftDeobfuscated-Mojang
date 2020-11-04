package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;

@Environment(EnvType.CLIENT)
public abstract class SkullModelBase extends Model {
	public SkullModelBase() {
		super(RenderType::entityTranslucent);
	}

	public abstract void setupAnim(float f, float g, float h);
}
