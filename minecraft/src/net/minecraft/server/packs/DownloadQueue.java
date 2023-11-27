package net.minecraft.server.packs;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.eventlog.JsonEventLog;
import net.minecraft.util.thread.ProcessorMailbox;
import org.slf4j.Logger;

public class DownloadQueue implements AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_KEPT_PACKS = 20;
	private final Path cacheDir;
	private final JsonEventLog<DownloadQueue.LogEntry> eventLog;
	private final ProcessorMailbox<Runnable> tasks = ProcessorMailbox.create(Util.nonCriticalIoPool(), "download-queue");

	public DownloadQueue(Path path) throws IOException {
		this.cacheDir = path;
		FileUtil.createDirectoriesSafe(path);
		this.eventLog = JsonEventLog.open(DownloadQueue.LogEntry.CODEC, path.resolve("log.json"));
		DownloadCacheCleaner.vacuumCacheDir(path, 20);
	}

	private DownloadQueue.BatchResult runDownload(DownloadQueue.BatchConfig batchConfig, Map<UUID, DownloadQueue.DownloadRequest> map) {
		DownloadQueue.BatchResult batchResult = new DownloadQueue.BatchResult();
		map.forEach(
			(uUID, downloadRequest) -> {
				Path path = this.cacheDir.resolve(uUID.toString());
				Path path2 = null;

				try {
					path2 = HttpUtil.downloadFile(
						path,
						downloadRequest.url,
						batchConfig.headers,
						batchConfig.hashFunction,
						downloadRequest.hash,
						batchConfig.maxSize,
						batchConfig.proxy,
						batchConfig.listener
					);
					batchResult.downloaded.put(uUID, path2);
				} catch (Exception var9) {
					LOGGER.error("Failed to download {}", downloadRequest.url, var9);
					batchResult.failed.add(uUID);
				}

				try {
					this.eventLog
						.write(
							new DownloadQueue.LogEntry(
								uUID,
								downloadRequest.url.toString(),
								Instant.now(),
								Optional.ofNullable(downloadRequest.hash).map(HashCode::toString),
								path2 != null ? this.getFileInfo(path2) : Either.left("download_failed")
							)
						);
				} catch (Exception var8) {
					LOGGER.error("Failed to log download of {}", downloadRequest.url, var8);
				}
			}
		);
		return batchResult;
	}

	private Either<String, DownloadQueue.FileInfoEntry> getFileInfo(Path path) {
		try {
			long l = Files.size(path);
			Path path2 = this.cacheDir.relativize(path);
			return Either.right(new DownloadQueue.FileInfoEntry(path2.toString(), l));
		} catch (IOException var5) {
			LOGGER.error("Failed to get file size of {}", path, var5);
			return Either.left("no_access");
		}
	}

	public CompletableFuture<DownloadQueue.BatchResult> downloadBatch(DownloadQueue.BatchConfig batchConfig, Map<UUID, DownloadQueue.DownloadRequest> map) {
		return CompletableFuture.supplyAsync(() -> this.runDownload(batchConfig, map), this.tasks::tell);
	}

	public void close() throws IOException {
		this.tasks.close();
		this.eventLog.close();
	}

	public static record BatchConfig(HashFunction hashFunction, int maxSize, Map<String, String> headers, Proxy proxy, HttpUtil.DownloadProgressListener listener) {
	}

	public static record BatchResult(Map<UUID, Path> downloaded, Set<UUID> failed) {

		public BatchResult() {
			this(new HashMap(), new HashSet());
		}
	}

	public static record DownloadRequest(URL url, @Nullable HashCode hash) {
	}

	static record FileInfoEntry(String name, long size) {
		public static final Codec<DownloadQueue.FileInfoEntry> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.STRING.fieldOf("name").forGetter(DownloadQueue.FileInfoEntry::name), Codec.LONG.fieldOf("size").forGetter(DownloadQueue.FileInfoEntry::size)
					)
					.apply(instance, DownloadQueue.FileInfoEntry::new)
		);
	}

	static record LogEntry(UUID id, String url, Instant time, Optional<String> hash, Either<String, DownloadQueue.FileInfoEntry> errorOrFileInfo) {
		public static final Codec<DownloadQueue.LogEntry> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(DownloadQueue.LogEntry::id),
						Codec.STRING.fieldOf("url").forGetter(DownloadQueue.LogEntry::url),
						ExtraCodecs.INSTANT_ISO8601.fieldOf("time").forGetter(DownloadQueue.LogEntry::time),
						Codec.STRING.optionalFieldOf("hash").forGetter(DownloadQueue.LogEntry::hash),
						Codec.mapEither(Codec.STRING.fieldOf("error"), DownloadQueue.FileInfoEntry.CODEC.fieldOf("file")).forGetter(DownloadQueue.LogEntry::errorOrFileInfo)
					)
					.apply(instance, DownloadQueue.LogEntry::new)
		);
	}
}
