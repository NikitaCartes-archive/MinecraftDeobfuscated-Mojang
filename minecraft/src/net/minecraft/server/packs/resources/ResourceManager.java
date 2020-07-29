package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

public interface ResourceManager {
	@Environment(EnvType.CLIENT)
	Set<String> getNamespaces();

	Resource getResource(ResourceLocation resourceLocation) throws IOException;

	@Environment(EnvType.CLIENT)
	boolean hasResource(ResourceLocation resourceLocation);

	List<Resource> getResources(ResourceLocation resourceLocation) throws IOException;

	Collection<ResourceLocation> listResources(String string, Predicate<String> predicate);

	@Environment(EnvType.CLIENT)
	Stream<PackResources> listPacks();

	public static enum Empty implements ResourceManager {
		INSTANCE;

		@Environment(EnvType.CLIENT)
		@Override
		public Set<String> getNamespaces() {
			return ImmutableSet.of();
		}

		@Override
		public Resource getResource(ResourceLocation resourceLocation) throws IOException {
			throw new FileNotFoundException(resourceLocation.toString());
		}

		@Environment(EnvType.CLIENT)
		@Override
		public boolean hasResource(ResourceLocation resourceLocation) {
			return false;
		}

		@Override
		public List<Resource> getResources(ResourceLocation resourceLocation) {
			return ImmutableList.of();
		}

		@Override
		public Collection<ResourceLocation> listResources(String string, Predicate<String> predicate) {
			return ImmutableSet.<ResourceLocation>of();
		}

		@Environment(EnvType.CLIENT)
		@Override
		public Stream<PackResources> listPacks() {
			return Stream.of();
		}
	}
}
