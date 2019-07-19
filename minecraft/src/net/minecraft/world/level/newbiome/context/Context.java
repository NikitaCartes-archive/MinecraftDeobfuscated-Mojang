package net.minecraft.world.level.newbiome.context;

import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

public interface Context {
	int nextRandom(int i);

	ImprovedNoise getBiomeNoise();
}
