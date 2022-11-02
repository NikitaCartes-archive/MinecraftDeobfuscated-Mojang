package net.minecraft.server.packs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class FilePackResources extends AbstractPackResources {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Splitter SPLITTER = Splitter.on('/').omitEmptyStrings().limit(3);
	private final File file;
	@Nullable
	private ZipFile zipFile;
	private boolean failedToLoad;

	public FilePackResources(String string, File file, boolean bl) {
		super(string, bl);
		this.file = file;
	}

	@Nullable
	private ZipFile getOrCreateZipFile() {
		if (this.failedToLoad) {
			return null;
		} else {
			if (this.zipFile == null) {
				try {
					this.zipFile = new ZipFile(this.file);
				} catch (IOException var2) {
					LOGGER.error("Failed to open pack {}", this.file, var2);
					this.failedToLoad = true;
					return null;
				}
			}

			return this.zipFile;
		}
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

	@Nullable
	private IoSupplier<InputStream> getResource(String string) {
		ZipFile zipFile = this.getOrCreateZipFile();
		if (zipFile == null) {
			return null;
		} else {
			ZipEntry zipEntry = zipFile.getEntry(string);
			return zipEntry == null ? null : IoSupplier.create(zipFile, zipEntry);
		}
	}

	@Override
	public Set<String> getNamespaces(PackType packType) {
		ZipFile zipFile = this.getOrCreateZipFile();
		if (zipFile == null) {
			return Set.of();
		} else {
			Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
			Set<String> set = Sets.<String>newHashSet();

			while (enumeration.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
				String string = zipEntry.getName();
				if (string.startsWith(packType.getDirectory() + "/")) {
					List<String> list = Lists.<String>newArrayList(SPLITTER.split(string));
					if (list.size() > 1) {
						String string2 = (String)list.get(1);
						if (string2.equals(string2.toLowerCase(Locale.ROOT))) {
							set.add(string2);
						} else {
							LOGGER.warn("Ignored non-lowercase namespace: {} in {}", string2, this.file);
						}
					}
				}
			}

			return set;
		}
	}

	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

	@Override
	public void close() {
		if (this.zipFile != null) {
			IOUtils.closeQuietly(this.zipFile);
			this.zipFile = null;
		}
	}

	@Override
	public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
		ZipFile zipFile = this.getOrCreateZipFile();
		if (zipFile != null) {
			Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
			String string3 = packType.getDirectory() + "/" + string + "/";
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
}
