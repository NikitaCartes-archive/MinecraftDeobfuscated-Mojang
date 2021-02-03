package net.minecraft.world.level.levelgen;

public interface RandomSource {
	int nextInt();

	int nextInt(int i);

	double nextDouble();

	default void consumeCount(int i) {
		for (int j = 0; j < i; j++) {
			this.nextInt();
		}
	}
}
