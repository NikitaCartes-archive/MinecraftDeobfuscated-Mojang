package net.minecraft.client.gui.screens.inventory.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@Environment(EnvType.CLIENT)
public class MenuTooltipPositioner implements ClientTooltipPositioner {
	private static final int MARGIN = 5;
	private static final int MOUSE_OFFSET_X = 12;
	public static final int MAX_OVERLAP_WITH_WIDGET = 3;
	public static final int MAX_DISTANCE_TO_WIDGET = 5;
	private final AbstractWidget widget;

	public MenuTooltipPositioner(AbstractWidget abstractWidget) {
		this.widget = abstractWidget;
	}

	@Override
	public Vector2ic positionTooltip(Screen screen, int i, int j, int k, int l) {
		Vector2i vector2i = new Vector2i(i + 12, j);
		if (vector2i.x + k > screen.width - 5) {
			vector2i.x = Math.max(i - 12 - k, 9);
		}

		vector2i.y += 3;
		int m = l + 3 + 3;
		int n = this.widget.getY() + this.widget.getHeight() + 3 + getOffset(0, 0, this.widget.getHeight());
		int o = screen.height - 5;
		if (n + m <= o) {
			vector2i.y = vector2i.y + getOffset(vector2i.y, this.widget.getY(), this.widget.getHeight());
		} else {
			vector2i.y = vector2i.y - (m + getOffset(vector2i.y, this.widget.getY() + this.widget.getHeight(), this.widget.getHeight()));
		}

		return vector2i;
	}

	private static int getOffset(int i, int j, int k) {
		int l = Math.min(Math.abs(i - j), k);
		return Math.round(Mth.lerp((float)l / (float)k, (float)(k - 3), 5.0F));
	}
}
