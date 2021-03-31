package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
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
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.network.chat.TranslatableComponent;
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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientPackSource implements RepositorySource {
	private static final PackMetadataSection BUILT_IN = new PackMetadataSection(
		new TranslatableComponent("resourcePack.vanilla.description"), PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion())
	);
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
	private static final int MAX_WEB_FILESIZE = 104857600;
	private static final int MAX_KEPT_PACKS = 10;
	private static final String VANILLA_ID = "vanilla";
	private static final String SERVER_ID = "server";
	private static final String PROGRAMMER_ART_ID = "programer_art";
	private static final String PROGRAMMER_ART_NAME = "Programmer Art";
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

	public CompletableFuture<?> downloadAndSelectResourcePack(String string, String string2) {
		String string3 = DigestUtils.sha1Hex(string);
		String string4 = SHA1.matcher(string2).matches() ? string2 : "";
		this.downloadLock.lock();

		CompletableFuture var13;
		try {
			this.clearServerPack();
			this.clearOldDownloads();
			File file = new File(this.serverPackDir, string3);
			CompletableFuture<?> completableFuture;
			if (file.exists()) {
				completableFuture = CompletableFuture.completedFuture("");
			} else {
				ProgressScreen progressScreen = new ProgressScreen();
				Map<String, String> map = getDownloadHeaders();
				Minecraft minecraft = Minecraft.getInstance();
				minecraft.executeBlocking(() -> minecraft.setScreen(progressScreen));
				completableFuture = HttpUtil.downloadTo(file, string, map, 104857600, progressScreen, minecraft.getProxy());
			}

			this.currentDownload = completableFuture.thenCompose(
					object -> !this.checkHash(string4, file)
							? Util.failedFuture(new RuntimeException("Hash check failure for file " + file + ", see log"))
							: this.setServerPack(file, PackSource.SERVER)
				)
				.whenComplete((void_, throwable) -> {
					if (throwable != null) {
						LOGGER.warn("Pack application failed: {}, deleting file {}", throwable.getMessage(), file);
						deleteQuietly(file);
					}
				});
			var13 = this.currentDownload;
		} finally {
			this.downloadLock.unlock();
		}

		return var13;
	}

	private static void deleteQuietly(File file) {
		try {
			Files.delete(file.toPath());
		} catch (IOException var2) {
			LOGGER.warn("Failed to delete file {}: {}", file, var2.getMessage());
		}
	}

	public void clearServerPack() {
		this.downloadLock.lock();

		try {
			if (this.currentDownload != null) {
				this.currentDownload.cancel(true);
			}

			this.currentDownload = null;
			if (this.serverPack != null) {
				this.serverPack = null;
				Minecraft.getInstance().delayTextureReload();
			}
		} finally {
			this.downloadLock.unlock();
		}
	}

	private boolean checkHash(String string, File file) {
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			Throwable var5 = null;

			String string2;
			try {
				string2 = DigestUtils.sha1Hex(fileInputStream);
			} catch (Throwable var15) {
				var5 = var15;
				throw var15;
			} finally {
				if (fileInputStream != null) {
					if (var5 != null) {
						try {
							fileInputStream.close();
						} catch (Throwable var14) {
							var5.addSuppressed(var14);
						}
					} else {
						fileInputStream.close();
					}
				}
			}

			if (string.isEmpty()) {
				LOGGER.info("Found file {} without verification hash", file);
				return true;
			}

			if (string2.toLowerCase(Locale.ROOT).equals(string.toLowerCase(Locale.ROOT))) {
				LOGGER.info("Found file {} matching requested hash {}", file, string);
				return true;
			}

			LOGGER.warn("File {} had wrong hash (expected {}, found {}).", file, string, string2);
		} catch (IOException var17) {
			LOGGER.warn("File {} couldn't be hashed.", file, var17);
		}

		return false;
	}

	private void clearOldDownloads() {
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
		} catch (IllegalArgumentException var5) {
			LOGGER.error("Error while deleting old server resource pack : {}", var5.getMessage());
		}
	}

	public CompletableFuture<Void> setServerPack(File file, PackSource packSource) {
		PackMetadataSection packMetadataSection;
		try (FilePackResources filePackResources = new FilePackResources(file)) {
			packMetadataSection = filePackResources.getMetadataSection(PackMetadataSection.SERIALIZER);
		} catch (IOException var17) {
			return Util.failedFuture(new IOException(String.format("Invalid resourcepack at %s", file), var17));
		}

		LOGGER.info("Applying server pack {}", file);
		this.serverPack = new Pack(
			"server",
			true,
			() -> new FilePackResources(file),
			new TranslatableComponent("resourcePack.server.name"),
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
