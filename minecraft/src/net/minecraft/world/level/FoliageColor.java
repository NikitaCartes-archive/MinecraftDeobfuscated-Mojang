package net.minecraft.world.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FoliageColor {
	private static int[] pixels = new int[65536];

	public static void init(int[] is) {
		pixels = is;
	}

	public static int get(double d, double e) {
		e *= d;
		int i = (int)((1.0 - d) * 255.0);
		int j = (int)((1.0 - e) * 255.0);
		return pixels[j << 8 | i];
	}

	public static int getEvergreenColor() {
		return 6396257;
	}

	public static int getBirchColor() {
		return 8431445;
	}

	public static int getDefaultColor() {
		return 4764952;
	}
}
