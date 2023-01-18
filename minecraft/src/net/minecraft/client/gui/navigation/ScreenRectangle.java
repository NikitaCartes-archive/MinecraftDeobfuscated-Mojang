package net.minecraft.client.gui.navigation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record ScreenRectangle(ScreenPosition position, int width, int height) {
	private static final ScreenRectangle EMPTY = new ScreenRectangle(0, 0, 0, 0);

	public ScreenRectangle(int i, int j, int k, int l) {
		this(new ScreenPosition(i, j), k, l);
	}

	public static ScreenRectangle empty() {
		return EMPTY;
	}

	public static ScreenRectangle of(ScreenAxis screenAxis, int i, int j, int k, int l) {
		return switch (screenAxis) {
			case HORIZONTAL -> new ScreenRectangle(i, j, k, l);
			case VERTICAL -> new ScreenRectangle(j, i, l, k);
		};
	}

	public ScreenRectangle step(ScreenDirection screenDirection) {
		return new ScreenRectangle(this.position.step(screenDirection), this.width, this.height);
	}

	public int getLength(ScreenAxis screenAxis) {
		return switch (screenAxis) {
			case HORIZONTAL -> this.width;
			case VERTICAL -> this.height;
		};
	}

	public int getBoundInDirection(ScreenDirection screenDirection) {
		ScreenAxis screenAxis = screenDirection.getAxis();
		return screenDirection.isPositive() ? this.position.getCoordinate(screenAxis) + this.getLength(screenAxis) - 1 : this.position.getCoordinate(screenAxis);
	}

	public ScreenRectangle getBorder(ScreenDirection screenDirection) {
		int i = this.getBoundInDirection(screenDirection);
		ScreenAxis screenAxis = screenDirection.getAxis().orthogonal();
		int j = this.getBoundInDirection(screenAxis.getNegative());
		int k = this.getLength(screenAxis);
		return of(screenDirection.getAxis(), i, j, 1, k).step(screenDirection);
	}

	public boolean overlaps(ScreenRectangle screenRectangle) {
		return this.overlapsInAxis(screenRectangle, ScreenAxis.HORIZONTAL) && this.overlapsInAxis(screenRectangle, ScreenAxis.VERTICAL);
	}

	public boolean overlapsInAxis(ScreenRectangle screenRectangle, ScreenAxis screenAxis) {
		int i = this.getBoundInDirection(screenAxis.getNegative());
		int j = screenRectangle.getBoundInDirection(screenAxis.getNegative());
		int k = this.getBoundInDirection(screenAxis.getPositive());
		int l = screenRectangle.getBoundInDirection(screenAxis.getPositive());
		return Math.max(i, j) <= Math.min(k, l);
	}

	public int getCenterInAxis(ScreenAxis screenAxis) {
		return (this.getBoundInDirection(screenAxis.getPositive()) + this.getBoundInDirection(screenAxis.getNegative())) / 2;
	}
}
