package net.minecraft.server.packs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

public class ResourcePackFileNotFoundException extends FileNotFoundException {
	public ResourcePackFileNotFoundException(File file, String string) {
		super(String.format(Locale.ROOT, "'%s' in ResourcePack '%s'", string, file));
	}
}
