package net.minecraft.server.packs.resources;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class MultiPackResourceManager implements CloseableResourceManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<String, FallbackResourceManager> namespacedManagers;
	private final List<PackResources> packs;

	public MultiPackResourceManager(PackType packType, List<PackResources> list) {
		this.packs = List.copyOf(list);
		Map<String, FallbackResourceManager> map = new HashMap();
		List<String> list2 = list.stream().flatMap(packResourcesx -> packResourcesx.getNamespaces(packType).stream()).toList();

		for (PackResources packResources : list) {
			ResourceFilterSection resourceFilterSection = this.getPackFilterSection(packResources);
			Set<String> set = packResources.getNamespaces(packType);
			Predicate<ResourceLocation> predicate = resourceFilterSection != null
				? resourceLocation -> resourceFilterSection.isPathFiltered(resourceLocation.getPath())
				: null;

			for (String string : list2) {
				boolean bl = set.contains(string);
				boolean bl2 = resourceFilterSection != null && resourceFilterSection.isNamespaceFiltered(string);
				if (bl || bl2) {
					FallbackResourceManager fallbackResourceManager = (FallbackResourceManager)map.get(string);
					if (fallbackResourceManager == null) {
						fallbackResourceManager = new FallbackResourceManager(packType, string);
						map.put(string, fallbackResourceManager);
					}

					if (bl && bl2) {
						fallbackResourceManager.push(packResources, predicate);
					} else if (bl) {
						fallbackResourceManager.push(packResources);
					} else {
						fallbackResourceManager.pushFilterOnly(packResources.getName(), predicate);
					}
				}
			}
		}

		this.namespacedManagers = map;
	}

	@Nullable
	private ResourceFilterSection getPackFilterSection(PackResources packResources) {
		try {
			return packResources.getMetadataSection(ResourceFilterSection.SERIALIZER);
		} catch (IOException var3) {
			LOGGER.error("Failed to get filter section from pack {}", packResources.getName());
			return null;
		}
	}

	@Override
	public Set<String> getNamespaces() {
		return this.namespacedManagers.keySet();
	}

	@Override
	public Optional<Resource> getResource(ResourceLocation resourceLocation) {
		ResourceManager resourceManager = (ResourceManager)this.namespacedManagers.get(resourceLocation.getNamespace());
		return resourceManager != null ? resourceManager.getResource(resourceLocation) : Optional.empty();
	}

	@Override
	public List<Resource> getResourceStack(ResourceLocation resourceLocation) {
		ResourceManager resourceManager = (ResourceManager)this.namespacedManagers.get(resourceLocation.getNamespace());
		return resourceManager != null ? resourceManager.getResourceStack(resourceLocation) : List.of();
	}

	@Override
	public Map<ResourceLocation, Resource> listResources(String string, Predicate<ResourceLocation> predicate) {
		Map<ResourceLocation, Resource> map = new TreeMap();

		for (FallbackResourceManager fallbackResourceManager : this.namespacedManagers.values()) {
			map.putAll(fallbackResourceManager.listResources(string, predicate));
		}

		return map;
	}

	@Override
	public Map<ResourceLocation, List<Resource>> listResourceStacks(String string, Predicate<ResourceLocation> predicate) {
		Map<ResourceLocation, List<Resource>> map = new TreeMap();

		for (FallbackResourceManager fallbackResourceManager : this.namespacedManagers.values()) {
			map.putAll(fallbackResourceManager.listResourceStacks(string, predicate));
		}

		return map;
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
