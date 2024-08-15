package net.minecraft.client.renderer;

import com.mojang.blaze3d.shaders.FogShape;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record FogParameters(float start, float end, FogShape shape, float red, float green, float blue, float alpha) {
	public static final FogParameters NO_FOG = new FogParameters(Float.MAX_VALUE, 0.0F, FogShape.SPHERE, 0.0F, 0.0F, 0.0F, 0.0F);
}
