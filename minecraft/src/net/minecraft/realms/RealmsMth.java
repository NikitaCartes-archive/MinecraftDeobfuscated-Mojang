package net.minecraft.realms;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class RealmsMth {
	public static float sin(float f) {
		return Mth.sin(f);
	}

	public static double nextDouble(Random random, double d, double e) {
		return Mth.nextDouble(random, d, e);
	}

	public static int ceil(float f) {
		return Mth.ceil(f);
	}

	public static int floor(double d) {
		return Mth.floor(d);
	}

	public static int intFloorDiv(int i, int j) {
		return Mth.intFloorDiv(i, j);
	}

	public static float abs(float f) {
		return Mth.abs(f);
	}

	public static int clamp(int i, int j, int k) {
		return Mth.clamp(i, j, k);
	}

	public static double clampedLerp(double d, double e, double f) {
		return Mth.clampedLerp(d, e, f);
	}

	public static int ceil(double d) {
		return Mth.ceil(d);
	}

	public static boolean isEmpty(String string) {
		return StringUtils.isEmpty(string);
	}

	public static long lfloor(double d) {
		return Mth.lfloor(d);
	}

	public static float sqrt(double d) {
		return Mth.sqrt(d);
	}

	public static double clamp(double d, double e, double f) {
		return Mth.clamp(d, e, f);
	}

	public static int getInt(String string, int i) {
		return Mth.getInt(string, i);
	}

	public static double getDouble(String string, double d) {
		return Mth.getDouble(string, d);
	}

	public static int log2(int i) {
		return Mth.log2(i);
	}

	public static int absFloor(double d) {
		return Mth.absFloor(d);
	}

	public static int smallestEncompassingPowerOfTwo(int i) {
		return Mth.smallestEncompassingPowerOfTwo(i);
	}

	public static float sqrt(float f) {
		return Mth.sqrt(f);
	}

	public static float cos(float f) {
		return Mth.cos(f);
	}

	public static int getInt(String string, int i, int j) {
		return Mth.getInt(string, i, j);
	}

	public static int fastFloor(double d) {
		return Mth.fastFloor(d);
	}

	public static double absMax(double d, double e) {
		return Mth.absMax(d, e);
	}

	public static float nextFloat(Random random, float f, float g) {
		return Mth.nextFloat(random, f, g);
	}

	public static double wrapDegrees(double d) {
		return Mth.wrapDegrees(d);
	}

	public static float wrapDegrees(float f) {
		return Mth.wrapDegrees(f);
	}

	public static float clamp(float f, float g, float h) {
		return Mth.clamp(f, g, h);
	}

	public static double getDouble(String string, double d, double e) {
		return Mth.getDouble(string, d, e);
	}

	public static int roundUp(int i, int j) {
		return Mth.roundUp(i, j);
	}

	public static double average(long[] ls) {
		return Mth.average(ls);
	}

	public static int floor(float f) {
		return Mth.floor(f);
	}

	public static int abs(int i) {
		return Mth.abs(i);
	}

	public static int nextInt(Random random, int i, int j) {
		return Mth.nextInt(random, i, j);
	}
}
