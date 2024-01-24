package net.minecraft.client.multiplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
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

	public RegistryAccess.Frozen collectGameRegistries(RegistryAccess registryAccess, boolean bl) {
		LayeredRegistryAccess<ClientRegistryLayer> layeredRegistryAccess = ClientRegistryLayer.createRegistryAccess();
		RegistryAccess registryAccess2;
		if (this.contentsCollector != null) {
			RegistryAccess.Frozen frozen = this.contentsCollector.loadRegistries(layeredRegistryAccess.getAccessForLoading(ClientRegistryLayer.REMOTE)).freeze();
			registryAccess2 = layeredRegistryAccess.replaceFrom(ClientRegistryLayer.REMOTE, frozen).compositeAccess();
		} else {
			registryAccess2 = registryAccess;
		}

		if (this.tagCollector != null && !bl) {
			layeredRegistryAccess.getLayer(ClientRegistryLayer.STATIC).registries().forEach(registryEntry -> registryEntry.value().resetTags());
			this.tagCollector.applyTags(registryAccess2);
		}

		return registryAccess2.freeze();
	}

	@Environment(EnvType.CLIENT)
	static class ContentsCollector {
		private final Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> elements = new HashMap();

		public void append(ResourceKey<? extends Registry<?>> resourceKey, List<RegistrySynchronization.PackedRegistryEntry> list) {
			((List)this.elements.computeIfAbsent(resourceKey, resourceKeyx -> new ArrayList())).addAll(list);
		}

		public RegistryAccess loadRegistries(RegistryAccess registryAccess) {
			return RegistryDataLoader.load(this.elements, registryAccess, RegistryDataLoader.SYNCHRONIZED_REGISTRIES);
		}
	}

	@Environment(EnvType.CLIENT)
	static class TagCollector {
		private final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags = new HashMap();

		public void append(ResourceKey<? extends Registry<?>> resourceKey, TagNetworkSerialization.NetworkPayload networkPayload) {
			this.tags.put(resourceKey, networkPayload);
		}

		public void applyTags(RegistryAccess registryAccess) {
			this.tags.forEach((resourceKey, networkPayload) -> networkPayload.applyToRegistry(registryAccess.registryOrThrow(resourceKey)));
		}
	}
}
