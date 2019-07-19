package net.minecraft.server.packs.resources;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleResource implements Resource {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Executor IO_EXECUTOR = Executors.newSingleThreadExecutor(
		new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Resource IO {0}").setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER)).build()
	);
	private final String sourceName;
	private final ResourceLocation location;
	private final InputStream resourceStream;
	private final InputStream metadataStream;
	@Environment(EnvType.CLIENT)
	private boolean triedMetadata;
	@Environment(EnvType.CLIENT)
	private JsonObject metadata;

	public SimpleResource(String string, ResourceLocation resourceLocation, InputStream inputStream, @Nullable InputStream inputStream2) {
		this.sourceName = string;
		this.location = resourceLocation;
		this.resourceStream = inputStream;
		this.metadataStream = inputStream2;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ResourceLocation getLocation() {
		return this.location;
	}

	@Override
	public InputStream getInputStream() {
		return this.resourceStream;
	}

	@Environment(EnvType.CLIENT)
	public boolean hasMetadata() {
		return this.metadataStream != null;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	@Override
	public <T> T getMetadata(MetadataSectionSerializer<T> metadataSectionSerializer) {
		if (!this.hasMetadata()) {
			return null;
		} else {
			if (this.metadata == null && !this.triedMetadata) {
				this.triedMetadata = true;
				BufferedReader bufferedReader = null;

				try {
					bufferedReader = new BufferedReader(new InputStreamReader(this.metadataStream, StandardCharsets.UTF_8));
					this.metadata = GsonHelper.parse(bufferedReader);
				} finally {
					IOUtils.closeQuietly(bufferedReader);
				}
			}

			if (this.metadata == null) {
				return null;
			} else {
				String string = metadataSectionSerializer.getMetadataSectionName();
				return this.metadata.has(string) ? metadataSectionSerializer.fromJson(GsonHelper.getAsJsonObject(this.metadata, string)) : null;
			}
		}
	}

	@Override
	public String getSourceName() {
		return this.sourceName;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof SimpleResource)) {
			return false;
		} else {
			SimpleResource simpleResource = (SimpleResource)object;
			if (this.location != null ? this.location.equals(simpleResource.location) : simpleResource.location == null) {
				return this.sourceName != null ? this.sourceName.equals(simpleResource.sourceName) : simpleResource.sourceName == null;
			} else {
				return false;
			}
		}
	}

	public int hashCode() {
		int i = this.sourceName != null ? this.sourceName.hashCode() : 0;
		return 31 * i + (this.location != null ? this.location.hashCode() : 0);
	}

	public void close() throws IOException {
		this.resourceStream.close();
		if (this.metadataStream != null) {
			this.metadataStream.close();
		}
	}
}
