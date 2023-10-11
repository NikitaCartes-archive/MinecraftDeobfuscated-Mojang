package net.minecraft.client.gui.screens.inventory.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@Environment(EnvType.CLIENT)
public class BelowOrAboveWidgetTooltipPositioner implements ClientTooltipPositioner {
	private final ScreenRectangle screenRectangle;

	public BelowOrAboveWidgetTooltipPositioner(ScreenRectangle screenRectangle) {
		this.screenRectangle = screenRectangle;
	}

	@Override
	public Vector2ic positionTooltip(int i, int j, int k, int l, int m, int n) {
		Vector2i vector2i = new Vector2i();
		vector2i.x = this.screenRectangle.left() + 3;
		vector2i.y = this.screenRectangle.bottom() + 3 + 1;
		if (vector2i.y + n + 3 > j) {
			vector2i.y = this.screenRectangle.top() - n - 3 - 1;
		}

		if (vector2i.x + m > i) {
			vector2i.x = Math.max(this.screenRectangle.right() - m - 3, 4);
		}

		return vector2i;
	}
}
