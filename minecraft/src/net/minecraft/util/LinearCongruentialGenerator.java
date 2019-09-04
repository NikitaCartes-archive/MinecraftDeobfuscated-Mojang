package net.minecraft.util;

public class LinearCongruentialGenerator {
	public static long next(long l, long m) {
		l *= l * 6364136223846793005L + 1442695040888963407L;
		return l + m;
	}
}
