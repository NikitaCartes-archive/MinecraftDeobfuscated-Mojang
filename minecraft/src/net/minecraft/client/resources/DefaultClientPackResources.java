package net.minecraft.client.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

@Environment(EnvType.CLIENT)
public class DefaultClientPackResources extends VanillaPackResources {
	private final AssetIndex assetIndex;

	public DefaultClientPackResources(PackMetadataSection packMetadataSection, AssetIndex assetIndex) {
		super(packMetadataSection, "minecraft", "realms", "nothingtoseeheremovealong");
		this.assetIndex = assetIndex;
	}

	@Nullable
	@Override
	protected InputStream getResourceAsStream(PackType packType, ResourceLocation resourceLocation) {
		if (packType == PackType.CLIENT_RESOURCES) {
			File file = this.assetIndex.getFile(resourceLocation);
			if (file != null && file.exists()) {
				try {
					return new FileInputStream(file);
				} catch (FileNotFoundException var5) {
				}
			}
		}

		return super.getResourceAsStream(packType, resourceLocation);
	}

	@Override
	public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
		if (packType == PackType.CLIENT_RESOURCES) {
			File file = this.assetIndex.getFile(resourceLocation);
			if (file != null && file.exists()) {
				return true;
			}
		}

		return super.hasResource(packType, resourceLocation);
	}

	@Nullable
	@Override
	protected InputStream getResourceAsStream(String string) {
		File file = this.assetIndex.getRootFile(string);
		if (file != null && file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException var4) {
			}
		}

		return super.getResourceAsStream(string);
	}

	@Override
	public Collection<ResourceLocation> getResources(PackType packType, String string, String string2, int i, Predicate<String> predicate) {
		Collection<ResourceLocation> collection = super.getResources(packType, string, string2, i, predicate);
		collection.addAll(this.assetIndex.getFiles(string2, string, i, predicate));
		return collection;
	}
}
