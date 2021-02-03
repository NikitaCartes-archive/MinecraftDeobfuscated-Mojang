package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer0;

public enum OceanLayer implements AreaTransformer0 {
	INSTANCE;

	@Override
	public int applyPixel(Context context, int i, int j) {
		ImprovedNoise improvedNoise = context.getBiomeNoise();
		double d = improvedNoise.noise((double)i / 8.0, (double)j / 8.0, 0.0);
		if (d > 0.4) {
			return 44;
		} else if (d > 0.2) {
			return 45;
		} else if (d < -0.4) {
			return 10;
		} else {
			return d < -0.2 ? 46 : 0;
		}
	}
}
