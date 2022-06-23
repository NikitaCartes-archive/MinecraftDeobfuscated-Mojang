package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public abstract class AbstractPackResources implements PackResources {
	private static final Logger LOGGER = LogUtils.getLogger();
	protected final File file;

	public AbstractPackResources(File file) {
		this.file = file;
	}

	private static String getPathFromLocation(PackType packType, ResourceLocation resourceLocation) {
		return String.format("%s/%s/%s", packType.getDirectory(), resourceLocation.getNamespace(), resourceLocation.getPath());
	}

	protected static String getRelativePath(File file, File file2) {
		return file.toURI().relativize(file2.toURI()).getPath();
	}

	@Override
	public InputStream getResource(PackType packType, ResourceLocation resourceLocation) throws IOException {
		return this.getResource(getPathFromLocation(packType, resourceLocation));
	}

	@Override
	public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
		return this.hasResource(getPathFromLocation(packType, resourceLocation));
	}

	protected abstract InputStream getResource(String string) throws IOException;

	@Override
	public InputStream getRootResource(String string) throws IOException {
		if (!string.contains("/") && !string.contains("\\")) {
			return this.getResource(string);
		} else {
			throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
		}
	}

	protected abstract boolean hasResource(String string);

	protected void logWarning(String string) {
		LOGGER.warn("ResourcePack: ignored non-lowercase namespace: {} in {}", string, this.file);
	}

	@Nullable
	@Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
		InputStream inputStream = this.getResource("pack.mcmeta");

		Object var3;
		try {
			var3 = getMetadataFromStream(metadataSectionSerializer, inputStream);
		} catch (Throwable var6) {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Throwable var5) {
					var6.addSuppressed(var5);
				}
			}

			throw var6;
		}

		if (inputStream != null) {
			inputStream.close();
		}

		return (T)var3;
	}

	@Nullable
	public static <T> T getMetadataFromStream(MetadataSectionSerializer<T> metadataSectionSerializer, InputStream inputStream) {
		JsonObject jsonObject;
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

			try {
				jsonObject = GsonHelper.parse(bufferedReader);
			} catch (Throwable var8) {
				try {
					bufferedReader.close();
				} catch (Throwable var6) {
					var8.addSuppressed(var6);
				}

				throw var8;
			}

			bufferedReader.close();
		} catch (Exception var9) {
			LOGGER.error("Couldn't load {} metadata", metadataSectionSerializer.getMetadataSectionName(), var9);
			return null;
		}

		if (!jsonObject.has(metadataSectionSerializer.getMetadataSectionName())) {
			return null;
		} else {
			try {
				return metadataSectionSerializer.fromJson(GsonHelper.getAsJsonObject(jsonObject, metadataSectionSerializer.getMetadataSectionName()));
			} catch (Exception var7) {
				LOGGER.error("Couldn't load {} metadata", metadataSectionSerializer.getMetadataSectionName(), var7);
				return null;
			}
		}
	}

	@Override
	public String getName() {
		return this.file.getName();
	}
}
