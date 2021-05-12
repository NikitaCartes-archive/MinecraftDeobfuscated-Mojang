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
	private final InputStream metadataStream;
	private boolean triedMetadata;
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
		return this.metadataStream != null;
	}

	@Nullable
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
		} else if (!(object instanceof SimpleResource simpleResource)) {
			return false;
		} else if (this.location != null ? this.location.equals(simpleResource.location) : simpleResource.location == null) {
			return this.sourceName != null ? this.sourceName.equals(simpleResource.sourceName) : simpleResource.sourceName == null;
		} else {
			return false;
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
