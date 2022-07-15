package com.mojang.realmsclient;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum Unit {
	B,
	KB,
	MB,
	GB;

	private static final int BASE_UNIT = 1024;

	public static Unit getLargest(long l) {
		if (l < 1024L) {
			return B;
		} else {
			try {
				int i = (int)(Math.log((double)l) / Math.log(1024.0));
				String string = String.valueOf("KMGTPE".charAt(i - 1));
				return valueOf(string + "B");
			} catch (Exception var4) {
				return GB;
			}
		}
	}

	public static double convertTo(long l, Unit unit) {
		return unit == B ? (double)l : (double)l / Math.pow(1024.0, (double)unit.ordinal());
	}

	public static String humanReadable(long l) {
		int i = 1024;
		if (l < 1024L) {
			return l + " B";
		} else {
			int j = (int)(Math.log((double)l) / Math.log(1024.0));
			String string = "KMGTPE".charAt(j - 1) + "";
			return String.format(Locale.ROOT, "%.1f %sB", (double)l / Math.pow(1024.0, (double)j), string);
		}
	}

	public static String humanReadable(long l, Unit unit) {
		return String.format(Locale.ROOT, "%." + (unit == GB ? "1" : "0") + "f %s", convertTo(l, unit), unit.name());
	}
}
