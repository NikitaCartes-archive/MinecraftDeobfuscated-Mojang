package net.minecraft.server.packs.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

public class Resource {
	private final String packId;
	private final Resource.IoSupplier<InputStream> streamSupplier;
	private final Resource.IoSupplier<ResourceMetadata> metadataSupplier;
	@Nullable
	private ResourceMetadata cachedMetadata;

	public Resource(String string, Resource.IoSupplier<InputStream> ioSupplier, Resource.IoSupplier<ResourceMetadata> ioSupplier2) {
		this.packId = string;
		this.streamSupplier = ioSupplier;
		this.metadataSupplier = ioSupplier2;
	}

	public Resource(String string, Resource.IoSupplier<InputStream> ioSupplier) {
		this.packId = string;
		this.streamSupplier = ioSupplier;
		this.metadataSupplier = () -> ResourceMetadata.EMPTY;
		this.cachedMetadata = ResourceMetadata.EMPTY;
	}

	public String sourcePackId() {
		return this.packId;
	}

	public InputStream open() throws IOException {
		return this.streamSupplier.get();
	}

	public BufferedReader openAsReader() throws IOException {
		return new BufferedReader(new InputStreamReader(this.open(), StandardCharsets.UTF_8));
	}

	public ResourceMetadata metadata() throws IOException {
		if (this.cachedMetadata == null) {
			this.cachedMetadata = this.metadataSupplier.get();
		}

		return this.cachedMetadata;
	}

	@FunctionalInterface
	public interface IoSupplier<T> {
		T get() throws IOException;
	}
}
