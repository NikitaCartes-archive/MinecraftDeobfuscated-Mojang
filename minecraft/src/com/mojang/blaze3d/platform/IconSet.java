package com.mojang.blaze3d.platform;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.lang3.ArrayUtils;

@Environment(EnvType.CLIENT)
public enum IconSet {
	RELEASE("icons"),
	SNAPSHOT("icons", "snapshot");

	private final String[] path;

	private IconSet(final String... strings) {
		this.path = strings;
	}

	public List<IoSupplier<InputStream>> getStandardIcons(PackResources packResources) throws IOException {
		return List.of(
			this.getFile(packResources, "icon_16x16.png"),
			this.getFile(packResources, "icon_32x32.png"),
			this.getFile(packResources, "icon_48x48.png"),
			this.getFile(packResources, "icon_128x128.png"),
			this.getFile(packResources, "icon_256x256.png")
		);
	}

	public IoSupplier<InputStream> getMacIcon(PackResources packResources) throws IOException {
		return this.getFile(packResources, "minecraft.icns");
	}

	private IoSupplier<InputStream> getFile(PackResources packResources, String string) throws IOException {
		String[] strings = ArrayUtils.add(this.path, string);
		IoSupplier<InputStream> ioSupplier = packResources.getRootResource(strings);
		if (ioSupplier == null) {
			throw new FileNotFoundException(String.join("/", strings));
		} else {
			return ioSupplier;
		}
	}
}
