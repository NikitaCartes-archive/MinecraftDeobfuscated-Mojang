package net.minecraft.server.packs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;

public interface PackResources extends AutoCloseable {
	String METADATA_EXTENSION = ".mcmeta";
	String PACK_META = "pack.mcmeta";

	@Nullable
	IoSupplier<InputStream> getRootResource(String... strings);

	@Nullable
	IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation);

	void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput);

	Set<String> getNamespaces(PackType packType);

	@Nullable
	<T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException;

	String packId();

	default boolean isBuiltin() {
		return false;
	}

	void close();

	@FunctionalInterface
	public interface ResourceOutput extends BiConsumer<ResourceLocation, IoSupplier<InputStream>> {
	}
}
