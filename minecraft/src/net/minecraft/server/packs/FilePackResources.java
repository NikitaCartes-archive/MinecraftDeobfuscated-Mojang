package net.minecraft.server.packs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class FilePackResources extends AbstractPackResources {
	static final Logger LOGGER = LogUtils.getLogger();
	private final FilePackResources.SharedZipFileAccess zipFileAccess;
	private final String prefix;

	FilePackResources(PackLocationInfo packLocationInfo, FilePackResources.SharedZipFileAccess sharedZipFileAccess, String string) {
		super(packLocationInfo);
		this.zipFileAccess = sharedZipFileAccess;
		this.prefix = string;
	}

	private static String getPathFromLocation(PackType packType, ResourceLocation resourceLocation) {
		return String.format(Locale.ROOT, "%s/%s/%s", packType.getDirectory(), resourceLocation.getNamespace(), resourceLocation.getPath());
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getRootResource(String... strings) {
		return this.getResource(String.join("/", strings));
	}

	@Override
	public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
		return this.getResource(getPathFromLocation(packType, resourceLocation));
	}

	private String addPrefix(String string) {
		return this.prefix.isEmpty() ? string : this.prefix + "/" + string;
	}

	@Nullable
	private IoSupplier<InputStream> getResource(String string) {
		ZipFile zipFile = this.zipFileAccess.getOrCreateZipFile();
		if (zipFile == null) {
			return null;
		} else {
			ZipEntry zipEntry = zipFile.getEntry(this.addPrefix(string));
			return zipEntry == null ? null : IoSupplier.create(zipFile, zipEntry);
		}
	}

	@Override
	public Set<String> getNamespaces(PackType packType) {
		ZipFile zipFile = this.zipFileAccess.getOrCreateZipFile();
		if (zipFile == null) {
			return Set.of();
		} else {
			Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
			Set<String> set = Sets.<String>newHashSet();
			String string = this.addPrefix(packType.getDirectory() + "/");

			while (enumeration.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
				String string2 = zipEntry.getName();
				String string3 = extractNamespace(string, string2);
				if (!string3.isEmpty()) {
					if (ResourceLocation.isValidNamespace(string3)) {
						set.add(string3);
					} else {
						LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", string3, this.zipFileAccess.file);
					}
				}
			}

			return set;
		}
	}

	@VisibleForTesting
	public static String extractNamespace(String string, String string2) {
		if (!string2.startsWith(string)) {
			return "";
		} else {
			int i = string.length();
			int j = string2.indexOf(47, i);
			return j == -1 ? string2.substring(i) : string2.substring(i, j);
		}
	}

	@Override
	public void close() {
		this.zipFileAccess.close();
	}

	@Override
	public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
		ZipFile zipFile = this.zipFileAccess.getOrCreateZipFile();
		if (zipFile != null) {
			Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
			String string3 = this.addPrefix(packType.getDirectory() + "/" + string + "/");
			String string4 = string3 + string2 + "/";

			while (enumeration.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
				if (!zipEntry.isDirectory()) {
					String string5 = zipEntry.getName();
					if (string5.startsWith(string4)) {
						String string6 = string5.substring(string3.length());
						ResourceLocation resourceLocation = ResourceLocation.tryBuild(string, string6);
						if (resourceLocation != null) {
							resourceOutput.accept(resourceLocation, IoSupplier.create(zipFile, zipEntry));
						} else {
							LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", string, string6);
						}
					}
				}
			}
		}
	}

	public static class FileResourcesSupplier implements Pack.ResourcesSupplier {
		private final File content;

		public FileResourcesSupplier(Path path) {
			this(path.toFile());
		}

		public FileResourcesSupplier(File file) {
			this.content = file;
		}

		@Override
		public PackResources openPrimary(PackLocationInfo packLocationInfo) {
			FilePackResources.SharedZipFileAccess sharedZipFileAccess = new FilePackResources.SharedZipFileAccess(this.content);
			return new FilePackResources(packLocationInfo, sharedZipFileAccess, "");
		}

		@Override
		public PackResources openFull(PackLocationInfo packLocationInfo, Pack.Metadata metadata) {
			FilePackResources.SharedZipFileAccess sharedZipFileAccess = new FilePackResources.SharedZipFileAccess(this.content);
			PackResources packResources = new FilePackResources(packLocationInfo, sharedZipFileAccess, "");
			List<String> list = metadata.overlays();
			if (list.isEmpty()) {
				return packResources;
			} else {
				List<PackResources> list2 = new ArrayList(list.size());

				for (String string : list) {
					list2.add(new FilePackResources(packLocationInfo, sharedZipFileAccess, string));
				}

				return new CompositePackResources(packResources, list2);
			}
		}
	}

	static class SharedZipFileAccess implements AutoCloseable {
		final File file;
		@Nullable
		private ZipFile zipFile;
		private boolean failedToLoad;

		SharedZipFileAccess(File file) {
			this.file = file;
		}

		@Nullable
		ZipFile getOrCreateZipFile() {
			if (this.failedToLoad) {
				return null;
			} else {
				if (this.zipFile == null) {
					try {
						this.zipFile = new ZipFile(this.file);
					} catch (IOException var2) {
						FilePackResources.LOGGER.error("Failed to open pack {}", this.file, var2);
						this.failedToLoad = true;
						return null;
					}
				}

				return this.zipFile;
			}
		}

		public void close() {
			if (this.zipFile != null) {
				IOUtils.closeQuietly(this.zipFile);
				this.zipFile = null;
			}
		}

		protected void finalize() throws Throwable {
			this.close();
			super.finalize();
		}
	}
}
