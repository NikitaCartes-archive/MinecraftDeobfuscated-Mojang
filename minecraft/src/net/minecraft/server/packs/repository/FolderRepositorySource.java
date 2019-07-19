package net.minecraft.server.packs.repository;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.server.packs.FileResourcePack;
import net.minecraft.server.packs.FolderResourcePack;
import net.minecraft.server.packs.Pack;

public class FolderRepositorySource implements RepositorySource {
	private static final FileFilter RESOURCEPACK_FILTER = file -> {
		boolean bl = file.isFile() && file.getName().endsWith(".zip");
		boolean bl2 = file.isDirectory() && new File(file, "pack.mcmeta").isFile();
		return bl || bl2;
	};
	private final File folder;

	public FolderRepositorySource(File file) {
		this.folder = file;
	}

	@Override
	public <T extends UnopenedPack> void loadPacks(Map<String, T> map, UnopenedPack.UnopenedPackConstructor<T> unopenedPackConstructor) {
		if (!this.folder.isDirectory()) {
			this.folder.mkdirs();
		}

		File[] files = this.folder.listFiles(RESOURCEPACK_FILTER);
		if (files != null) {
			for (File file : files) {
				String string = "file/" + file.getName();
				T unopenedPack = UnopenedPack.create(string, false, this.createSupplier(file), unopenedPackConstructor, UnopenedPack.Position.TOP);
				if (unopenedPack != null) {
					map.put(string, unopenedPack);
				}
			}
		}
	}

	private Supplier<Pack> createSupplier(File file) {
		return file.isDirectory() ? () -> new FolderResourcePack(file) : () -> new FileResourcePack(file);
	}
}
