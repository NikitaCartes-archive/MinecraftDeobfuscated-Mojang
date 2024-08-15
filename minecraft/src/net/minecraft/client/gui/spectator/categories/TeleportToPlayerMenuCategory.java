package net.minecraft.client.gui.spectator.categories;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.spectator.PlayerMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.GameType;

@Environment(EnvType.CLIENT)
public class TeleportToPlayerMenuCategory implements SpectatorMenuCategory, SpectatorMenuItem {
	private static final ResourceLocation TELEPORT_TO_PLAYER_SPRITE = ResourceLocation.withDefaultNamespace("spectator/teleport_to_player");
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
	public void renderIcon(GuiGraphics guiGraphics, float f, float g) {
		guiGraphics.blitSprite(RenderType::guiTextured, TELEPORT_TO_PLAYER_SPRITE, 0, 0, 16, 16, ARGB.colorFromFloat(g, f, f, f));
	}

	@Override
	public boolean isEnabled() {
		return !this.items.isEmpty();
	}
}
