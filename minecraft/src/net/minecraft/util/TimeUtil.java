package net.minecraft.util;

public class TimeUtil {
	public static IntRange rangeOfSeconds(int i, int j) {
		return new IntRange(i * 20, j * 20);
	}
}
