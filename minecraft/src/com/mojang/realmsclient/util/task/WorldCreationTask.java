package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldCreationTask extends LongRunningTask {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component TITLE = Component.translatable("mco.create.world.wait");
	private final String name;
	private final String motd;
	private final long worldId;
	private final Screen lastScreen;

	public WorldCreationTask(long l, String string, String string2, Screen screen) {
		this.worldId = l;
		this.name = string;
		this.motd = string2;
		this.lastScreen = screen;
	}

	public void run() {
		RealmsClient realmsClient = RealmsClient.create();

		try {
			realmsClient.initializeWorld(this.worldId, this.name, this.motd);
			setScreen(this.lastScreen);
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
