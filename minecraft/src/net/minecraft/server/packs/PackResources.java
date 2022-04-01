package net.minecraft.server.packs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

public interface PackResources extends AutoCloseable {
	String METADATA_EXTENSION = ".mcmeta";
	String PACK_META = "pack.mcmeta";

	@Nullable
	InputStream getRootResource(String string) throws IOException;

	InputStream getResource(PackType packType, ResourceLocation resourceLocation) throws IOException;

	Collection<ResourceLocation> getResources(PackType packType, String string, String string2, int i, Predicate<String> predicate);

	boolean hasResource(PackType packType, ResourceLocation resourceLocation);

	Set<String> getNamespaces(PackType packType);

	@Nullable
	<T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException;

	String getName();

	void close();
}
