package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class FallbackResourceManager implements ResourceManager {
	static final Logger LOGGER = LogUtils.getLogger();
	protected final List<FallbackResourceManager.PackEntry> fallbacks = Lists.<FallbackResourceManager.PackEntry>newArrayList();
	private final PackType type;
	private final String namespace;

	public FallbackResourceManager(PackType packType, String string) {
		this.type = packType;
		this.namespace = string;
	}

	public void push(PackResources packResources) {
		this.pushInternal(packResources.packId(), packResources, null);
	}

	public void push(PackResources packResources, Predicate<ResourceLocation> predicate) {
		this.pushInternal(packResources.packId(), packResources, predicate);
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
		for (int i = this.fallbacks.size() - 1; i >= 0; i--) {
			FallbackResourceManager.PackEntry packEntry = (FallbackResourceManager.PackEntry)this.fallbacks.get(i);
			PackResources packResources = packEntry.resources;
			if (packResources != null) {
				IoSupplier<InputStream> ioSupplier = packResources.getResource(this.type, resourceLocation);
				if (ioSupplier != null) {
					IoSupplier<ResourceMetadata> ioSupplier2 = this.createStackMetadataFinder(resourceLocation, i);
					return Optional.of(createResource(packResources, resourceLocation, ioSupplier, ioSupplier2));
				}
			}

			if (packEntry.isFiltered(resourceLocation)) {
				LOGGER.warn("Resource {} not found, but was filtered by pack {}", resourceLocation, packEntry.name);
				return Optional.empty();
			}
		}

		return Optional.empty();
	}

	private static Resource createResource(
		PackResources packResources, ResourceLocation resourceLocation, IoSupplier<InputStream> ioSupplier, IoSupplier<ResourceMetadata> ioSupplier2
	) {
		return new Resource(packResources, wrapForDebug(resourceLocation, packResources, ioSupplier), ioSupplier2);
	}

	private static IoSupplier<InputStream> wrapForDebug(ResourceLocation resourceLocation, PackResources packResources, IoSupplier<InputStream> ioSupplier) {
		return LOGGER.isDebugEnabled()
			? () -> new FallbackResourceManager.LeakedResourceWarningInputStream(ioSupplier.get(), resourceLocation, packResources.packId())
			: ioSupplier;
	}

	@Override
	public List<Resource> getResourceStack(ResourceLocation resourceLocation) {
		ResourceLocation resourceLocation2 = getMetadataLocation(resourceLocation);
		List<Resource> list = new ArrayList();
		boolean bl = false;
		String string = null;

		for (int i = this.fallbacks.size() - 1; i >= 0; i--) {
			FallbackResourceManager.PackEntry packEntry = (FallbackResourceManager.PackEntry)this.fallbacks.get(i);
			PackResources packResources = packEntry.resources;
			if (packResources != null) {
				IoSupplier<InputStream> ioSupplier = packResources.getResource(this.type, resourceLocation);
				if (ioSupplier != null) {
					IoSupplier<ResourceMetadata> ioSupplier2;
					if (bl) {
						ioSupplier2 = ResourceMetadata.EMPTY_SUPPLIER;
					} else {
						ioSupplier2 = () -> {
							IoSupplier<InputStream> ioSupplierx = packResources.getResource(this.type, resourceLocation2);
							return ioSupplierx != null ? parseMetadata(ioSupplierx) : ResourceMetadata.EMPTY;
						};
					}

					list.add(new Resource(packResources, ioSupplier, ioSupplier2));
				}
			}

			if (packEntry.isFiltered(resourceLocation)) {
				string = packEntry.name;
				break;
			}

			if (packEntry.isFiltered(resourceLocation2)) {
				bl = true;
			}
		}

		if (list.isEmpty() && string != null) {
			LOGGER.warn("Resource {} not found, but was filtered by pack {}", resourceLocation, string);
		}

		return Lists.reverse(list);
	}

	private static boolean isMetadata(ResourceLocation resourceLocation) {
		return resourceLocation.getPath().endsWith(".mcmeta");
	}

	private static ResourceLocation getResourceLocationFromMetadata(ResourceLocation resourceLocation) {
		String string = resourceLocation.getPath().substring(0, resourceLocation.getPath().length() - ".mcmeta".length());
		return resourceLocation.withPath(string);
	}

	static ResourceLocation getMetadataLocation(ResourceLocation resourceLocation) {
		return resourceLocation.withPath(resourceLocation.getPath() + ".mcmeta");
	}

	@Override
	public Map<ResourceLocation, Resource> listResources(String string, Predicate<ResourceLocation> predicate) {
		record ResourceWithSourceAndIndex(PackResources packResources, IoSupplier<InputStream> resource, int packIndex) {
		}

		Map<ResourceLocation, ResourceWithSourceAndIndex> map = new HashMap();
		Map<ResourceLocation, ResourceWithSourceAndIndex> map2 = new HashMap();
		int i = this.fallbacks.size();

		for (int j = 0; j < i; j++) {
			FallbackResourceManager.PackEntry packEntry = (FallbackResourceManager.PackEntry)this.fallbacks.get(j);
			packEntry.filterAll(map.keySet());
			packEntry.filterAll(map2.keySet());
			PackResources packResources = packEntry.resources;
			if (packResources != null) {
				int k = j;
				packResources.listResources(this.type, this.namespace, string, (resourceLocation, ioSupplier) -> {
					if (isMetadata(resourceLocation)) {
						if (predicate.test(getResourceLocationFromMetadata(resourceLocation))) {
							map2.put(resourceLocation, new ResourceWithSourceAndIndex(packResources, ioSupplier, k));
						}
					} else if (predicate.test(resourceLocation)) {
						map.put(resourceLocation, new ResourceWithSourceAndIndex(packResources, ioSupplier, k));
					}
				});
			}
		}

		Map<ResourceLocation, Resource> map3 = Maps.<ResourceLocation, Resource>newTreeMap();
		map.forEach((resourceLocation, arg) -> {
			ResourceLocation resourceLocation2 = getMetadataLocation(resourceLocation);
			ResourceWithSourceAndIndex lv = (ResourceWithSourceAndIndex)map2.get(resourceLocation2);
			IoSupplier<ResourceMetadata> ioSupplier;
			if (lv != null && lv.packIndex >= arg.packIndex) {
				ioSupplier = convertToMetadata(lv.resource);
			} else {
				ioSupplier = ResourceMetadata.EMPTY_SUPPLIER;
			}

			map3.put(resourceLocation, createResource(arg.packResources, resourceLocation, arg.resource, ioSupplier));
		});
		return map3;
	}

	private IoSupplier<ResourceMetadata> createStackMetadataFinder(ResourceLocation resourceLocation, int i) {
		return () -> {
			ResourceLocation resourceLocation2 = getMetadataLocation(resourceLocation);

			for (int j = this.fallbacks.size() - 1; j >= i; j--) {
				FallbackResourceManager.PackEntry packEntry = (FallbackResourceManager.PackEntry)this.fallbacks.get(j);
				PackResources packResources = packEntry.resources;
				if (packResources != null) {
					IoSupplier<InputStream> ioSupplier = packResources.getResource(this.type, resourceLocation2);
					if (ioSupplier != null) {
						return parseMetadata(ioSupplier);
					}
				}

				if (packEntry.isFiltered(resourceLocation2)) {
					break;
				}
			}

			return ResourceMetadata.EMPTY;
		};
	}

	private static IoSupplier<ResourceMetadata> convertToMetadata(IoSupplier<InputStream> ioSupplier) {
		return () -> parseMetadata(ioSupplier);
	}

	private static ResourceMetadata parseMetadata(IoSupplier<InputStream> ioSupplier) throws IOException {
		InputStream inputStream = ioSupplier.get();

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
	}

	private static void applyPackFiltersToExistingResources(
		FallbackResourceManager.PackEntry packEntry, Map<ResourceLocation, FallbackResourceManager.EntryStack> map
	) {
		for (FallbackResourceManager.EntryStack entryStack : map.values()) {
			if (packEntry.isFiltered(entryStack.fileLocation)) {
				entryStack.fileSources.clear();
			} else if (packEntry.isFiltered(entryStack.metadataLocation())) {
				entryStack.metaSources.clear();
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
			packResources.listResources(
				this.type,
				this.namespace,
				string,
				(resourceLocation, ioSupplier) -> {
					if (isMetadata(resourceLocation)) {
						ResourceLocation resourceLocation2 = getResourceLocationFromMetadata(resourceLocation);
						if (!predicate.test(resourceLocation2)) {
							return;
						}

						((FallbackResourceManager.EntryStack)map.computeIfAbsent(resourceLocation2, FallbackResourceManager.EntryStack::new))
							.metaSources
							.put(packResources, ioSupplier);
					} else {
						if (!predicate.test(resourceLocation)) {
							return;
						}

						((FallbackResourceManager.EntryStack)map.computeIfAbsent(resourceLocation, FallbackResourceManager.EntryStack::new))
							.fileSources
							.add(new FallbackResourceManager.ResourceWithSource(packResources, ioSupplier));
					}
				}
			);
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

		for (FallbackResourceManager.EntryStack entryStack : map.values()) {
			if (!entryStack.fileSources.isEmpty()) {
				List<Resource> list = new ArrayList();

				for (FallbackResourceManager.ResourceWithSource resourceWithSource : entryStack.fileSources) {
					PackResources packResources = resourceWithSource.source;
					IoSupplier<InputStream> ioSupplier = (IoSupplier<InputStream>)entryStack.metaSources.get(packResources);
					IoSupplier<ResourceMetadata> ioSupplier2 = ioSupplier != null ? convertToMetadata(ioSupplier) : ResourceMetadata.EMPTY_SUPPLIER;
					list.add(createResource(packResources, entryStack.fileLocation, resourceWithSource.resource, ioSupplier2));
				}

				treeMap.put(entryStack.fileLocation, list);
			}
		}

		return treeMap;
	}

	@Override
	public Stream<PackResources> listPacks() {
		return this.fallbacks.stream().map(packEntry -> packEntry.resources).filter(Objects::nonNull);
	}

	static record EntryStack(
		ResourceLocation fileLocation,
		ResourceLocation metadataLocation,
		List<FallbackResourceManager.ResourceWithSource> fileSources,
		Map<PackResources, IoSupplier<InputStream>> metaSources
	) {

		EntryStack(ResourceLocation resourceLocation) {
			this(resourceLocation, FallbackResourceManager.getMetadataLocation(resourceLocation), new ArrayList(), new Object2ObjectArrayMap<>());
		}
	}

	static class LeakedResourceWarningInputStream extends FilterInputStream {
		private final Supplier<String> message;
		private boolean closed;

		public LeakedResourceWarningInputStream(InputStream inputStream, ResourceLocation resourceLocation, String string) {
			super(inputStream);
			Exception exception = new Exception("Stacktrace");
			this.message = () -> {
				StringWriter stringWriter = new StringWriter();
				exception.printStackTrace(new PrintWriter(stringWriter));
				return "Leaked resource: '" + resourceLocation + "' loaded from pack: '" + string + "'\n" + stringWriter;
			};
		}

		public void close() throws IOException {
			super.close();
			this.closed = true;
		}

		protected void finalize() throws Throwable {
			if (!this.closed) {
				FallbackResourceManager.LOGGER.warn("{}", this.message.get());
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

	static record ResourceWithSource(PackResources source, IoSupplier<InputStream> resource) {
	}
}
