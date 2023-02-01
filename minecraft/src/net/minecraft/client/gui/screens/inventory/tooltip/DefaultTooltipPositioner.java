package net.minecraft.client.gui.screens.inventory.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@Environment(EnvType.CLIENT)
public class DefaultTooltipPositioner implements ClientTooltipPositioner {
	public static final ClientTooltipPositioner INSTANCE = new DefaultTooltipPositioner();

	private DefaultTooltipPositioner() {
	}

	@Override
	public Vector2ic positionTooltip(Screen screen, int i, int j, int k, int l) {
		Vector2i vector2i = new Vector2i(i, j).add(12, -12);
		this.positionTooltip(screen, vector2i, k, l);
		return vector2i;
	}

	private void positionTooltip(Screen screen, Vector2i vector2i, int i, int j) {
		if (vector2i.x + i > screen.width) {
			vector2i.x = Math.max(vector2i.x - 24 - i, 4);
		}

		int k = j + 3;
		if (vector2i.y + k > screen.height) {
			vector2i.y = screen.height - k;
		}
	}
}
