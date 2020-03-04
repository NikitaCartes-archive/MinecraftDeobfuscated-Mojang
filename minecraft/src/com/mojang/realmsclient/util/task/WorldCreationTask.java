package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

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
		String string = I18n.get("mco.create.world.wait");
		this.setTitle(string);
		RealmsClient realmsClient = RealmsClient.create();

		try {
			realmsClient.initializeWorld(this.worldId, this.name, this.motd);
			setScreen(this.lastScreen);
		} catch (RealmsServiceException var4) {
			LOGGER.error("Couldn't create world");
			this.error(var4.toString());
		} catch (Exception var5) {
			LOGGER.error("Could not create world");
			this.error(var5.getLocalizedMessage());
		}
	}
}
