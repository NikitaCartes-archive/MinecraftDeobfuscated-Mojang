package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmCreationTask extends LongRunningTask {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component TITLE = Component.translatable("mco.create.world.wait");
	private final String name;
	private final String motd;
	private final long realmId;

	public RealmCreationTask(long l, String string, String string2) {
		this.realmId = l;
		this.name = string;
		this.motd = string2;
	}

	public void run() {
		RealmsClient realmsClient = RealmsClient.create();

		try {
			realmsClient.initializeRealm(this.realmId, this.name, this.motd);
		} catch (RealmsServiceException var3) {
			LOGGER.error("Couldn't create world", (Throwable)var3);
			this.error(var3);
		} catch (Exception var4) {
			LOGGER.error("Could not create world", (Throwable)var4);
			this.error(var4);
		}
	}

	@Override
	public Component getTitle() {
		return TITLE;
	}
}
