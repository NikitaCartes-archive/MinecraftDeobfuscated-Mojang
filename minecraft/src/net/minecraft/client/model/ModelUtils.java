package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;

@Environment(EnvType.CLIENT)
public class ModelUtils {
	public static float rotlerpRad(float f, float g, float h) {
		float i = g - f;

		while (i < (float) -Math.PI) {
			i += (float) (Math.PI * 2);
		}

		while (i >= (float) Math.PI) {
			i -= (float) (Math.PI * 2);
		}

		return f + h * i;
	}

	public static void setRotation(ModelPart modelPart, float f, float g, float h) {
		modelPart.xRot = f;
		modelPart.yRot = g;
		modelPart.zRot = h;
	}
}
