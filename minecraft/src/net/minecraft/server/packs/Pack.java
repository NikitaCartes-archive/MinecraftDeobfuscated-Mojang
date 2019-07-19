package net.minecraft.server.packs;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

public interface Pack extends Closeable {
	@Environment(EnvType.CLIENT)
	InputStream getRootResource(String string) throws IOException;

	InputStream getResource(PackType packType, ResourceLocation resourceLocation) throws IOException;

	Collection<ResourceLocation> getResources(PackType packType, String string, int i, Predicate<String> predicate);

	boolean hasResource(PackType packType, ResourceLocation resourceLocation);

	Set<String> getNamespaces(PackType packType);

	@Nullable
	<T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException;

	String getName();
}
