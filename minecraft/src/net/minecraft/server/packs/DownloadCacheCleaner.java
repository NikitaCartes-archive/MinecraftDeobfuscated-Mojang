package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;

public class DownloadCacheCleaner {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void vacuumCacheDir(Path path, int i) {
		try {
			List<DownloadCacheCleaner.PathAndTime> list = listFilesWithModificationTimes(path);
			int j = list.size() - i;
			if (j <= 0) {
				return;
			}

			list.sort(DownloadCacheCleaner.PathAndTime.NEWEST_FIRST);
			List<DownloadCacheCleaner.PathAndPriority> list2 = prioritizeFilesInDirs(list);
			Collections.reverse(list2);
			list2.sort(DownloadCacheCleaner.PathAndPriority.HIGHEST_PRIORITY_FIRST);
			Set<Path> set = new HashSet();

			for (int k = 0; k < j; k++) {
				DownloadCacheCleaner.PathAndPriority pathAndPriority = (DownloadCacheCleaner.PathAndPriority)list2.get(k);
				Path path2 = pathAndPriority.path;

				try {
					Files.delete(path2);
					if (pathAndPriority.removalPriority == 0) {
						set.add(path2.getParent());
					}
				} catch (IOException var12) {
					LOGGER.warn("Failed to delete cache file {}", path2, var12);
				}
			}

			set.remove(path);

			for (Path path3 : set) {
				try {
					Files.delete(path3);
				} catch (DirectoryNotEmptyException var10) {
				} catch (IOException var11) {
					LOGGER.warn("Failed to delete empty(?) cache directory {}", path3, var11);
				}
			}
		} catch (UncheckedIOException | IOException var13) {
			LOGGER.error("Failed to vacuum cache dir {}", path, var13);
		}
	}

	private static List<DownloadCacheCleaner.PathAndTime> listFilesWithModificationTimes(Path path) throws IOException {
		try {
			final List<DownloadCacheCleaner.PathAndTime> list = new ArrayList();
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
					if (basicFileAttributes.isRegularFile() && !path.getParent().equals(path)) {
						FileTime fileTime = basicFileAttributes.lastModifiedTime();
						list.add(new DownloadCacheCleaner.PathAndTime(path, fileTime));
					}

					return FileVisitResult.CONTINUE;
				}
			});
			return list;
		} catch (NoSuchFileException var2) {
			return List.of();
		}
	}

	private static List<DownloadCacheCleaner.PathAndPriority> prioritizeFilesInDirs(List<DownloadCacheCleaner.PathAndTime> list) {
		List<DownloadCacheCleaner.PathAndPriority> list2 = new ArrayList();
		Object2IntOpenHashMap<Path> object2IntOpenHashMap = new Object2IntOpenHashMap<>();

		for (DownloadCacheCleaner.PathAndTime pathAndTime : list) {
			int i = object2IntOpenHashMap.addTo(pathAndTime.path.getParent(), 1);
			list2.add(new DownloadCacheCleaner.PathAndPriority(pathAndTime.path, i));
		}

		return list2;
	}

	static record PathAndPriority(Path path, int removalPriority) {
		public static final Comparator<DownloadCacheCleaner.PathAndPriority> HIGHEST_PRIORITY_FIRST = Comparator.comparing(
				DownloadCacheCleaner.PathAndPriority::removalPriority
			)
			.reversed();
	}

	static record PathAndTime(Path path, FileTime modifiedTime) {
		public static final Comparator<DownloadCacheCleaner.PathAndTime> NEWEST_FIRST = Comparator.comparing(DownloadCacheCleaner.PathAndTime::modifiedTime)
			.reversed();
	}
}
