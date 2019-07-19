package net.minecraft.server.packs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;

public class FileResourcePack extends AbstractResourcePack {
	public static final Splitter SPLITTER = Splitter.on('/').omitEmptyStrings().limit(3);
	private ZipFile zipFile;

	public FileResourcePack(File file) {
		super(file);
	}

	private ZipFile getOrCreateZipFile() throws IOException {
		if (this.zipFile == null) {
			this.zipFile = new ZipFile(this.file);
		}

		return this.zipFile;
	}

	@Override
	protected InputStream getResource(String string) throws IOException {
		ZipFile zipFile = this.getOrCreateZipFile();
		ZipEntry zipEntry = zipFile.getEntry(string);
		if (zipEntry == null) {
			throw new ResourcePackFileNotFoundException(this.file, string);
		} else {
			return zipFile.getInputStream(zipEntry);
		}
	}

	@Override
	public boolean hasResource(String string) {
		try {
			return this.getOrCreateZipFile().getEntry(string) != null;
		} catch (IOException var3) {
			return false;
		}
	}

	@Override
	public Set<String> getNamespaces(PackType packType) {
		ZipFile zipFile;
		try {
			zipFile = this.getOrCreateZipFile();
		} catch (IOException var9) {
			return Collections.emptySet();
		}

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
						this.logWarning(string2);
					}
				}
			}
		}

		return set;
	}

	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

	public void close() {
		if (this.zipFile != null) {
			IOUtils.closeQuietly(this.zipFile);
			this.zipFile = null;
		}
	}

	@Override
	public Collection<ResourceLocation> getResources(PackType packType, String string, int i, Predicate<String> predicate) {
		ZipFile zipFile;
		try {
			zipFile = this.getOrCreateZipFile();
		} catch (IOException var15) {
			return Collections.emptySet();
		}

		Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
		List<ResourceLocation> list = Lists.<ResourceLocation>newArrayList();
		String string2 = packType.getDirectory() + "/";

		while (enumeration.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
			if (!zipEntry.isDirectory() && zipEntry.getName().startsWith(string2)) {
				String string3 = zipEntry.getName().substring(string2.length());
				if (!string3.endsWith(".mcmeta")) {
					int j = string3.indexOf(47);
					if (j >= 0) {
						String string4 = string3.substring(j + 1);
						if (string4.startsWith(string + "/")) {
							String[] strings = string4.substring(string.length() + 2).split("/");
							if (strings.length >= i + 1 && predicate.test(string4)) {
								String string5 = string3.substring(0, j);
								list.add(new ResourceLocation(string5, string4));
							}
						}
					}
				}
			}
		}

		return list;
	}
}
