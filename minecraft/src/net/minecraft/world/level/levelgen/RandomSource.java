package net.minecraft.world.level.levelgen;

public interface RandomSource {
	RandomSource fork();

	default PositionalRandomFactory forkPositional() {
		return new PositionalRandomFactory(this.nextLong());
	}

	void setSeed(long l);

	int nextInt();

	int nextInt(int i);

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
