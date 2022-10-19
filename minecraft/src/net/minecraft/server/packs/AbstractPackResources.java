package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public abstract class AbstractPackResources implements PackResources {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final String name;

	protected AbstractPackResources(String string) {
		this.name = string;
	}

	@Nullable
	@Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
		IoSupplier<InputStream> ioSupplier = this.getRootResource(new String[]{"pack.mcmeta"});
		if (ioSupplier == null) {
			return null;
		} else {
			InputStream inputStream = ioSupplier.get();

			Object var4;
			try {
				var4 = getMetadataFromStream(metadataSectionSerializer, inputStream);
			} catch (Throwable var7) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var6) {
						var7.addSuppressed(var6);
					}
				}

				throw var7;
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return (T)var4;
		}
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
	public String packId() {
		return this.name;
	}
}
