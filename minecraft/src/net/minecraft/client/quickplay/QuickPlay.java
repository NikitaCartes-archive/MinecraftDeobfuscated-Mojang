package net.minecraft.client.quickplay;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class QuickPlay {
	public static final Component ERROR_TITLE = Component.translatable("quickplay.error.title");
	private static final Component INVALID_IDENTIFIER = Component.translatable("quickplay.error.invalid_identifier");
	private static final Component REALM_CONNECT = Component.translatable("quickplay.error.realm_connect");
	private static final Component REALM_PERMISSION = Component.translatable("quickplay.error.realm_permission");
	private static final Component TO_TITLE = Component.translatable("gui.toTitle");
	private static final Component TO_WORLD_LIST = Component.translatable("gui.toWorld");
	private static final Component TO_REALMS_LIST = Component.translatable("gui.toRealms");

	public static void connect(Minecraft minecraft, GameConfig.QuickPlayData quickPlayData, RealmsClient realmsClient) {
		String string = quickPlayData.singleplayer();
		String string2 = quickPlayData.multiplayer();
		String string3 = quickPlayData.realms();
		if (!Util.isBlank(string)) {
			joinSingleplayerWorld(minecraft, string);
		} else if (!Util.isBlank(string2)) {
			joinMultiplayerWorld(minecraft, string2);
		} else if (!Util.isBlank(string3)) {
			joinRealmsWorld(minecraft, realmsClient, string3);
		}
	}

	private static void joinSingleplayerWorld(Minecraft minecraft, String string) {
		if (!minecraft.getLevelSource().levelExists(string)) {
			Screen screen = new SelectWorldScreen(new TitleScreen());
			minecraft.setScreen(new DisconnectedScreen(screen, ERROR_TITLE, INVALID_IDENTIFIER, TO_WORLD_LIST));
		} else {
			minecraft.createWorldOpenFlows().checkForBackupAndLoad(string, () -> minecraft.setScreen(new TitleScreen()));
		}
	}

	private static void joinMultiplayerWorld(Minecraft minecraft, String string) {
		ServerList serverList = new ServerList(minecraft);
		serverList.load();
		ServerData serverData = serverList.get(string);
		if (serverData == null) {
			serverData = new ServerData(I18n.get("selectServer.defaultName"), string, ServerData.Type.OTHER);
			serverList.add(serverData, true);
			serverList.save();
		}

		ServerAddress serverAddress = ServerAddress.parseString(string);
		ConnectScreen.startConnecting(new JoinMultiplayerScreen(new TitleScreen()), minecraft, serverAddress, serverData, true);
	}

	private static void joinRealmsWorld(Minecraft minecraft, RealmsClient realmsClient, String string) {
		long l;
		RealmsServerList realmsServerList;
		try {
			l = Long.parseLong(string);
			realmsServerList = realmsClient.listWorlds();
		} catch (NumberFormatException var9) {
			Screen screen = new RealmsMainScreen(new TitleScreen());
			minecraft.setScreen(new DisconnectedScreen(screen, ERROR_TITLE, INVALID_IDENTIFIER, TO_REALMS_LIST));
			return;
		} catch (RealmsServiceException var10) {
			Screen screenx = new TitleScreen();
			minecraft.setScreen(new DisconnectedScreen(screenx, ERROR_TITLE, REALM_CONNECT, TO_TITLE));
			return;
		}

		RealmsServer realmsServer = (RealmsServer)realmsServerList.servers.stream().filter(realmsServerx -> realmsServerx.id == l).findFirst().orElse(null);
		if (realmsServer == null) {
			Screen screen = new RealmsMainScreen(new TitleScreen());
			minecraft.setScreen(new DisconnectedScreen(screen, ERROR_TITLE, REALM_PERMISSION, TO_REALMS_LIST));
		} else {
			TitleScreen titleScreen = new TitleScreen();
			GetServerDetailsTask getServerDetailsTask = new GetServerDetailsTask(titleScreen, realmsServer);
			minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(titleScreen, getServerDetailsTask));
		}
	}
}
