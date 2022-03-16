package net.minecraft.server.packs.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

public interface ResourceManager extends ResourceProvider {
	Set<String> getNamespaces();

	boolean hasResource(ResourceLocation resourceLocation);

	List<ResourceThunk> getResourceStack(ResourceLocation resourceLocation) throws IOException;

	Map<ResourceLocation, ResourceThunk> listResources(String string, Predicate<ResourceLocation> predicate);

	Map<ResourceLocation, List<ResourceThunk>> listResourceStacks(String string, Predicate<ResourceLocation> predicate);

	Stream<PackResources> listPacks();

	public static enum Empty implements ResourceManager {
		INSTANCE;

		@Override
		public Set<String> getNamespaces() {
			return Set.of();
		}

		@Override
		public Resource getResource(ResourceLocation resourceLocation) throws IOException {
			throw new FileNotFoundException(resourceLocation.toString());
		}

		@Override
		public boolean hasResource(ResourceLocation resourceLocation) {
			return false;
		}

		@Override
		public List<ResourceThunk> getResourceStack(ResourceLocation resourceLocation) throws IOException {
			throw new FileNotFoundException(resourceLocation.toString());
		}

		@Override
		public Map<ResourceLocation, ResourceThunk> listResources(String string, Predicate<ResourceLocation> predicate) {
			return Map.of();
		}

		@Override
		public Map<ResourceLocation, List<ResourceThunk>> listResourceStacks(String string, Predicate<ResourceLocation> predicate) {
			return Map.of();
		}

		@Override
		public Stream<PackResources> listPacks() {
			return Stream.of();
		}
	}
}
