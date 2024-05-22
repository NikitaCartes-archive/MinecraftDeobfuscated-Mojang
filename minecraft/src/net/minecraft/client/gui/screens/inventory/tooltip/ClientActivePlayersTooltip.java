package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

@Environment(EnvType.CLIENT)
public class ClientActivePlayersTooltip implements ClientTooltipComponent {
	private static final int SKIN_SIZE = 10;
	private static final int PADDING = 2;
	private final List<ProfileResult> activePlayers;

	public ClientActivePlayersTooltip(ClientActivePlayersTooltip.ActivePlayersTooltip activePlayersTooltip) {
		this.activePlayers = activePlayersTooltip.profiles();
	}

	@Override
	public int getHeight() {
		return this.activePlayers.size() * 12 + 2;
	}

	@Override
	public int getWidth(Font font) {
		int i = 0;

		for (ProfileResult profileResult : this.activePlayers) {
			int j = font.width(profileResult.profile().getName());
			if (j > i) {
				i = j;
			}
		}

		return i + 10 + 6;
	}

	@Override
	public void renderImage(Font font, int i, int j, GuiGraphics guiGraphics) {
		for (int k = 0; k < this.activePlayers.size(); k++) {
			ProfileResult profileResult = (ProfileResult)this.activePlayers.get(k);
			int l = j + 2 + k * 12;
			PlayerFaceRenderer.draw(guiGraphics, Minecraft.getInstance().getSkinManager().getInsecureSkin(profileResult.profile()), i + 2, l, 10);
			guiGraphics.drawString(font, profileResult.profile().getName(), i + 10 + 4, l + 2, -1);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record ActivePlayersTooltip(List<ProfileResult> profiles) implements TooltipComponent {
	}
}
