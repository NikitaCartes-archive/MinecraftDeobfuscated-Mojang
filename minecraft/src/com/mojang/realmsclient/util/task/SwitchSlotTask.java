package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RetryCallException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class SwitchSlotTask extends LongRunningTask {
	private final long worldId;
	private final int slot;
	private final Runnable callback;

	public SwitchSlotTask(long l, int i, Runnable runnable) {
		this.worldId = l;
		this.slot = i;
		this.callback = runnable;
	}

	public void run() {
		RealmsClient realmsClient = RealmsClient.create();
		this.setTitle(new TranslatableComponent("mco.minigame.world.slot.screen.title"));

		for (int i = 0; i < 25; i++) {
			try {
				if (this.aborted()) {
					return;
				}

				if (realmsClient.switchSlot(this.worldId, this.slot)) {
					this.callback.run();
					break;
				}
			} catch (RetryCallException var4) {
				if (this.aborted()) {
					return;
				}

				pause(var4.delaySeconds);
			} catch (Exception var5) {
				if (this.aborted()) {
					return;
				}

				LOGGER.error("Couldn't switch world!");
				this.error(var5.toString());
			}
		}
	}
}
