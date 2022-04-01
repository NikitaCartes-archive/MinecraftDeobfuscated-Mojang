package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

public class MultiPackResourceManager implements CloseableResourceManager {
	private final Map<String, FallbackResourceManager> namespacedManagers;
	private final List<PackResources> packs;

	public MultiPackResourceManager(PackType packType, List<PackResources> list) {
		this.packs = List.copyOf(list);
		Map<String, FallbackResourceManager> map = new HashMap();

		for (PackResources packResources : list) {
			for (String string : packResources.getNamespaces(packType)) {
				((FallbackResourceManager)map.computeIfAbsent(string, stringx -> new FallbackResourceManager(packType, stringx))).add(packResources);
			}
		}

		this.namespacedManagers = map;
	}

	@Override
	public Set<String> getNamespaces() {
		return this.namespacedManagers.keySet();
	}

	@Override
	public Resource getResource(ResourceLocation resourceLocation) throws IOException {
		ResourceManager resourceManager = (ResourceManager)this.namespacedManagers.get(resourceLocation.getNamespace());
		if (resourceManager != null) {
			return resourceManager.getResource(resourceLocation);
		} else {
			throw new FileNotFoundException(resourceLocation.toString());
		}
	}

	@Override
	public boolean hasResource(ResourceLocation resourceLocation) {
		ResourceManager resourceManager = (ResourceManager)this.namespacedManagers.get(resourceLocation.getNamespace());
		return resourceManager != null ? resourceManager.hasResource(resourceLocation) : false;
	}

	@Override
	public List<Resource> getResources(ResourceLocation resourceLocation) throws IOException {
		ResourceManager resourceManager = (ResourceManager)this.namespacedManagers.get(resourceLocation.getNamespace());
		if (resourceManager != null) {
			return resourceManager.getResources(resourceLocation);
		} else {
			throw new FileNotFoundException(resourceLocation.toString());
		}
	}

	@Override
	public Collection<ResourceLocation> listResources(String string, Predicate<String> predicate) {
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();

		for (FallbackResourceManager fallbackResourceManager : this.namespacedManagers.values()) {
			set.addAll(fallbackResourceManager.listResources(string, predicate));
		}

		List<ResourceLocation> list = Lists.<ResourceLocation>newArrayList(set);
		Collections.sort(list);
		return list;
	}

	@Override
	public Stream<PackResources> listPacks() {
		return this.packs.stream();
	}

	@Override
	public void close() {
		this.packs.forEach(PackResources::close);
	}
}
