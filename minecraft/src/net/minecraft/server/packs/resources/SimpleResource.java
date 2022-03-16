package net.minecraft.server.packs.resources;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

public class SimpleResource implements Resource {
	private final String sourceName;
	private final ResourceLocation location;
	private final InputStream resourceStream;
	@Nullable
	private InputStream metadataStream;
	@Nullable
	private JsonObject metadata;

	public SimpleResource(String string, ResourceLocation resourceLocation, InputStream inputStream, @Nullable InputStream inputStream2) {
		this.sourceName = string;
		this.location = resourceLocation;
		this.resourceStream = inputStream;
		this.metadataStream = inputStream2;
	}

	@Override
	public ResourceLocation getLocation() {
		return this.location;
	}

	@Override
	public InputStream getInputStream() {
		return this.resourceStream;
	}

	@Override
	public boolean hasMetadata() {
		return this.metadata != null || this.metadataStream != null;
	}

	@Nullable
	@Override
	public <T> T getMetadata(MetadataSectionSerializer<T> metadataSectionSerializer) {
		if (this.metadata == null && this.metadataStream != null) {
			BufferedReader bufferedReader = null;

			try {
				bufferedReader = new BufferedReader(new InputStreamReader(this.metadataStream, StandardCharsets.UTF_8));
				this.metadata = GsonHelper.parse(bufferedReader);
			} finally {
				IOUtils.closeQuietly(bufferedReader);
			}

			this.metadataStream = null;
		}

		if (this.metadata == null) {
			return null;
		} else {
			String string = metadataSectionSerializer.getMetadataSectionName();
			return this.metadata.has(string) ? metadataSectionSerializer.fromJson(GsonHelper.getAsJsonObject(this.metadata, string)) : null;
		}
	}

	@Override
	public String getSourceName() {
		return this.sourceName;
	}

	public void close() throws IOException {
		this.resourceStream.close();
		if (this.metadataStream != null) {
			this.metadataStream.close();
		}
	}
}
