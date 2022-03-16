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
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	public Resource getResource(ResourceLocation resourceLocation) throws IOException {
		this.validateLocation(resourceLocation);
		PackResources packResources = null;
		ResourceLocation resourceLocation2 = getMetadataLocation(resourceLocation);
		boolean bl = false;

		for (FallbackResourceManager.PackEntry packEntry : Lists.reverse(this.fallbacks)) {
			PackResources packResources2 = packEntry.resources;
			if (packResources2 != null) {
				if (!bl) {
					if (packResources2.hasResource(this.type, resourceLocation2)) {
						packResources = packResources2;
						bl = true;
					} else {
						bl = packEntry.isFiltered(resourceLocation2);
					}
				}

				if (packResources2.hasResource(this.type, resourceLocation)) {
					InputStream inputStream = null;
					if (packResources != null) {
						inputStream = this.getWrappedResource(resourceLocation2, packResources);
					}

					return new SimpleResource(packResources2.getName(), resourceLocation, this.getWrappedResource(resourceLocation, packResources2), inputStream);
				}
			}

			if (packEntry.isFiltered(resourceLocation)) {
				throw new FileNotFoundException(resourceLocation + " (filtered by: " + packEntry.name + ")");
			}
		}

		throw new FileNotFoundException(resourceLocation.toString());
	}

	@Override
	public boolean hasResource(ResourceLocation resourceLocation) {
		if (!this.isValidLocation(resourceLocation)) {
			return false;
		} else {
			for (FallbackResourceManager.PackEntry packEntry : Lists.reverse(this.fallbacks)) {
				if (packEntry.hasResource(this.type, resourceLocation)) {
					return true;
				}

				if (packEntry.isFiltered(resourceLocation)) {
					return false;
				}
			}

			return false;
		}
	}

	protected InputStream getWrappedResource(ResourceLocation resourceLocation, PackResources packResources) throws IOException {
		InputStream inputStream = packResources.getResource(this.type, resourceLocation);
		return (InputStream)(LOGGER.isDebugEnabled()
			? new FallbackResourceManager.LeakedResourceWarningInputStream(inputStream, resourceLocation, packResources.getName())
			: inputStream);
	}

	private void validateLocation(ResourceLocation resourceLocation) throws IOException {
		if (!this.isValidLocation(resourceLocation)) {
			throw new IOException("Invalid relative path to resource: " + resourceLocation);
		}
	}

	private boolean isValidLocation(ResourceLocation resourceLocation) {
		return !resourceLocation.getPath().contains("..");
	}

	@Override
	public List<ResourceThunk> getResourceStack(ResourceLocation resourceLocation) throws IOException {
		this.validateLocation(resourceLocation);
		List<FallbackResourceManager.SinglePackResourceThunkSupplier> list = Lists.<FallbackResourceManager.SinglePackResourceThunkSupplier>newArrayList();
		ResourceLocation resourceLocation2 = getMetadataLocation(resourceLocation);
		String string = null;

		for (FallbackResourceManager.PackEntry packEntry : this.fallbacks) {
			if (packEntry.isFiltered(resourceLocation)) {
				list.clear();
				string = packEntry.name;
			} else if (packEntry.isFiltered(resourceLocation2)) {
				list.forEach(FallbackResourceManager.SinglePackResourceThunkSupplier::ignoreMeta);
			}

			PackResources packResources = packEntry.resources;
			if (packResources != null && packResources.hasResource(this.type, resourceLocation)) {
				list.add(new FallbackResourceManager.SinglePackResourceThunkSupplier(resourceLocation, resourceLocation2, packResources));
			}
		}

		if (!list.isEmpty()) {
			return list.stream().map(FallbackResourceManager.SinglePackResourceThunkSupplier::create).toList();
		} else if (string != null) {
			throw new FileNotFoundException(resourceLocation + " (filtered by: " + string + ")");
		} else {
			throw new FileNotFoundException(resourceLocation.toString());
		}
	}

	@Override
	public Map<ResourceLocation, ResourceThunk> listResources(String string, Predicate<ResourceLocation> predicate) {
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

		Map<ResourceLocation, ResourceThunk> map = Maps.<ResourceLocation, ResourceThunk>newTreeMap();

		for (Entry<ResourceLocation> entry : Object2IntMaps.fastIterable(object2IntMap)) {
			int k = entry.getIntValue();
			ResourceLocation resourceLocation2 = (ResourceLocation)entry.getKey();
			PackResources packResources = ((FallbackResourceManager.PackEntry)this.fallbacks.get(k)).resources;
			String string2 = packResources.getName();
			map.put(resourceLocation2, new ResourceThunk(string2, () -> {
				ResourceLocation resourceLocation2x = getMetadataLocation(resourceLocation2);
				InputStream inputStream = null;

				for (int jx = this.fallbacks.size() - 1; jx >= k; jx--) {
					FallbackResourceManager.PackEntry packEntryx = (FallbackResourceManager.PackEntry)this.fallbacks.get(jx);
					PackResources packResources2 = packEntryx.resources;
					if (packResources2 != null && packResources2.hasResource(this.type, resourceLocation2x)) {
						inputStream = this.getWrappedResource(resourceLocation2x, packResources2);
						break;
					}

					if (packEntryx.isFiltered(resourceLocation2x)) {
						break;
					}
				}

				return new SimpleResource(string2, resourceLocation2, this.getWrappedResource(resourceLocation2, packResources), inputStream);
			}));
		}

		return map;
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
	public Map<ResourceLocation, List<ResourceThunk>> listResourceStacks(String string, Predicate<ResourceLocation> predicate) {
		Map<ResourceLocation, FallbackResourceManager.EntryStack> map = Maps.<ResourceLocation, FallbackResourceManager.EntryStack>newHashMap();

		for (FallbackResourceManager.PackEntry packEntry : this.fallbacks) {
			applyPackFiltersToExistingResources(packEntry, map);
			this.listPackResources(packEntry, string, predicate, map);
		}

		TreeMap<ResourceLocation, List<ResourceThunk>> treeMap = Maps.newTreeMap();
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

		List<ResourceThunk> createThunks() {
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

		boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
			return this.resources != null && this.resources.hasResource(packType, resourceLocation);
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

		public ResourceThunk create() {
			String string = this.source.getName();
			return this.shouldGetMeta
				? new ResourceThunk(
					string,
					() -> {
						InputStream inputStream = this.source.hasResource(FallbackResourceManager.this.type, this.metadataLocation)
							? FallbackResourceManager.this.getWrappedResource(this.metadataLocation, this.source)
							: null;
						return new SimpleResource(string, this.location, FallbackResourceManager.this.getWrappedResource(this.location, this.source), inputStream);
					}
				)
				: new ResourceThunk(
					string, () -> new SimpleResource(string, this.location, FallbackResourceManager.this.getWrappedResource(this.location, this.source), null)
				);
		}
	}
}
