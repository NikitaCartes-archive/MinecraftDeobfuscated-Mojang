package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

public class FallbackResourceManager implements ResourceManager {
	static final Logger LOGGER = LogUtils.getLogger();
	protected final List<FallbackResourceManager.PackEntry> fallbacks = Lists.<FallbackResourceManager.PackEntry>newArrayList();
	final PackType type;
	private final String namespace;

	public FallbackResourceManager(PackType packType, String string) {
		this.type = packType;
		this.namespace = string;
	}

	public void push(PackResources packResources) {
		this.pushInternal(packResources.getName(), packResources, null);
	}

	public void push(PackResources packResources, Predicate<ResourceLocation> predicate) {
		this.pushInternal(packResources.getName(), packResources, predicate);
	}

	public void pushFilterOnly(String string, Predicate<ResourceLocation> predicate) {
		this.pushInternal(string, null, predicate);
	}

	private void pushInternal(String string, @Nullable PackResources packResources, @Nullable Predicate<ResourceLocation> predicate) {
		this.fallbacks.add(new FallbackResourceManager.PackEntry(string, packResources, predicate));
	}

	@Override
	public Set<String> getNamespaces() {
		return ImmutableSet.of(this.namespace);
	}

	@Override
	public Optional<Resource> getResource(ResourceLocation resourceLocation) {
		if (!this.isValidLocation(resourceLocation)) {
			return Optional.empty();
		} else {
			for (int i = this.fallbacks.size() - 1; i >= 0; i--) {
				FallbackResourceManager.PackEntry packEntry = (FallbackResourceManager.PackEntry)this.fallbacks.get(i);
				PackResources packResources = packEntry.resources;
				if (packResources != null && packResources.hasResource(this.type, resourceLocation)) {
					return Optional.of(
						new Resource(packResources.getName(), this.createResourceGetter(resourceLocation, packResources), this.createStackMetadataFinder(resourceLocation, i))
					);
				}

				if (packEntry.isFiltered(resourceLocation)) {
					LOGGER.warn("Resource {} not found, but was filtered by pack {}", resourceLocation, packEntry.name);
					return Optional.empty();
				}
			}

			return Optional.empty();
		}
	}

	Resource.IoSupplier<InputStream> createResourceGetter(ResourceLocation resourceLocation, PackResources packResources) {
		return LOGGER.isDebugEnabled() ? () -> {
			InputStream inputStream = packResources.getResource(this.type, resourceLocation);
			return new FallbackResourceManager.LeakedResourceWarningInputStream(inputStream, resourceLocation, packResources.getName());
		} : () -> packResources.getResource(this.type, resourceLocation);
	}

	private boolean isValidLocation(ResourceLocation resourceLocation) {
		return !resourceLocation.getPath().contains("..");
	}

	@Override
	public List<Resource> getResourceStack(ResourceLocation resourceLocation) {
		if (!this.isValidLocation(resourceLocation)) {
			return List.of();
		} else {
			List<FallbackResourceManager.SinglePackResourceThunkSupplier> list = Lists.<FallbackResourceManager.SinglePackResourceThunkSupplier>newArrayList();
			ResourceLocation resourceLocation2 = getMetadataLocation(resourceLocation);
			String string = null;

			for (FallbackResourceManager.PackEntry packEntry : this.fallbacks) {
				if (packEntry.isFiltered(resourceLocation)) {
					if (!list.isEmpty()) {
						string = packEntry.name;
					}

					list.clear();
				} else if (packEntry.isFiltered(resourceLocation2)) {
					list.forEach(FallbackResourceManager.SinglePackResourceThunkSupplier::ignoreMeta);
				}

				PackResources packResources = packEntry.resources;
				if (packResources != null && packResources.hasResource(this.type, resourceLocation)) {
					list.add(new FallbackResourceManager.SinglePackResourceThunkSupplier(resourceLocation, resourceLocation2, packResources));
				}
			}

			if (list.isEmpty() && string != null) {
				LOGGER.info("Resource {} was filtered by pack {}", resourceLocation, string);
			}

			return list.stream().map(FallbackResourceManager.SinglePackResourceThunkSupplier::create).toList();
		}
	}

	@Override
	public Map<ResourceLocation, Resource> listResources(String string, Predicate<ResourceLocation> predicate) {
		Object2IntMap<ResourceLocation> object2IntMap = new Object2IntOpenHashMap<>();
		int i = this.fallbacks.size();

		for (int j = 0; j < i; j++) {
			FallbackResourceManager.PackEntry packEntry = (FallbackResourceManager.PackEntry)this.fallbacks.get(j);
			packEntry.filterAll(object2IntMap.keySet());
			if (packEntry.resources != null) {
				for (ResourceLocation resourceLocation : packEntry.resources.getResources(this.type, this.namespace, string, predicate)) {
					object2IntMap.put(resourceLocation, j);
				}
			}
		}

		Map<ResourceLocation, Resource> map = Maps.<ResourceLocation, Resource>newTreeMap();

		for (Entry<ResourceLocation> entry : Object2IntMaps.fastIterable(object2IntMap)) {
			int k = entry.getIntValue();
			ResourceLocation resourceLocation2 = (ResourceLocation)entry.getKey();
			PackResources packResources = ((FallbackResourceManager.PackEntry)this.fallbacks.get(k)).resources;
			map.put(
				resourceLocation2,
				new Resource(packResources.getName(), this.createResourceGetter(resourceLocation2, packResources), this.createStackMetadataFinder(resourceLocation2, k))
			);
		}

		return map;
	}

	private Resource.IoSupplier<ResourceMetadata> createStackMetadataFinder(ResourceLocation resourceLocation, int i) {
		return () -> {
			ResourceLocation resourceLocation2 = getMetadataLocation(resourceLocation);

			for (int j = this.fallbacks.size() - 1; j >= i; j--) {
				FallbackResourceManager.PackEntry packEntry = (FallbackResourceManager.PackEntry)this.fallbacks.get(j);
				PackResources packResources = packEntry.resources;
				if (packResources != null && packResources.hasResource(this.type, resourceLocation2)) {
					InputStream inputStream = packResources.getResource(this.type, resourceLocation2);

					ResourceMetadata var8;
					try {
						var8 = ResourceMetadata.fromJsonStream(inputStream);
					} catch (Throwable var11) {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (Throwable var10) {
								var11.addSuppressed(var10);
							}
						}

						throw var11;
					}

					if (inputStream != null) {
						inputStream.close();
					}

					return var8;
				}

				if (packEntry.isFiltered(resourceLocation2)) {
					break;
				}
			}

			return ResourceMetadata.EMPTY;
		};
	}

	private static void applyPackFiltersToExistingResources(
		FallbackResourceManager.PackEntry packEntry, Map<ResourceLocation, FallbackResourceManager.EntryStack> map
	) {
		Iterator<java.util.Map.Entry<ResourceLocation, FallbackResourceManager.EntryStack>> iterator = map.entrySet().iterator();

		while (iterator.hasNext()) {
			java.util.Map.Entry<ResourceLocation, FallbackResourceManager.EntryStack> entry = (java.util.Map.Entry<ResourceLocation, FallbackResourceManager.EntryStack>)iterator.next();
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
			FallbackResourceManager.EntryStack entryStack = (FallbackResourceManager.EntryStack)entry.getValue();
			if (packEntry.isFiltered(resourceLocation)) {
				iterator.remove();
			} else if (packEntry.isFiltered(entryStack.metadataLocation())) {
				entryStack.entries.forEach(FallbackResourceManager.SinglePackResourceThunkSupplier::ignoreMeta);
			}
		}
	}

	private void listPackResources(
		FallbackResourceManager.PackEntry packEntry,
		String string,
		Predicate<ResourceLocation> predicate,
		Map<ResourceLocation, FallbackResourceManager.EntryStack> map
	) {
		PackResources packResources = packEntry.resources;
		if (packResources != null) {
			for (ResourceLocation resourceLocation : packResources.getResources(this.type, this.namespace, string, predicate)) {
				ResourceLocation resourceLocation2 = getMetadataLocation(resourceLocation);
				((FallbackResourceManager.EntryStack)map.computeIfAbsent(
						resourceLocation,
						resourceLocation2x -> new FallbackResourceManager.EntryStack(
								resourceLocation2, Lists.<FallbackResourceManager.SinglePackResourceThunkSupplier>newArrayList()
							)
					))
					.entries()
					.add(new FallbackResourceManager.SinglePackResourceThunkSupplier(resourceLocation, resourceLocation2, packResources));
			}
		}
	}

	@Override
	public Map<ResourceLocation, List<Resource>> listResourceStacks(String string, Predicate<ResourceLocation> predicate) {
		Map<ResourceLocation, FallbackResourceManager.EntryStack> map = Maps.<ResourceLocation, FallbackResourceManager.EntryStack>newHashMap();

		for (FallbackResourceManager.PackEntry packEntry : this.fallbacks) {
			applyPackFiltersToExistingResources(packEntry, map);
			this.listPackResources(packEntry, string, predicate, map);
		}

		TreeMap<ResourceLocation, List<Resource>> treeMap = Maps.newTreeMap();
		map.forEach((resourceLocation, entryStack) -> treeMap.put(resourceLocation, entryStack.createThunks()));
		return treeMap;
	}

	@Override
	public Stream<PackResources> listPacks() {
		return this.fallbacks.stream().map(packEntry -> packEntry.resources).filter(Objects::nonNull);
	}

	static ResourceLocation getMetadataLocation(ResourceLocation resourceLocation) {
		return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + ".mcmeta");
	}

	static record EntryStack(ResourceLocation metadataLocation, List<FallbackResourceManager.SinglePackResourceThunkSupplier> entries) {

		List<Resource> createThunks() {
			return this.entries().stream().map(FallbackResourceManager.SinglePackResourceThunkSupplier::create).toList();
		}
	}

	static class LeakedResourceWarningInputStream extends FilterInputStream {
		private final String message;
		private boolean closed;

		public LeakedResourceWarningInputStream(InputStream inputStream, ResourceLocation resourceLocation, String string) {
			super(inputStream);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			new Exception().printStackTrace(new PrintStream(byteArrayOutputStream));
			this.message = "Leaked resource: '" + resourceLocation + "' loaded from pack: '" + string + "'\n" + byteArrayOutputStream;
		}

		public void close() throws IOException {
			super.close();
			this.closed = true;
		}

		protected void finalize() throws Throwable {
			if (!this.closed) {
				FallbackResourceManager.LOGGER.warn(this.message);
			}

			super.finalize();
		}
	}

	static record PackEntry(String name, @Nullable PackResources resources, @Nullable Predicate<ResourceLocation> filter) {

		public void filterAll(Collection<ResourceLocation> collection) {
			if (this.filter != null) {
				collection.removeIf(this.filter);
			}
		}

		public boolean isFiltered(ResourceLocation resourceLocation) {
			return this.filter != null && this.filter.test(resourceLocation);
		}
	}

	class SinglePackResourceThunkSupplier {
		private final ResourceLocation location;
		private final ResourceLocation metadataLocation;
		private final PackResources source;
		private boolean shouldGetMeta = true;

		SinglePackResourceThunkSupplier(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, PackResources packResources) {
			this.source = packResources;
			this.location = resourceLocation;
			this.metadataLocation = resourceLocation2;
		}

		public void ignoreMeta() {
			this.shouldGetMeta = false;
		}

		public Resource create() {
			String string = this.source.getName();
			return this.shouldGetMeta ? new Resource(string, FallbackResourceManager.this.createResourceGetter(this.location, this.source), () -> {
				if (this.source.hasResource(FallbackResourceManager.this.type, this.metadataLocation)) {
					InputStream inputStream = this.source.getResource(FallbackResourceManager.this.type, this.metadataLocation);

					ResourceMetadata var2;
					try {
						var2 = ResourceMetadata.fromJsonStream(inputStream);
					} catch (Throwable var5) {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (Throwable var4) {
								var5.addSuppressed(var4);
							}
						}

						throw var5;
					}

					if (inputStream != null) {
						inputStream.close();
					}

					return var2;
				} else {
					return ResourceMetadata.EMPTY;
				}
			}) : new Resource(string, FallbackResourceManager.this.createResourceGetter(this.location, this.source));
		}
	}
}
