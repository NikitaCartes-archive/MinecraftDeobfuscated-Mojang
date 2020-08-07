package net.minecraft.server.packs.repository;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;

public class FolderRepositorySource implements RepositorySource {
	private static final FileFilter RESOURCEPACK_FILTER = file -> {
		boolean bl = file.isFile() && file.getName().endsWith(".zip");
		boolean bl2 = file.isDirectory() && new File(file, "pack.mcmeta").isFile();
		return bl || bl2;
	};
	private final File folder;
	private final PackSource packSource;

	public FolderRepositorySource(File file, PackSource packSource) {
		this.folder = file;
		this.packSource = packSource;
	}

	@Override
	public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor packConstructor) {
		if (!this.folder.isDirectory()) {
			this.folder.mkdirs();
		}

		File[] files = this.folder.listFiles(RESOURCEPACK_FILTER);
		if (files != null) {
			for (File file : files) {
				String string = "file/" + file.getName();
				Pack pack = Pack.create(string, false, this.createSupplier(file), packConstructor, Pack.Position.TOP, this.packSource);
				if (pack != null) {
					consumer.accept(pack);
				}
			}
		}
	}

	private Supplier<PackResources> createSupplier(File file) {
		return file.isDirectory() ? () -> new FolderPackResources(file) : () -> new FilePackResources(file);
	}
}
