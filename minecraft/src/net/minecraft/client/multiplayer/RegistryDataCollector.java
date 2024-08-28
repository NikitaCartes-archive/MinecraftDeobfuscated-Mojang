package net.minecraft.client.multiplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagNetworkSerialization;

@Environment(EnvType.CLIENT)
public class RegistryDataCollector {
	@Nullable
	private RegistryDataCollector.ContentsCollector contentsCollector;
	@Nullable
	private RegistryDataCollector.TagCollector tagCollector;

	public void appendContents(ResourceKey<? extends Registry<?>> resourceKey, List<RegistrySynchronization.PackedRegistryEntry> list) {
		if (this.contentsCollector == null) {
			this.contentsCollector = new RegistryDataCollector.ContentsCollector();
		}

		this.contentsCollector.append(resourceKey, list);
	}

	public void appendTags(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> map) {
		if (this.tagCollector == null) {
			this.tagCollector = new RegistryDataCollector.TagCollector();
		}

		map.forEach(this.tagCollector::append);
	}

	private static <T> Registry.PendingTags<T> resolveRegistryTags(
		RegistryAccess.Frozen frozen, ResourceKey<? extends Registry<? extends T>> resourceKey, TagNetworkSerialization.NetworkPayload networkPayload
	) {
		Registry<T> registry = frozen.lookupOrThrow(resourceKey);
		return registry.prepareTagReload(networkPayload.resolve(registry));
	}

	private RegistryAccess loadNewElementsAndTags(ResourceProvider resourceProvider, RegistryDataCollector.ContentsCollector contentsCollector, boolean bl) {
		LayeredRegistryAccess<ClientRegistryLayer> layeredRegistryAccess = ClientRegistryLayer.createRegistryAccess();
		RegistryAccess.Frozen frozen = layeredRegistryAccess.getAccessForLoading(ClientRegistryLayer.REMOTE);
		Map<ResourceKey<? extends Registry<?>>, RegistryDataLoader.NetworkedRegistryData> map = new HashMap();
		contentsCollector.elements
			.forEach((resourceKey, listx) -> map.put(resourceKey, new RegistryDataLoader.NetworkedRegistryData(listx, TagNetworkSerialization.NetworkPayload.EMPTY)));
		List<Registry.PendingTags<?>> list = new ArrayList();
		if (this.tagCollector != null) {
			this.tagCollector.forEach((resourceKey, networkPayload) -> {
				if (!networkPayload.isEmpty()) {
					if (RegistrySynchronization.isNetworkable(resourceKey)) {
						map.compute(resourceKey, (resourceKeyx, networkedRegistryData) -> {
							List<RegistrySynchronization.PackedRegistryEntry> listxx = networkedRegistryData != null ? networkedRegistryData.elements() : List.of();
							return new RegistryDataLoader.NetworkedRegistryData(listxx, networkPayload);
						});
					} else if (!bl) {
						list.add(resolveRegistryTags(frozen, resourceKey, networkPayload));
					}
				}
			});
		}

		List<HolderLookup.RegistryLookup<?>> list2 = TagLoader.buildUpdatedLookups(frozen, list);
		RegistryAccess.Frozen frozen2 = RegistryDataLoader.load(map, resourceProvider, list2, RegistryDataLoader.SYNCHRONIZED_REGISTRIES).freeze();
		RegistryAccess registryAccess = layeredRegistryAccess.replaceFrom(ClientRegistryLayer.REMOTE, frozen2).compositeAccess();
		list.forEach(Registry.PendingTags::apply);
		return registryAccess;
	}

	private void loadOnlyTags(RegistryDataCollector.TagCollector tagCollector, RegistryAccess.Frozen frozen, boolean bl) {
		tagCollector.forEach((resourceKey, networkPayload) -> {
			if (bl || RegistrySynchronization.isNetworkable(resourceKey)) {
				resolveRegistryTags(frozen, resourceKey, networkPayload).apply();
			}
		});
	}

	public RegistryAccess.Frozen collectGameRegistries(ResourceProvider resourceProvider, RegistryAccess.Frozen frozen, boolean bl) {
		RegistryAccess registryAccess;
		if (this.contentsCollector != null) {
			registryAccess = this.loadNewElementsAndTags(resourceProvider, this.contentsCollector, bl);
		} else {
			if (this.tagCollector != null) {
				this.loadOnlyTags(this.tagCollector, frozen, !bl);
			}

			registryAccess = frozen;
		}

		return registryAccess.freeze();
	}

	@Environment(EnvType.CLIENT)
	static class ContentsCollector {
		final Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> elements = new HashMap();

		public void append(ResourceKey<? extends Registry<?>> resourceKey, List<RegistrySynchronization.PackedRegistryEntry> list) {
			((List)this.elements.computeIfAbsent(resourceKey, resourceKeyx -> new ArrayList())).addAll(list);
		}
	}

	@Environment(EnvType.CLIENT)
	static class TagCollector {
		private final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags = new HashMap();

		public void append(ResourceKey<? extends Registry<?>> resourceKey, TagNetworkSerialization.NetworkPayload networkPayload) {
			this.tags.put(resourceKey, networkPayload);
		}

		public void forEach(BiConsumer<? super ResourceKey<? extends Registry<?>>, ? super TagNetworkSerialization.NetworkPayload> biConsumer) {
			this.tags.forEach(biConsumer);
		}
	}
}
