package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
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
import net.minecraft.server.packs.FileResourcePack;
import net.minecraft.server.packs.VanillaPack;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.UnopenedPack;
import net.minecraft.util.HttpUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientPackSource implements RepositorySource {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
	private final VanillaPack vanillaPack;
	private final File serverPackDir;
	private final ReentrantLock downloadLock = new ReentrantLock();
	private final AssetIndex assetIndex;
	@Nullable
	private CompletableFuture<?> currentDownload;
	@Nullable
	private UnopenedResourcePack serverPack;

	public ClientPackSource(File file, AssetIndex assetIndex) {
		this.serverPackDir = file;
		this.assetIndex = assetIndex;
		this.vanillaPack = new DefaultClientResourcePack(assetIndex);
	}

	@Override
	public <T extends UnopenedPack> void loadPacks(Map<String, T> map, UnopenedPack.UnopenedPackConstructor<T> unopenedPackConstructor) {
		T unopenedPack = UnopenedPack.create("vanilla", true, () -> this.vanillaPack, unopenedPackConstructor, UnopenedPack.Position.BOTTOM);
		if (unopenedPack != null) {
			map.put("vanilla", unopenedPack);
		}

		if (this.serverPack != null) {
			map.put("server", this.serverPack);
		}

		File file = this.assetIndex.getFile(new ResourceLocation("resourcepacks/programmer_art.zip"));
		if (file != null && file.isFile()) {
			T unopenedPack2 = UnopenedPack.create("programer_art", false, () -> new FileResourcePack(file) {
					@Override
					public String getName() {
						return "Programmer Art";
					}
				}, unopenedPackConstructor, UnopenedPack.Position.TOP);
			if (unopenedPack2 != null) {
				map.put("programer_art", unopenedPack2);
			}
		}
	}

	public VanillaPack getVanillaPack() {
		return this.vanillaPack;
	}

	public static Map<String, String> getDownloadHeaders() {
		Map<String, String> map = Maps.<String, String>newHashMap();
		map.put("X-Minecraft-Username", Minecraft.getInstance().getUser().getName());
		map.put("X-Minecraft-UUID", Minecraft.getInstance().getUser().getUuid());
		map.put("X-Minecraft-Version", SharedConstants.getCurrentVersion().getName());
		map.put("X-Minecraft-Version-ID", SharedConstants.getCurrentVersion().getId());
		map.put("X-Minecraft-Pack-Format", String.valueOf(SharedConstants.getCurrentVersion().getPackVersion()));
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
							: this.setServerPack(file)
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

	public CompletableFuture<Void> setServerPack(File file) {
		PackMetadataSection packMetadataSection = null;
		NativeImage nativeImage = null;
		String string = null;

		try {
			FileResourcePack fileResourcePack = new FileResourcePack(file);
			Throwable var6 = null;

			try {
				packMetadataSection = fileResourcePack.getMetadataSection(PackMetadataSection.SERIALIZER);

				try {
					InputStream inputStream = fileResourcePack.getRootResource("pack.png");
					Throwable var8 = null;

					try {
						nativeImage = NativeImage.read(inputStream);
					} catch (Throwable var35) {
						var8 = var35;
						throw var35;
					} finally {
						if (inputStream != null) {
							if (var8 != null) {
								try {
									inputStream.close();
								} catch (Throwable var34) {
									var8.addSuppressed(var34);
								}
							} else {
								inputStream.close();
							}
						}
					}
				} catch (IllegalArgumentException | IOException var37) {
					LOGGER.info("Could not read pack.png: {}", var37.getMessage());
				}
			} catch (Throwable var38) {
				var6 = var38;
				throw var38;
			} finally {
				if (fileResourcePack != null) {
					if (var6 != null) {
						try {
							fileResourcePack.close();
						} catch (Throwable var33) {
							var6.addSuppressed(var33);
						}
					} else {
						fileResourcePack.close();
					}
				}
			}
		} catch (IOException var40) {
			string = var40.getMessage();
		}

		if (string != null) {
			return Util.failedFuture(new RuntimeException(String.format("Invalid resourcepack at %s: %s", file, string)));
		} else {
			LOGGER.info("Applying server pack {}", file);
			this.serverPack = new UnopenedResourcePack(
				"server",
				true,
				() -> new FileResourcePack(file),
				new TranslatableComponent("resourcePack.server.name"),
				packMetadataSection.getDescription(),
				PackCompatibility.forFormat(packMetadataSection.getPackFormat()),
				UnopenedPack.Position.TOP,
				true,
				nativeImage
			);
			return Minecraft.getInstance().delayTextureReload();
		}
	}
}
