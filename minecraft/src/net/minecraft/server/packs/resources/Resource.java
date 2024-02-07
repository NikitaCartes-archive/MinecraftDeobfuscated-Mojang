package net.minecraft.server.packs.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.KnownPack;

public class Resource {
	private final PackResources source;
	private final IoSupplier<InputStream> streamSupplier;
	private final IoSupplier<ResourceMetadata> metadataSupplier;
	@Nullable
	private ResourceMetadata cachedMetadata;

	public Resource(PackResources packResources, IoSupplier<InputStream> ioSupplier, IoSupplier<ResourceMetadata> ioSupplier2) {
		this.source = packResources;
		this.streamSupplier = ioSupplier;
		this.metadataSupplier = ioSupplier2;
	}

	public Resource(PackResources packResources, IoSupplier<InputStream> ioSupplier) {
		this.source = packResources;
		this.streamSupplier = ioSupplier;
		this.metadataSupplier = ResourceMetadata.EMPTY_SUPPLIER;
		this.cachedMetadata = ResourceMetadata.EMPTY;
	}

	public PackResources source() {
		return this.source;
	}

	public String sourcePackId() {
		return this.source.packId();
	}

	public Optional<KnownPack> knownPackInfo() {
		return this.source.knownPackInfo();
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
}
