package net.minecraft.server.packs.resources;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.Pack;

public interface ResourceManager {
	@Environment(EnvType.CLIENT)
	Set<String> getNamespaces();

	Resource getResource(ResourceLocation resourceLocation) throws IOException;

	@Environment(EnvType.CLIENT)
	boolean hasResource(ResourceLocation resourceLocation);

	List<Resource> getResources(ResourceLocation resourceLocation) throws IOException;

	Collection<ResourceLocation> listResources(String string, Predicate<String> predicate);

	@Environment(EnvType.CLIENT)
	Stream<Pack> listPacks();
}
