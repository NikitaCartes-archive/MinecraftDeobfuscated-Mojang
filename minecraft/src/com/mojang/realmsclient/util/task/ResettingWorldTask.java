package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public abstract class ResettingWorldTask extends LongRunningTask {
	private final long serverId;
	private final Component title;
	private final Runnable callback;

	public ResettingWorldTask(long l, Component component, Runnable runnable) {
		this.serverId = l;
		this.title = component;
		this.callback = runnable;
	}

	protected abstract void sendResetRequest(RealmsClient realmsClient, long l) throws RealmsServiceException;

	public void run() {
		RealmsClient realmsClient = RealmsClient.create();
		this.setTitle(this.title);
		int i = 0;

		while (i < 25) {
			try {
				if (this.aborted()) {
					return;
				}

				this.sendResetRequest(realmsClient, this.serverId);
				if (this.aborted()) {
					return;
				}

				this.callback.run();
				return;
			} catch (RetryCallException var4) {
				if (this.aborted()) {
					return;
				}

				pause((long)var4.delaySeconds);
				i++;
			} catch (Exception var5) {
				if (this.aborted()) {
					return;
				}

				LOGGER.error("Couldn't reset world");
				this.error(var5.toString());
				return;
			}
		}
	}
}
