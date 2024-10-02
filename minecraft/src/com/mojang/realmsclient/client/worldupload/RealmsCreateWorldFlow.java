package com.mojang.realmsclient.client.worldupload;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.util.task.RealmCreationTask;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsCreateWorldFlow {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void createWorld(
		Minecraft minecraft, Screen screen, Screen screen2, int i, RealmsServer realmsServer, @Nullable RealmCreationTask realmCreationTask
	) {
		CreateWorldScreen.openFresh(
			minecraft,
			screen,
			(createWorldScreen, layeredRegistryAccess, primaryLevelData, path) -> {
				Path path2;
				try {
					path2 = createTemporaryWorldFolder(layeredRegistryAccess, primaryLevelData, path);
				} catch (IOException var13) {
					LOGGER.warn("Failed to create temporary world folder.");
					minecraft.setScreen(new RealmsGenericErrorScreen(Component.translatable("mco.create.world.failed"), screen2));
					return true;
				}

				RealmsWorldOptions realmsWorldOptions = RealmsWorldOptions.createFromSettings(
					primaryLevelData.getLevelSettings(), SharedConstants.getCurrentVersion().getName()
				);
				RealmsWorldUpload realmsWorldUpload = new RealmsWorldUpload(
					path2, realmsWorldOptions, minecraft.getUser(), realmsServer.id, i, RealmsWorldUploadStatusTracker.noOp()
				);
				minecraft.forceSetScreen(
					new AlertScreen(realmsWorldUpload::cancel, Component.translatable("mco.create.world.reset.title"), Component.empty(), CommonComponents.GUI_CANCEL, false)
				);
				if (realmCreationTask != null) {
					realmCreationTask.run();
				}

				realmsWorldUpload.packAndUpload().handleAsync((object, throwable) -> {
					if (throwable != null) {
						if (throwable instanceof CompletionException completionException) {
							throwable = completionException.getCause();
						}

						if (throwable instanceof RealmsUploadCanceledException) {
							minecraft.forceSetScreen(screen2);
						} else {
							if (throwable instanceof RealmsUploadFailedException realmsUploadFailedException) {
								LOGGER.warn("Failed to create realms world {}", realmsUploadFailedException.getStatusMessage());
							} else {
								LOGGER.warn("Failed to create realms world {}", throwable.getMessage());
							}

							minecraft.forceSetScreen(new RealmsGenericErrorScreen(Component.translatable("mco.create.world.failed"), screen2));
						}
					} else {
						if (screen instanceof RealmsConfigureWorldScreen realmsConfigureWorldScreen) {
							realmsConfigureWorldScreen.fetchServerData(realmsServer.id);
						}

						if (realmCreationTask != null) {
							RealmsMainScreen.play(realmsServer, screen, true);
						} else {
							minecraft.forceSetScreen(screen);
						}

						RealmsMainScreen.refreshServerList();
					}

					return null;
				}, minecraft);
				return true;
			}
		);
	}

	private static Path createTemporaryWorldFolder(
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PrimaryLevelData primaryLevelData, @Nullable Path path
	) throws IOException {
		Path path2 = Files.createTempDirectory("minecraft_realms_world_upload");
		if (path != null) {
			Files.move(path, path2.resolve("datapacks"));
		}

		CompoundTag compoundTag = primaryLevelData.createTag(layeredRegistryAccess.compositeAccess(), null);
		CompoundTag compoundTag2 = new CompoundTag();
		compoundTag2.put("Data", compoundTag);
		Path path3 = Files.createFile(path2.resolve("level.dat"));
		NbtIo.writeCompressed(compoundTag2, path3);
		return path2;
	}
}
