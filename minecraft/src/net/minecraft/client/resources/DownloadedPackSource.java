package net.minecraft.client.resources;

import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import com.mojang.util.UndashedUuid;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class DownloadedPackSource implements RepositorySource {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
	private static final int MAX_PACK_SIZE_BYTES = 262144000;
	private static final int MAX_KEPT_PACKS = 10;
	private static final String SERVER_ID = "server";
	private static final Component SERVER_NAME = Component.translatable("resourcePack.server.name");
	private static final Component APPLYING_PACK_TEXT = Component.translatable("multiplayer.applyingPack");
	private final File serverPackDir;
	private final ReentrantLock downloadLock = new ReentrantLock();
	@Nullable
	private CompletableFuture<?> currentDownload;
	@Nullable
	private Pack serverPack;

	public DownloadedPackSource(File file) {
		this.serverPackDir = file;
	}

	@Override
	public void loadPacks(Consumer<Pack> consumer) {
		if (this.serverPack != null) {
			consumer.accept(this.serverPack);
		}
	}

	private static Map<String, String> getDownloadHeaders() {
		return Map.of(
			"X-Minecraft-Username",
			Minecraft.getInstance().getUser().getName(),
			"X-Minecraft-UUID",
			UndashedUuid.toString(Minecraft.getInstance().getUser().getProfileId()),
			"X-Minecraft-Version",
			SharedConstants.getCurrentVersion().getName(),
			"X-Minecraft-Version-ID",
			SharedConstants.getCurrentVersion().getId(),
			"X-Minecraft-Pack-Format",
			String.valueOf(SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES)),
			"User-Agent",
			"Minecraft Java/" + SharedConstants.getCurrentVersion().getName()
		);
	}

	public CompletableFuture<?> downloadAndSelectResourcePack(URL uRL, String string, boolean bl) {
		String string2 = Hashing.sha1().hashString(uRL.toString(), StandardCharsets.UTF_8).toString();
		String string3 = SHA1.matcher(string).matches() ? string : "";
		this.downloadLock.lock();

		CompletableFuture var14;
		try {
			Minecraft minecraft = Minecraft.getInstance();
			File file = new File(this.serverPackDir, string2);
			CompletableFuture<?> completableFuture;
			if (file.exists()) {
				completableFuture = CompletableFuture.completedFuture("");
			} else {
				ProgressScreen progressScreen = new ProgressScreen(bl);
				Map<String, String> map = getDownloadHeaders();
				minecraft.executeBlocking(() -> minecraft.setScreen(progressScreen));
				completableFuture = HttpUtil.downloadTo(file, uRL, map, 262144000, progressScreen, minecraft.getProxy());
			}

			this.currentDownload = completableFuture.thenCompose(object -> {
					if (!this.checkHash(string3, file)) {
						return CompletableFuture.failedFuture(new RuntimeException("Hash check failure for file " + file + ", see log"));
					} else {
						minecraft.execute(() -> {
							if (!bl) {
								minecraft.setScreen(new GenericDirtMessageScreen(APPLYING_PACK_TEXT));
							}
						});
						return this.setServerPack(file, PackSource.SERVER);
					}
				})
				.exceptionallyCompose(
					throwable -> this.clearServerPack()
							.thenAcceptAsync(void_ -> {
								LOGGER.warn("Pack application failed: {}, deleting file {}", throwable.getMessage(), file);
								deleteQuietly(file);
							}, Util.ioPool())
							.thenAcceptAsync(
								void_ -> minecraft.setScreen(
										new ConfirmScreen(
											blx -> {
												if (blx) {
													minecraft.setScreen(null);
												} else {
													ClientPacketListener clientPacketListener = minecraft.getConnection();
													if (clientPacketListener != null) {
														clientPacketListener.getConnection().disconnect(Component.translatable("connect.aborted"));
													}
												}
											},
											Component.translatable("multiplayer.texturePrompt.failure.line1"),
											Component.translatable("multiplayer.texturePrompt.failure.line2"),
											CommonComponents.GUI_PROCEED,
											Component.translatable("menu.disconnect")
										)
									),
								minecraft
							)
				)
				.thenAcceptAsync(void_ -> this.clearOldDownloads(), Util.ioPool());
			var14 = this.currentDownload;
		} finally {
			this.downloadLock.unlock();
		}

		return var14;
	}

	private static void deleteQuietly(File file) {
		try {
			Files.delete(file.toPath());
		} catch (IOException var2) {
			LOGGER.warn("Failed to delete file {}: {}", file, var2.getMessage());
		}
	}

	public CompletableFuture<Void> clearServerPack() {
		this.downloadLock.lock();

		try {
			if (this.currentDownload != null) {
				this.currentDownload.cancel(true);
			}

			this.currentDownload = null;
			if (this.serverPack != null) {
				this.serverPack = null;
				return Minecraft.getInstance().delayTextureReload();
			}
		} finally {
			this.downloadLock.unlock();
		}

		return CompletableFuture.completedFuture(null);
	}

	private boolean checkHash(String string, File file) {
		try {
			String string2 = com.google.common.io.Files.asByteSource(file).hash(Hashing.sha1()).toString();
			if (string.isEmpty()) {
				LOGGER.info("Found file {} without verification hash", file);
				return true;
			}

			if (string2.toLowerCase(Locale.ROOT).equals(string.toLowerCase(Locale.ROOT))) {
				LOGGER.info("Found file {} matching requested hash {}", file, string);
				return true;
			}

			LOGGER.warn("File {} had wrong hash (expected {}, found {}).", file, string, string2);
		} catch (IOException var4) {
			LOGGER.warn("File {} couldn't be hashed.", file, var4);
		}

		return false;
	}

	private void clearOldDownloads() {
		if (this.serverPackDir.isDirectory()) {
			try {
				List<File> list = new ArrayList(FileUtils.listFiles(this.serverPackDir, TrueFileFilter.TRUE, null));
				list.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
				int i = 0;

				for (File file : list) {
					if (i++ >= 10) {
						LOGGER.info("Deleting old server resource pack {}", file.getName());
						FileUtils.deleteQuietly(file);
					}
				}
			} catch (Exception var5) {
				LOGGER.error("Error while deleting old server resource pack : {}", var5.getMessage());
			}
		}
	}

	public CompletableFuture<Void> setServerPack(File file, PackSource packSource) {
		Pack.ResourcesSupplier resourcesSupplier = new FilePackResources.FileResourcesSupplier(file, false);
		int i = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
		Pack.Info info = Pack.readPackInfo("server", resourcesSupplier, i);
		if (info == null) {
			return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid pack metadata at " + file));
		} else {
			LOGGER.info("Applying server pack {}", file);
			this.serverPack = Pack.create("server", SERVER_NAME, true, resourcesSupplier, info, Pack.Position.TOP, true, packSource);
			return Minecraft.getInstance().delayTextureReload();
		}
	}

	public CompletableFuture<Void> loadBundledResourcePack(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		Path path = levelStorageAccess.getLevelPath(LevelResource.MAP_RESOURCE_FILE);
		return Files.exists(path, new LinkOption[0]) && !Files.isDirectory(path, new LinkOption[0])
			? this.setServerPack(path.toFile(), PackSource.WORLD)
			: CompletableFuture.completedFuture(null);
	}
}
