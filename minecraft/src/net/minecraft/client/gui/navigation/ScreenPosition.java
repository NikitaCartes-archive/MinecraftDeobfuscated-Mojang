package net.minecraft.client.gui.navigation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record ScreenPosition(int x, int y) {
	public static ScreenPosition of(ScreenAxis screenAxis, int i, int j) {
		return switch (screenAxis) {
			case HORIZONTAL -> new ScreenPosition(i, j);
			case VERTICAL -> new ScreenPosition(j, i);
		};
	}

	public ScreenPosition step(ScreenDirection screenDirection) {
		return switch (screenDirection) {
			case DOWN -> new ScreenPosition(this.x, this.y + 1);
			case UP -> new ScreenPosition(this.x, this.y - 1);
			case LEFT -> new ScreenPosition(this.x - 1, this.y);
			case RIGHT -> new ScreenPosition(this.x + 1, this.y);
		};
	}

	public int getCoordinate(ScreenAxis screenAxis) {
		return switch (screenAxis) {
			case HORIZONTAL -> this.x;
			case VERTICAL -> this.y;
		};
	}
}
