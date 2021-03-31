package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractPackResources implements PackResources {
	private static final Logger LOGGER = LogManager.getLogger();
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
		Throwable var3 = null;

		Object var4;
		try {
			var4 = getMetadataFromStream(metadataSectionSerializer, inputStream);
		} catch (Throwable var13) {
			var3 = var13;
			throw var13;
		} finally {
			if (inputStream != null) {
				if (var3 != null) {
					try {
						inputStream.close();
					} catch (Throwable var12) {
						var3.addSuppressed(var12);
					}
				} else {
					inputStream.close();
				}
			}
		}

		return (T)var4;
	}

	@Nullable
	public static <T> T getMetadataFromStream(MetadataSectionSerializer<T> metadataSectionSerializer, InputStream inputStream) {
		JsonObject jsonObject;
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			Throwable var4 = null;

			try {
				jsonObject = GsonHelper.parse(bufferedReader);
			} catch (Throwable var16) {
				var4 = var16;
				throw var16;
			} finally {
				if (bufferedReader != null) {
					if (var4 != null) {
						try {
							bufferedReader.close();
						} catch (Throwable var14) {
							var4.addSuppressed(var14);
						}
					} else {
						bufferedReader.close();
					}
				}
			}
		} catch (JsonParseException | IOException var18) {
			LOGGER.error("Couldn't load {} metadata", metadataSectionSerializer.getMetadataSectionName(), var18);
			return null;
		}

		if (!jsonObject.has(metadataSectionSerializer.getMetadataSectionName())) {
			return null;
		} else {
			try {
				return metadataSectionSerializer.fromJson(GsonHelper.getAsJsonObject(jsonObject, metadataSectionSerializer.getMetadataSectionName()));
			} catch (JsonParseException var15) {
				LOGGER.error("Couldn't load {} metadata", metadataSectionSerializer.getMetadataSectionName(), var15);
				return null;
			}
		}
	}

	@Override
	public String getName() {
		return this.file.getName();
	}
}
