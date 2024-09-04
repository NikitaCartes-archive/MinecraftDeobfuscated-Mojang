package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;

public interface Control {
	default float rotateTowards(float f, float g, float h) {
		float i = Mth.degreesDifference(f, g);
		float j = Mth.clamp(i, -h, h);
		return f + j;
	}
}
