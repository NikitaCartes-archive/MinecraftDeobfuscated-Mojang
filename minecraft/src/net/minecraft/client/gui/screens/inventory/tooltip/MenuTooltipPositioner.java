package net.minecraft.client.gui.screens.inventory.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
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
	public Vector2ic positionTooltip(int i, int j, int k, int l, int m, int n) {
		Vector2i vector2i = new Vector2i(k + 12, l);
		if (vector2i.x + m > i - 5) {
			vector2i.x = Math.max(k - 12 - m, 9);
		}

		vector2i.y += 3;
		int o = n + 3 + 3;
		int p = this.widget.getY() + this.widget.getHeight() + 3 + getOffset(0, 0, this.widget.getHeight());
		int q = j - 5;
		if (p + o <= q) {
			vector2i.y = vector2i.y + getOffset(vector2i.y, this.widget.getY(), this.widget.getHeight());
		} else {
			vector2i.y = vector2i.y - (o + getOffset(vector2i.y, this.widget.getY() + this.widget.getHeight(), this.widget.getHeight()));
		}

		return vector2i;
	}

	private static int getOffset(int i, int j, int k) {
		int l = Math.min(Math.abs(i - j), k);
		return Math.round(Mth.lerp((float)l / (float)k, (float)(k - 3), 5.0F));
	}
}
