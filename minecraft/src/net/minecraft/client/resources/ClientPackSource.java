package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
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
public class ClientPackSource implements RepositorySource {
	private static final PackMetadataSection BUILT_IN = new PackMetadataSection(
		Component.translatable("resourcePack.vanilla.description"), PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion())
	);
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
	private static final int MAX_PACK_SIZE_BYTES = 262144000;
	private static final int MAX_KEPT_PACKS = 10;
	private static final String VANILLA_ID = "vanilla";
	private static final String SERVER_ID = "server";
	private static final String PROGRAMMER_ART_ID = "programer_art";
	private static final String PROGRAMMER_ART_NAME = "Programmer Art";
	private static final Component APPLYING_PACK_TEXT = Component.translatable("multiplayer.applyingPack");
	private final VanillaPackResources vanillaPack;
	private final File serverPackDir;
	private final ReentrantLock downloadLock = new ReentrantLock();
	private final AssetIndex assetIndex;
	@Nullable
	private CompletableFuture<?> currentDownload;
	@Nullable
	private Pack serverPack;

	public ClientPackSource(File file, AssetIndex assetIndex) {
		this.serverPackDir = file;
		this.assetIndex = assetIndex;
		this.vanillaPack = new DefaultClientPackResources(BUILT_IN, assetIndex);
	}

	@Override
	public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor packConstructor) {
		Pack pack = Pack.create("vanilla", true, () -> this.vanillaPack, packConstructor, Pack.Position.BOTTOM, PackSource.BUILT_IN);
		if (pack != null) {
			consumer.accept(pack);
		}

		if (this.serverPack != null) {
			consumer.accept(this.serverPack);
		}

		Pack pack2 = this.createProgrammerArtPack(packConstructor);
		if (pack2 != null) {
			consumer.accept(pack2);
		}
	}

	public VanillaPackResources getVanillaPack() {
		return this.vanillaPack;
	}

	private static Map<String, String> getDownloadHeaders() {
		Map<String, String> map = Maps.<String, String>newHashMap();
		map.put("X-Minecraft-Username", Minecraft.getInstance().getUser().getName());
		map.put("X-Minecraft-UUID", Minecraft.getInstance().getUser().getUuid());
		map.put("X-Minecraft-Version", SharedConstants.getCurrentVersion().getName());
		map.put("X-Minecraft-Version-ID", SharedConstants.getCurrentVersion().getId());
		map.put("X-Minecraft-Pack-Format", String.valueOf(PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion())));
		map.put("User-Agent", "Minecraft Java/" + SharedConstants.getCurrentVersion().getName());
		return map;
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
						return Util.failedFuture(new RuntimeException("Hash check failure for file " + file + ", see log"));
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
				List<File> list = Lists.<File>newArrayList(FileUtils.listFiles(this.serverPackDir, TrueFileFilter.TRUE, null));
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

	public CompletableFuture<Void> loadBundledResourcePack(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		Path path = levelStorageAccess.getLevelPath(LevelResource.MAP_RESOURCE_FILE);
		return Files.exists(path, new LinkOption[0]) && !Files.isDirectory(path, new LinkOption[0])
			? this.setServerPack(path.toFile(), PackSource.WORLD)
			: CompletableFuture.completedFuture(null);
	}

	public CompletableFuture<Void> setServerPack(File file, PackSource packSource) {
		PackMetadataSection packMetadataSection;
		try (FilePackResources filePackResources = new FilePackResources(file)) {
			packMetadataSection = filePackResources.getMetadataSection(PackMetadataSection.SERIALIZER);
		} catch (IOException var9) {
			return Util.failedFuture(new IOException(String.format(Locale.ROOT, "Invalid resourcepack at %s", file), var9));
		}

		LOGGER.info("Applying server pack {}", file);
		this.serverPack = new Pack(
			"server",
			true,
			() -> new FilePackResources(file),
			Component.translatable("resourcePack.server.name"),
			packMetadataSection.getDescription(),
			PackCompatibility.forMetadata(packMetadataSection, PackType.CLIENT_RESOURCES),
			Pack.Position.TOP,
			true,
			packSource
		);
		return Minecraft.getInstance().delayTextureReload();
	}

	@Nullable
	private Pack createProgrammerArtPack(Pack.PackConstructor packConstructor) {
		Pack pack = null;
		File file = this.assetIndex.getFile(new ResourceLocation("resourcepacks/programmer_art.zip"));
		if (file != null && file.isFile()) {
			pack = createProgrammerArtPack(packConstructor, () -> createProgrammerArtZipPack(file));
		}

		if (pack == null && SharedConstants.IS_RUNNING_IN_IDE) {
			File file2 = this.assetIndex.getRootFile("../resourcepacks/programmer_art");
			if (file2 != null && file2.isDirectory()) {
				pack = createProgrammerArtPack(packConstructor, () -> createProgrammerArtDirPack(file2));
			}
		}

		return pack;
	}

	@Nullable
	private static Pack createProgrammerArtPack(Pack.PackConstructor packConstructor, Supplier<PackResources> supplier) {
		return Pack.create("programer_art", false, supplier, packConstructor, Pack.Position.TOP, PackSource.BUILT_IN);
	}

	private static FolderPackResources createProgrammerArtDirPack(File file) {
		return new FolderPackResources(file) {
			@Override
			public String getName() {
				return "Programmer Art";
			}
		};
	}

	private static PackResources createProgrammerArtZipPack(File file) {
		return new FilePackResources(file) {
			@Override
			public String getName() {
				return "Programmer Art";
			}
		};
	}
}
