package net.minecraft.world.damagesource;

import net.minecraft.util.Mth;

public class CombatRules {
	public static float getDamageAfterAbsorb(float f, float g, float h) {
		float i = 2.0F + h / 4.0F;
		float j = Mth.clamp(g - f / i, g * 0.2F, 20.0F);
		return f * (1.0F - j / 25.0F);
	}

	public static float getDamageAfterMagicAbsorb(float f, float g) {
		float h = Mth.clamp(g, 0.0F, 20.0F);
		return f * (1.0F - h / 25.0F);
	}
}
