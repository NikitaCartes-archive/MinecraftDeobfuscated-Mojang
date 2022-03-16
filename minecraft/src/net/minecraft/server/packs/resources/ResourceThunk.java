package net.minecraft.server.packs.resources;

import java.io.IOException;

public class ResourceThunk {
	private final String packId;
	private final ResourceThunk.ResourceSupplier resourceSupplier;

	public ResourceThunk(String string, ResourceThunk.ResourceSupplier resourceSupplier) {
		this.packId = string;
		this.resourceSupplier = resourceSupplier;
	}

	public String sourcePackId() {
		return this.packId;
	}

	public Resource open() throws IOException {
		return this.resourceSupplier.open();
	}

	@FunctionalInterface
	public interface ResourceSupplier {
		Resource open() throws IOException;
	}
}
