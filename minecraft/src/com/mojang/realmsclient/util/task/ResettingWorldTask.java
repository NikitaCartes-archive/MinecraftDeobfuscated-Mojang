package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RetryCallException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

@Environment(EnvType.CLIENT)
public class ResettingWorldTask extends LongRunningTask {
	private final String seed;
	private final WorldTemplate worldTemplate;
	private final int levelType;
	private final boolean generateStructures;
	private final long serverId;
	private String title = I18n.get("mco.reset.world.resetting.screen.title");
	private final Runnable callback;

	public ResettingWorldTask(
		@Nullable String string, @Nullable WorldTemplate worldTemplate, int i, boolean bl, long l, @Nullable String string2, Runnable runnable
	) {
		this.seed = string;
		this.worldTemplate = worldTemplate;
		this.levelType = i;
		this.generateStructures = bl;
		this.serverId = l;
		if (string2 != null) {
			this.title = string2;
		}

		this.callback = runnable;
	}

	public void run() {
		RealmsClient realmsClient = RealmsClient.create();
		this.setTitle(this.title);
		int i = 0;

		while (i < 25) {
			try {
				if (this.aborted()) {
					return;
				}

				if (this.worldTemplate != null) {
					realmsClient.resetWorldWithTemplate(this.serverId, this.worldTemplate.id);
				} else {
					realmsClient.resetWorldWithSeed(this.serverId, this.seed, this.levelType, this.generateStructures);
				}

				if (this.aborted()) {
					return;
				}

				this.callback.run();
				return;
			} catch (RetryCallException var4) {
				if (this.aborted()) {
					return;
				}

				pause(var4.delaySeconds);
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
