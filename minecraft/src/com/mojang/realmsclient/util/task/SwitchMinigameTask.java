package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SwitchMinigameTask extends LongRunningTask {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component TITLE = Component.translatable("mco.minigame.world.starting.screen.title");
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

		for (int i = 0; i < 25; i++) {
			try {
				if (this.aborted()) {
					return;
				}

				if (realmsClient.putIntoMinigameMode(this.worldId, this.worldTemplate.id)) {
					setScreen(this.lastScreen);
					break;
				}
			} catch (RetryCallException var4) {
				if (this.aborted()) {
					return;
				}

				pause((long)var4.delaySeconds);
			} catch (Exception var5) {
				if (this.aborted()) {
					return;
				}

				LOGGER.error("Couldn't start mini game!");
				this.error(var5);
			}
		}
	}

	@Override
	public Component getTitle() {
		return TITLE;
	}
}
