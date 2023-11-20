package net.minecraft.client.resources.server;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.Unit;
import com.mojang.util.UndashedUuid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.main.GameConfig;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.packs.DownloadQueue;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.HttpUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class DownloadedPackSource implements AutoCloseable {
	private static final Component SERVER_NAME = Component.translatable("resourcePack.server.name");
	private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
	static final Logger LOGGER = LogUtils.getLogger();
	private static final RepositorySource EMPTY_SOURCE = consumer -> {
	};
	private static final PackLoadFeedback LOG_ONLY_FEEDBACK = (uUID, result) -> LOGGER.debug("Downloaded pack {} changed state to {}", uUID, result);
	final Minecraft minecraft;
	private RepositorySource packSource = EMPTY_SOURCE;
	@Nullable
	private PackReloadConfig.Callbacks pendingReload;
	final ServerPackManager manager;
	private final DownloadQueue downloadQueue;
	private PackSource packType = PackSource.SERVER;
	private PackLoadFeedback packFeedback = LOG_ONLY_FEEDBACK;

	public DownloadedPackSource(Minecraft minecraft, Path path, GameConfig.UserData userData) {
		this.minecraft = minecraft;

		try {
			this.downloadQueue = new DownloadQueue(path);
		} catch (IOException var5) {
			throw new UncheckedIOException("Failed to open download queue in directory " + path, var5);
		}

		Executor executor = minecraft::tell;
		this.manager = new ServerPackManager(
			this.createDownloader(this.downloadQueue, executor, userData.user, userData.proxy),
			(uUID, result) -> this.packFeedback.sendResponse(uUID, result),
			this.createReloadConfig(),
			this.createUpdateScheduler(executor),
			ServerPackManager.PackPromptStatus.PENDING
		);
	}

	HttpUtil.DownloadProgressListener createDownloadNotifier(int i) {
		return new HttpUtil.DownloadProgressListener() {
			private final SystemToast.SystemToastId toastId = new SystemToast.SystemToastId(10000L);
			private Component title = Component.empty();
			@Nullable
			private Component message = null;
			private int count;
			private OptionalLong totalBytes = OptionalLong.empty();

			private void updateToast() {
				SystemToast.addOrUpdate(DownloadedPackSource.this.minecraft.getToasts(), this.toastId, this.title, this.message);
			}

			private void updateProgress(long l) {
				if (this.totalBytes.isPresent()) {
					this.message = Component.translatable("download.pack.progress.percent", l * 100L / this.totalBytes.getAsLong());
				} else {
					this.message = Component.translatable("download.pack.progress.bytes", Unit.humanReadable(l));
				}

				this.updateToast();
			}

			@Override
			public void requestStart() {
				this.count++;
				this.title = Component.translatable("download.pack.title", this.count, i);
				this.updateToast();
				DownloadedPackSource.LOGGER.debug("Starting pack {}/{} download", this.count, i);
			}

			@Override
			public void downloadStart(OptionalLong optionalLong) {
				DownloadedPackSource.LOGGER.debug("File size = {} bytes", optionalLong);
				this.totalBytes = optionalLong;
				this.updateProgress(0L);
			}

			@Override
			public void downloadedBytes(long l) {
				DownloadedPackSource.LOGGER.debug("Progress for pack {}: {} bytes", this.count, l);
				this.updateProgress(l);
			}

			@Override
			public void requestFinished() {
				DownloadedPackSource.LOGGER.debug("Download ended for pack {}", this.count);
				if (this.count == i) {
					SystemToast.forceHide(DownloadedPackSource.this.minecraft.getToasts(), this.toastId);
				}
			}
		};
	}

	private PackDownloader createDownloader(DownloadQueue downloadQueue, Executor executor, User user, Proxy proxy) {
		return new PackDownloader() {
			private static final int MAX_PACK_SIZE_BYTES = 262144000;
			private static final HashFunction CACHE_HASHING_FUNCTION = Hashing.sha1();

			private Map<String, String> createDownloadHeaders() {
				WorldVersion worldVersion = SharedConstants.getCurrentVersion();
				return Map.of(
					"X-Minecraft-Username",
					user.getName(),
					"X-Minecraft-UUID",
					UndashedUuid.toString(user.getProfileId()),
					"X-Minecraft-Version",
					worldVersion.getName(),
					"X-Minecraft-Version-ID",
					worldVersion.getId(),
					"X-Minecraft-Pack-Format",
					String.valueOf(worldVersion.getPackVersion(PackType.CLIENT_RESOURCES)),
					"User-Agent",
					"Minecraft Java/" + worldVersion.getName()
				);
			}

			@Override
			public void download(Map<UUID, DownloadQueue.DownloadRequest> map, Consumer<DownloadQueue.BatchResult> consumer) {
				downloadQueue.downloadBatch(
						new DownloadQueue.BatchConfig(
							CACHE_HASHING_FUNCTION, 262144000, this.createDownloadHeaders(), proxy, DownloadedPackSource.this.createDownloadNotifier(map.size())
						),
						map
					)
					.thenAcceptAsync(consumer, executor);
			}
		};
	}

	private Runnable createUpdateScheduler(Executor executor) {
		return new Runnable() {
			private boolean scheduledInMainExecutor;
			private boolean hasUpdates;

			public void run() {
				this.hasUpdates = true;
				if (!this.scheduledInMainExecutor) {
					this.scheduledInMainExecutor = true;
					executor.execute(this::runAllUpdates);
				}
			}

			private void runAllUpdates() {
				while (this.hasUpdates) {
					this.hasUpdates = false;
					DownloadedPackSource.this.manager.tick();
				}

				this.scheduledInMainExecutor = false;
			}
		};
	}

	private PackReloadConfig createReloadConfig() {
		return this::startReload;
	}

	@Nullable
	private List<Pack> loadRequestedPacks(List<PackReloadConfig.IdAndPath> list) {
		List<Pack> list2 = new ArrayList(list.size());

		for (PackReloadConfig.IdAndPath idAndPath : list) {
			String string = "server/" + idAndPath.id();
			Path path = idAndPath.path();
			Pack.ResourcesSupplier resourcesSupplier = new FilePackResources.FileResourcesSupplier(path, false);
			int i = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
			Pack.Info info = Pack.readPackInfo(string, resourcesSupplier, i);
			if (info == null) {
				LOGGER.warn("Invalid pack metadata in {}, ignoring all", path);
				return null;
			}

			list2.add(Pack.create(string, SERVER_NAME, true, resourcesSupplier, info, Pack.Position.TOP, true, this.packType));
		}

		return list2;
	}

	public RepositorySource createRepositorySource() {
		return consumer -> this.packSource.loadPacks(consumer);
	}

	private static RepositorySource configureSource(List<Pack> list) {
		return list.isEmpty() ? EMPTY_SOURCE : list::forEach;
	}

	private void startReload(PackReloadConfig.Callbacks callbacks) {
		this.pendingReload = callbacks;
		List<PackReloadConfig.IdAndPath> list = callbacks.packsToLoad();
		List<Pack> list2 = this.loadRequestedPacks(list);
		if (list2 == null) {
			callbacks.onFailure(false);
			List<PackReloadConfig.IdAndPath> list3 = callbacks.packsToLoad();
			list2 = this.loadRequestedPacks(list3);
			if (list2 == null) {
				LOGGER.warn("Double failure in loading server packs");
				list2 = List.of();
			}
		}

		this.packSource = configureSource(list2);
		this.minecraft.reloadResourcePacks();
	}

	public void onRecovery() {
		if (this.pendingReload != null) {
			this.pendingReload.onFailure(false);
			List<Pack> list = this.loadRequestedPacks(this.pendingReload.packsToLoad());
			if (list == null) {
				LOGGER.warn("Double failure in loading server packs");
				list = List.of();
			}

			this.packSource = configureSource(list);
		}
	}

	public void onRecoveryFailure() {
		if (this.pendingReload != null) {
			this.pendingReload.onFailure(true);
			this.pendingReload = null;
			this.packSource = EMPTY_SOURCE;
		}
	}

	public void onReloadSuccess() {
		if (this.pendingReload != null) {
			this.pendingReload.onSuccess();
			this.pendingReload = null;
		}
	}

	@Nullable
	private static HashCode tryParseSha1Hash(@Nullable String string) {
		return string != null && SHA1.matcher(string).matches() ? HashCode.fromString(string.toLowerCase(Locale.ROOT)) : null;
	}

	public void pushPack(UUID uUID, URL uRL, @Nullable String string) {
		HashCode hashCode = tryParseSha1Hash(string);
		this.manager.pushPack(uUID, uRL, hashCode);
	}

	public void pushLocalPack(UUID uUID, Path path) {
		this.manager.pushLocalPack(uUID, path);
	}

	public void popPack(UUID uUID) {
		this.manager.popPack(uUID);
	}

	public void popAll() {
		this.manager.popAll();
	}

	private static PackLoadFeedback createPackResponseSender(Connection connection) {
		return (uUID, result) -> {
			LOGGER.debug("Pack {} changed status to {}", uUID, result);

			ServerboundResourcePackPacket.Action action = switch (result) {
				case ACCEPTED -> ServerboundResourcePackPacket.Action.ACCEPTED;
				case APPLIED -> ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED;
				case DOWNLOAD_FAILED -> ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD;
				case DECLINED -> ServerboundResourcePackPacket.Action.DECLINED;
				case DISCARDED -> ServerboundResourcePackPacket.Action.DISCARDED;
				case ACTIVATION_FAILED -> ServerboundResourcePackPacket.Action.FAILED_RELOAD;
			};
			connection.send(new ServerboundResourcePackPacket(uUID, action));
		};
	}

	public void configureForServerControl(Connection connection, ServerPackManager.PackPromptStatus packPromptStatus) {
		this.packType = PackSource.SERVER;
		this.packFeedback = createPackResponseSender(connection);
		switch (packPromptStatus) {
			case ALLOWED:
				this.manager.allowServerPacks();
				break;
			case DECLINED:
				this.manager.rejectServerPacks();
				break;
			case PENDING:
				this.manager.resetPromptStatus();
		}
	}

	public void configureForLocalWorld() {
		this.packType = PackSource.WORLD;
		this.packFeedback = LOG_ONLY_FEEDBACK;
		this.manager.allowServerPacks();
	}

	public void allowServerPacks() {
		this.manager.allowServerPacks();
	}

	public void rejectServerPacks() {
		this.manager.rejectServerPacks();
	}

	public CompletableFuture<Void> waitForPackFeedback(UUID uUID) {
		CompletableFuture<Void> completableFuture = new CompletableFuture();
		PackLoadFeedback packLoadFeedback = this.packFeedback;
		this.packFeedback = (uUID2, result) -> {
			if (uUID.equals(uUID2)) {
				if (result == PackLoadFeedback.Result.ACCEPTED) {
					packLoadFeedback.sendResponse(uUID2, result);
					return;
				}

				this.packFeedback = packLoadFeedback;
				if (result == PackLoadFeedback.Result.APPLIED) {
					completableFuture.complete(null);
				} else {
					completableFuture.completeExceptionally(new IllegalStateException("Failed to apply pack " + uUID2 + ", reason: " + result));
				}

				packLoadFeedback.sendResponse(uUID2, result);
			}
		};
		return completableFuture;
	}

	public void cleanupAfterDisconnect() {
		this.manager.popAll();
		this.packFeedback = LOG_ONLY_FEEDBACK;
		this.manager.resetPromptStatus();
	}

	public void close() throws IOException {
		this.downloadQueue.close();
	}
}
