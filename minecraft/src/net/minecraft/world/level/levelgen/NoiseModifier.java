package net.minecraft.world.level.levelgen;

@FunctionalInterface
public interface NoiseModifier {
	NoiseModifier PASSTHROUGH = (d, i, j, k) -> d;

	double modifyNoise(double d, int i, int j, int k);
}
