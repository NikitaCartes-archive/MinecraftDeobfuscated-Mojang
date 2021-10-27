package net.minecraft.world.level.levelgen;

public interface RandomSource {
	RandomSource fork();

	PositionalRandomFactory forkPositional();

	void setSeed(long l);

	int nextInt();

	int nextInt(int i);

	default int nextIntBetweenInclusive(int i, int j) {
		return this.nextInt(j - i + 1) + i;
	}

	long nextLong();

	boolean nextBoolean();

	float nextFloat();

	double nextDouble();

	double nextGaussian();

	default void consumeCount(int i) {
		for (int j = 0; j < i; j++) {
			this.nextInt();
		}
	}
}
