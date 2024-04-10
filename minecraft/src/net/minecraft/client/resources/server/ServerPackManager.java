package net.minecraft.client.resources.server;

import com.google.common.hash.HashCode;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.DownloadQueue;

@Environment(EnvType.CLIENT)
public class ServerPackManager {
	private final PackDownloader downloader;
	final PackLoadFeedback packLoadFeedback;
	private final PackReloadConfig reloadConfig;
	private final Runnable updateRequest;
	private ServerPackManager.PackPromptStatus packPromptStatus;
	final List<ServerPackManager.ServerPackData> packs = new ArrayList();

	public ServerPackManager(
		PackDownloader packDownloader,
		PackLoadFeedback packLoadFeedback,
		PackReloadConfig packReloadConfig,
		Runnable runnable,
		ServerPackManager.PackPromptStatus packPromptStatus
	) {
		this.downloader = packDownloader;
		this.packLoadFeedback = packLoadFeedback;
		this.reloadConfig = packReloadConfig;
		this.updateRequest = runnable;
		this.packPromptStatus = packPromptStatus;
	}

	void registerForUpdate() {
		this.updateRequest.run();
	}

	private void markExistingPacksAsRemoved(UUID uUID) {
		for (ServerPackManager.ServerPackData serverPackData : this.packs) {
			if (serverPackData.id.equals(uUID)) {
				serverPackData.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.SERVER_REPLACED);
			}
		}
	}

	public void pushPack(UUID uUID, URL uRL, @Nullable HashCode hashCode) {
		if (this.packPromptStatus == ServerPackManager.PackPromptStatus.DECLINED) {
			this.packLoadFeedback.reportFinalResult(uUID, PackLoadFeedback.FinalResult.DECLINED);
		} else {
			this.pushNewPack(uUID, new ServerPackManager.ServerPackData(uUID, uRL, hashCode));
		}
	}

	public void pushLocalPack(UUID uUID, Path path) {
		if (this.packPromptStatus == ServerPackManager.PackPromptStatus.DECLINED) {
			this.packLoadFeedback.reportFinalResult(uUID, PackLoadFeedback.FinalResult.DECLINED);
		} else {
			URL uRL;
			try {
				uRL = path.toUri().toURL();
			} catch (MalformedURLException var5) {
				throw new IllegalStateException("Can't convert path to URL " + path, var5);
			}

			ServerPackManager.ServerPackData serverPackData = new ServerPackManager.ServerPackData(uUID, uRL, null);
			serverPackData.downloadStatus = ServerPackManager.PackDownloadStatus.DONE;
			serverPackData.path = path;
			this.pushNewPack(uUID, serverPackData);
		}
	}

	private void pushNewPack(UUID uUID, ServerPackManager.ServerPackData serverPackData) {
		this.markExistingPacksAsRemoved(uUID);
		this.packs.add(serverPackData);
		if (this.packPromptStatus == ServerPackManager.PackPromptStatus.ALLOWED) {
			this.acceptPack(serverPackData);
		}

		this.registerForUpdate();
	}

	private void acceptPack(ServerPackManager.ServerPackData serverPackData) {
		this.packLoadFeedback.reportUpdate(serverPackData.id, PackLoadFeedback.Update.ACCEPTED);
		serverPackData.promptAccepted = true;
	}

	@Nullable
	private ServerPackManager.ServerPackData findPackInfo(UUID uUID) {
		for (ServerPackManager.ServerPackData serverPackData : this.packs) {
			if (!serverPackData.isRemoved() && serverPackData.id.equals(uUID)) {
				return serverPackData;
			}
		}

		return null;
	}

	public void popPack(UUID uUID) {
		ServerPackManager.ServerPackData serverPackData = this.findPackInfo(uUID);
		if (serverPackData != null) {
			serverPackData.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.SERVER_REMOVED);
			this.registerForUpdate();
		}
	}

	public void popAll() {
		for (ServerPackManager.ServerPackData serverPackData : this.packs) {
			serverPackData.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.SERVER_REMOVED);
		}

		this.registerForUpdate();
	}

	public void allowServerPacks() {
		this.packPromptStatus = ServerPackManager.PackPromptStatus.ALLOWED;

		for (ServerPackManager.ServerPackData serverPackData : this.packs) {
			if (!serverPackData.promptAccepted && !serverPackData.isRemoved()) {
				this.acceptPack(serverPackData);
			}
		}

		this.registerForUpdate();
	}

	public void rejectServerPacks() {
		this.packPromptStatus = ServerPackManager.PackPromptStatus.DECLINED;

		for (ServerPackManager.ServerPackData serverPackData : this.packs) {
			if (!serverPackData.promptAccepted) {
				serverPackData.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.DECLINED);
			}
		}

		this.registerForUpdate();
	}

	public void resetPromptStatus() {
		this.packPromptStatus = ServerPackManager.PackPromptStatus.PENDING;
	}

	public void tick() {
		boolean bl = this.updateDownloads();
		if (!bl) {
			this.triggerReloadIfNeeded();
		}

		this.cleanupRemovedPacks();
	}

	private void cleanupRemovedPacks() {
		this.packs.removeIf(serverPackData -> {
			if (serverPackData.activationStatus != ServerPackManager.ActivationStatus.INACTIVE) {
				return false;
			} else if (serverPackData.removalReason != null) {
				PackLoadFeedback.FinalResult finalResult = serverPackData.removalReason.serverResponse;
				if (finalResult != null) {
					this.packLoadFeedback.reportFinalResult(serverPackData.id, finalResult);
				}

				return true;
			} else {
				return false;
			}
		});
	}

	private void onDownload(Collection<ServerPackManager.ServerPackData> collection, DownloadQueue.BatchResult batchResult) {
		if (!batchResult.failed().isEmpty()) {
			for (ServerPackManager.ServerPackData serverPackData : this.packs) {
				if (serverPackData.activationStatus != ServerPackManager.ActivationStatus.ACTIVE) {
					if (batchResult.failed().contains(serverPackData.id)) {
						serverPackData.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.DOWNLOAD_FAILED);
					} else {
						serverPackData.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.DISCARDED);
					}
				}
			}
		}

		for (ServerPackManager.ServerPackData serverPackDatax : collection) {
			Path path = (Path)batchResult.downloaded().get(serverPackDatax.id);
			if (path != null) {
				serverPackDatax.downloadStatus = ServerPackManager.PackDownloadStatus.DONE;
				serverPackDatax.path = path;
				if (!serverPackDatax.isRemoved()) {
					this.packLoadFeedback.reportUpdate(serverPackDatax.id, PackLoadFeedback.Update.DOWNLOADED);
				}
			}
		}

		this.registerForUpdate();
	}

	private boolean updateDownloads() {
		List<ServerPackManager.ServerPackData> list = new ArrayList();
		boolean bl = false;

		for (ServerPackManager.ServerPackData serverPackData : this.packs) {
			if (!serverPackData.isRemoved() && serverPackData.promptAccepted) {
				if (serverPackData.downloadStatus != ServerPackManager.PackDownloadStatus.DONE) {
					bl = true;
				}

				if (serverPackData.downloadStatus == ServerPackManager.PackDownloadStatus.REQUESTED) {
					serverPackData.downloadStatus = ServerPackManager.PackDownloadStatus.PENDING;
					list.add(serverPackData);
				}
			}
		}

		if (!list.isEmpty()) {
			Map<UUID, DownloadQueue.DownloadRequest> map = new HashMap();

			for (ServerPackManager.ServerPackData serverPackData2 : list) {
				map.put(serverPackData2.id, new DownloadQueue.DownloadRequest(serverPackData2.url, serverPackData2.hash));
			}

			this.downloader.download(map, batchResult -> this.onDownload(list, batchResult));
		}

		return bl;
	}

	private void triggerReloadIfNeeded() {
		boolean bl = false;
		final List<ServerPackManager.ServerPackData> list = new ArrayList();
		final List<ServerPackManager.ServerPackData> list2 = new ArrayList();

		for (ServerPackManager.ServerPackData serverPackData : this.packs) {
			if (serverPackData.activationStatus == ServerPackManager.ActivationStatus.PENDING) {
				return;
			}

			boolean bl2 = serverPackData.promptAccepted && serverPackData.downloadStatus == ServerPackManager.PackDownloadStatus.DONE && !serverPackData.isRemoved();
			if (bl2 && serverPackData.activationStatus == ServerPackManager.ActivationStatus.INACTIVE) {
				list.add(serverPackData);
				bl = true;
			}

			if (serverPackData.activationStatus == ServerPackManager.ActivationStatus.ACTIVE) {
				if (!bl2) {
					bl = true;
					list2.add(serverPackData);
				} else {
					list.add(serverPackData);
				}
			}
		}

		if (bl) {
			for (ServerPackManager.ServerPackData serverPackData : list) {
				if (serverPackData.activationStatus != ServerPackManager.ActivationStatus.ACTIVE) {
					serverPackData.activationStatus = ServerPackManager.ActivationStatus.PENDING;
				}
			}

			for (ServerPackManager.ServerPackData serverPackDatax : list2) {
				serverPackDatax.activationStatus = ServerPackManager.ActivationStatus.PENDING;
			}

			this.reloadConfig.scheduleReload(new PackReloadConfig.Callbacks() {
				@Override
				public void onSuccess() {
					for (ServerPackManager.ServerPackData serverPackData : list) {
						serverPackData.activationStatus = ServerPackManager.ActivationStatus.ACTIVE;
						if (serverPackData.removalReason == null) {
							ServerPackManager.this.packLoadFeedback.reportFinalResult(serverPackData.id, PackLoadFeedback.FinalResult.APPLIED);
						}
					}

					for (ServerPackManager.ServerPackData serverPackDatax : list2) {
						serverPackDatax.activationStatus = ServerPackManager.ActivationStatus.INACTIVE;
					}

					ServerPackManager.this.registerForUpdate();
				}

				@Override
				public void onFailure(boolean bl) {
					if (!bl) {
						list.clear();

						for (ServerPackManager.ServerPackData serverPackData : ServerPackManager.this.packs) {
							switch (serverPackData.activationStatus) {
								case INACTIVE:
									serverPackData.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.DISCARDED);
									break;
								case PENDING:
									serverPackData.activationStatus = ServerPackManager.ActivationStatus.INACTIVE;
									serverPackData.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.ACTIVATION_FAILED);
									break;
								case ACTIVE:
									list.add(serverPackData);
							}
						}

						ServerPackManager.this.registerForUpdate();
					} else {
						for (ServerPackManager.ServerPackData serverPackData : ServerPackManager.this.packs) {
							if (serverPackData.activationStatus == ServerPackManager.ActivationStatus.PENDING) {
								serverPackData.activationStatus = ServerPackManager.ActivationStatus.INACTIVE;
							}
						}
					}
				}

				@Override
				public List<PackReloadConfig.IdAndPath> packsToLoad() {
					return list.stream().map(serverPackData -> new PackReloadConfig.IdAndPath(serverPackData.id, serverPackData.path)).toList();
				}
			});
		}
	}

	@Environment(EnvType.CLIENT)
	static enum ActivationStatus {
		INACTIVE,
		PENDING,
		ACTIVE;
	}

	@Environment(EnvType.CLIENT)
	static enum PackDownloadStatus {
		REQUESTED,
		PENDING,
		DONE;
	}

	@Environment(EnvType.CLIENT)
	public static enum PackPromptStatus {
		PENDING,
		ALLOWED,
		DECLINED;
	}

	@Environment(EnvType.CLIENT)
	static enum RemovalReason {
		DOWNLOAD_FAILED(PackLoadFeedback.FinalResult.DOWNLOAD_FAILED),
		ACTIVATION_FAILED(PackLoadFeedback.FinalResult.ACTIVATION_FAILED),
		DECLINED(PackLoadFeedback.FinalResult.DECLINED),
		DISCARDED(PackLoadFeedback.FinalResult.DISCARDED),
		SERVER_REMOVED(null),
		SERVER_REPLACED(null);

		@Nullable
		final PackLoadFeedback.FinalResult serverResponse;

		private RemovalReason(@Nullable final PackLoadFeedback.FinalResult finalResult) {
			this.serverResponse = finalResult;
		}
	}

	@Environment(EnvType.CLIENT)
	static class ServerPackData {
		final UUID id;
		final URL url;
		@Nullable
		final HashCode hash;
		@Nullable
		Path path;
		@Nullable
		ServerPackManager.RemovalReason removalReason;
		ServerPackManager.PackDownloadStatus downloadStatus = ServerPackManager.PackDownloadStatus.REQUESTED;
		ServerPackManager.ActivationStatus activationStatus = ServerPackManager.ActivationStatus.INACTIVE;
		boolean promptAccepted;

		ServerPackData(UUID uUID, URL uRL, @Nullable HashCode hashCode) {
			this.id = uUID;
			this.url = uRL;
			this.hash = hashCode;
		}

		public void setRemovalReasonIfNotSet(ServerPackManager.RemovalReason removalReason) {
			if (this.removalReason == null) {
				this.removalReason = removalReason;
			}
		}

		public boolean isRemoved() {
			return this.removalReason != null;
		}
	}
}
