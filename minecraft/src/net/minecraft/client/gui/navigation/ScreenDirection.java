package net.minecraft.client.gui.navigation;

import it.unimi.dsi.fastutil.ints.IntComparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum ScreenDirection {
	UP,
	DOWN,
	LEFT,
	RIGHT;

	private final IntComparator coordinateValueComparator = (ix, j) -> ix == j ? 0 : (this.isBefore(ix, j) ? -1 : 1);

	public ScreenAxis getAxis() {
		return switch (this) {
			case UP, DOWN -> ScreenAxis.VERTICAL;
			case LEFT, RIGHT -> ScreenAxis.HORIZONTAL;
		};
	}

	public ScreenDirection getOpposite() {
		return switch (this) {
			case UP -> DOWN;
			case DOWN -> UP;
			case LEFT -> RIGHT;
			case RIGHT -> LEFT;
		};
	}

	public boolean isPositive() {
		return switch (this) {
			case UP, LEFT -> false;
			case DOWN, RIGHT -> true;
		};
	}

	public boolean isAfter(int i, int j) {
		return this.isPositive() ? i > j : j > i;
	}

	public boolean isBefore(int i, int j) {
		return this.isPositive() ? i < j : j < i;
	}

	public IntComparator coordinateValueComparator() {
		return this.coordinateValueComparator;
	}
}
