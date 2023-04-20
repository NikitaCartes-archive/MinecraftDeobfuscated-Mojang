package net.minecraft.client.gui.spectator.categories;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.PlayerMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

@Environment(EnvType.CLIENT)
public class TeleportToPlayerMenuCategory implements SpectatorMenuCategory, SpectatorMenuItem {
	private static final Comparator<PlayerInfo> PROFILE_ORDER = Comparator.comparing(playerInfo -> playerInfo.getProfile().getId());
	private static final Component TELEPORT_TEXT = Component.translatable("spectatorMenu.teleport");
	private static final Component TELEPORT_PROMPT = Component.translatable("spectatorMenu.teleport.prompt");
	private final List<SpectatorMenuItem> items;

	public TeleportToPlayerMenuCategory() {
		this(Minecraft.getInstance().getConnection().getListedOnlinePlayers());
	}

	public TeleportToPlayerMenuCategory(Collection<PlayerInfo> collection) {
		this.items = collection.stream()
			.filter(playerInfo -> playerInfo.getGameMode() != GameType.SPECTATOR)
			.sorted(PROFILE_ORDER)
			.map(playerInfo -> new PlayerMenuItem(playerInfo.getProfile()))
			.toList();
	}

	@Override
	public List<SpectatorMenuItem> getItems() {
		return this.items;
	}

	@Override
	public Component getPrompt() {
		return TELEPORT_PROMPT;
	}

	@Override
	public void selectItem(SpectatorMenu spectatorMenu) {
		spectatorMenu.selectCategory(this);
	}

	@Override
	public Component getName() {
		return TELEPORT_TEXT;
	}

	@Override
	public void renderIcon(GuiGraphics guiGraphics, float f, int i) {
		guiGraphics.blit(SpectatorGui.SPECTATOR_LOCATION, 0, 0, 0.0F, 0.0F, 16, 16, 256, 256);
	}

	@Override
	public boolean isEnabled() {
		return !this.items.isEmpty();
	}
}
