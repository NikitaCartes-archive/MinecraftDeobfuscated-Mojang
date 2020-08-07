package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class WorldCreationTask extends LongRunningTask {
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
		this.setTitle(new TranslatableComponent("mco.create.world.wait"));
		RealmsClient realmsClient = RealmsClient.create();

		try {
			realmsClient.initializeWorld(this.worldId, this.name, this.motd);
			setScreen(this.lastScreen);
		} catch (RealmsServiceException var3) {
			LOGGER.error("Couldn't create world");
			this.error(var3.toString());
		} catch (Exception var4) {
			LOGGER.error("Could not create world");
			this.error(var4.getLocalizedMessage());
		}
	}
}
