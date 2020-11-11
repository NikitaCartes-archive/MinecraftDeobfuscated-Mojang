package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameType;

@Environment(EnvType.CLIENT)
public enum SelectedGameMode {
	SURVIVAL(GameType.SURVIVAL),
	CREATIVE(GameType.CREATIVE),
	ADVENTURE(GameType.ADVENTURE),
	SPECTATOR(GameType.SPECTATOR);

	private final GameType gameType;
	private final Component displayName;

	private SelectedGameMode(GameType gameType) {
		this.gameType = gameType;
		this.displayName = new TranslatableComponent("selectWorld.gameMode." + gameType.getName());
	}

	public GameType getGameType() {
		return this.gameType;
	}

	public Component getDisplayName() {
		return this.displayName;
	}
}
