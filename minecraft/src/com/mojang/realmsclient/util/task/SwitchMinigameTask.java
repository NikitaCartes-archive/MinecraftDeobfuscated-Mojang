package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

@Environment(EnvType.CLIENT)
public class SwitchMinigameTask extends LongRunningTask {
	private final long worldId;
	private final WorldTemplate worldTemplate;
	private final RealmsConfigureWorldScreen lastScreen;

	public SwitchMinigameTask(long l, WorldTemplate worldTemplate, RealmsConfigureWorldScreen realmsConfigureWorldScreen) {
		this.worldId = l;
		this.worldTemplate = worldTemplate;
		this.lastScreen = realmsConfigureWorldScreen;
	}

	public void run() {
		RealmsClient realmsClient = RealmsClient.create();
		String string = I18n.get("mco.minigame.world.starting.screen.title");
		this.setTitle(string);

		for (int i = 0; i < 25; i++) {
			try {
				if (this.aborted()) {
					return;
				}

				if (realmsClient.putIntoMinigameMode(this.worldId, this.worldTemplate.id)) {
					setScreen(this.lastScreen);
					break;
				}
			} catch (RetryCallException var5) {
				if (this.aborted()) {
					return;
				}

				pause(var5.delaySeconds);
			} catch (Exception var6) {
				if (this.aborted()) {
					return;
				}

				LOGGER.error("Couldn't start mini game!");
				this.error(var6.toString());
			}
		}
	}
}
