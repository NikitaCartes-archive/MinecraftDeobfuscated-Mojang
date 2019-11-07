package net.minecraft.server.packs;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FolderResourcePack extends AbstractResourcePack {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final boolean ON_WINDOWS = Util.getPlatform() == Util.OS.WINDOWS;
	private static final CharMatcher BACKSLASH_MATCHER = CharMatcher.is('\\');

	public FolderResourcePack(File file) {
		super(file);
	}

	public static boolean validatePath(File file, String string) throws IOException {
		String string2 = file.getCanonicalPath();
		if (ON_WINDOWS) {
			string2 = BACKSLASH_MATCHER.replaceFrom(string2, '/');
		}

		return string2.endsWith(string);
	}

	@Override
	protected InputStream getResource(String string) throws IOException {
		File file = this.getFile(string);
		if (file == null) {
			throw new ResourcePackFileNotFoundException(this.file, string);
		} else {
			return new FileInputStream(file);
		}
	}

	@Override
	protected boolean hasResource(String string) {
		return this.getFile(string) != null;
	}

	@Nullable
	private File getFile(String string) {
		try {
			File file = new File(this.file, string);
			if (file.isFile() && validatePath(file, string)) {
				return file;
			}
		} catch (IOException var3) {
		}

		return null;
	}

	@Override
	public Set<String> getNamespaces(PackType packType) {
		Set<String> set = Sets.<String>newHashSet();
		File file = new File(this.file, packType.getDirectory());
		File[] files = file.listFiles(DirectoryFileFilter.DIRECTORY);
		if (files != null) {
			for (File file2 : files) {
				String string = getRelativePath(file, file2);
				if (string.equals(string.toLowerCase(Locale.ROOT))) {
					set.add(string.substring(0, string.length() - 1));
				} else {
					this.logWarning(string);
				}
			}
		}

		return set;
	}

	public void close() throws IOException {
	}

	@Override
	public Collection<ResourceLocation> getResources(PackType packType, String string, String string2, int i, Predicate<String> predicate) {
		File file = new File(this.file, packType.getDirectory());
		List<ResourceLocation> list = Lists.<ResourceLocation>newArrayList();
		this.listResources(new File(new File(file, string), string2), i, string, list, string2 + "/", predicate);
		return list;
	}

	private void listResources(File file, int i, String string, List<ResourceLocation> list, String string2, Predicate<String> predicate) {
		File[] files = file.listFiles();
		if (files != null) {
			for (File file2 : files) {
				if (file2.isDirectory()) {
					if (i > 0) {
						this.listResources(file2, i - 1, string, list, string2 + file2.getName() + "/", predicate);
					}
				} else if (!file2.getName().endsWith(".mcmeta") && predicate.test(file2.getName())) {
					try {
						list.add(new ResourceLocation(string, string2 + file2.getName()));
					} catch (ResourceLocationException var13) {
						LOGGER.error(var13.getMessage());
					}
				}
			}
		}
	}
}
