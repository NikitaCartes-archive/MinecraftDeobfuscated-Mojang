package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

public interface ResourceManager extends ResourceProvider {
	Set<String> getNamespaces();

	List<Resource> getResourceStack(ResourceLocation resourceLocation);

	Map<ResourceLocation, Resource> listResources(String string, Predicate<ResourceLocation> predicate);

	Map<ResourceLocation, List<Resource>> listResourceStacks(String string, Predicate<ResourceLocation> predicate);

	Stream<PackResources> listPacks();

	public static enum Empty implements ResourceManager {
		INSTANCE;

		@Override
		public Set<String> getNamespaces() {
			return Set.of();
		}

		@Override
		public Optional<Resource> getResource(ResourceLocation resourceLocation) {
			return Optional.empty();
		}

		@Override
		public List<Resource> getResourceStack(ResourceLocation resourceLocation) {
			return List.of();
		}

		@Override
		public Map<ResourceLocation, Resource> listResources(String string, Predicate<ResourceLocation> predicate) {
			return Map.of();
		}

		@Override
		public Map<ResourceLocation, List<Resource>> listResourceStacks(String string, Predicate<ResourceLocation> predicate) {
			return Map.of();
		}

		@Override
		public Stream<PackResources> listPacks() {
			return Stream.of();
		}
	}
}
