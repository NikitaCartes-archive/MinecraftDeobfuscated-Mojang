package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.platform.DisplayData;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.User;
import net.minecraft.client.resources.IndexedAssetSource;

@Environment(EnvType.CLIENT)
public class GameConfig {
	public final GameConfig.UserData user;
	public final DisplayData display;
	public final GameConfig.FolderData location;
	public final GameConfig.GameData game;
	public final GameConfig.ServerData server;

	public GameConfig(
		GameConfig.UserData userData, DisplayData displayData, GameConfig.FolderData folderData, GameConfig.GameData gameData, GameConfig.ServerData serverData
	) {
		this.user = userData;
		this.display = displayData;
		this.location = folderData;
		this.game = gameData;
		this.server = serverData;
	}

	@Environment(EnvType.CLIENT)
	public static class FolderData {
		public final File gameDirectory;
		public final File resourcePackDirectory;
		public final File assetDirectory;
		@Nullable
		public final String assetIndex;

		public FolderData(File file, File file2, File file3, @Nullable String string) {
			this.gameDirectory = file;
			this.resourcePackDirectory = file2;
			this.assetDirectory = file3;
			this.assetIndex = string;
		}

		public Path getExternalAssetSource() {
			return this.assetIndex == null ? this.assetDirectory.toPath() : IndexedAssetSource.createIndexFs(this.assetDirectory.toPath(), this.assetIndex);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class GameData {
		public final boolean demo;
		public final String launchVersion;
		public final String versionType;
		public final boolean disableMultiplayer;
		public final boolean disableChat;

		public GameData(boolean bl, String string, String string2, boolean bl2, boolean bl3) {
			this.demo = bl;
			this.launchVersion = string;
			this.versionType = string2;
			this.disableMultiplayer = bl2;
			this.disableChat = bl3;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class ServerData {
		@Nullable
		public final String hostname;
		public final int port;

		public ServerData(@Nullable String string, int i) {
			this.hostname = string;
			this.port = i;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class UserData {
		public final User user;
		public final PropertyMap userProperties;
		public final PropertyMap profileProperties;
		public final Proxy proxy;

		public UserData(User user, PropertyMap propertyMap, PropertyMap propertyMap2, Proxy proxy) {
			this.user = user;
			this.userProperties = propertyMap;
			this.profileProperties = propertyMap2;
			this.proxy = proxy;
		}
	}
}
