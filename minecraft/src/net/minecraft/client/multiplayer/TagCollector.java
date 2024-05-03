package net.minecraft.client.multiplayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

@Environment(EnvType.CLIENT)
public class TagCollector {
	private final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags = new HashMap();

	public void append(ResourceKey<? extends Registry<?>> resourceKey, TagNetworkSerialization.NetworkPayload networkPayload) {
		this.tags.put(resourceKey, networkPayload);
	}

	private static void refreshBuiltInTagDependentData() {
		AbstractFurnaceBlockEntity.invalidateCache();
		Blocks.rebuildCache();
	}

	private void applyTags(RegistryAccess registryAccess, Predicate<ResourceKey<? extends Registry<?>>> predicate) {
		this.tags.forEach((resourceKey, networkPayload) -> {
			if (predicate.test(resourceKey)) {
				networkPayload.applyToRegistry(registryAccess.registryOrThrow(resourceKey));
			}
		});
	}

	public void updateTags(RegistryAccess registryAccess, boolean bl) {
		if (bl) {
			this.applyTags(registryAccess, RegistrySynchronization.NETWORKABLE_REGISTRIES::contains);
		} else {
			registryAccess.registries()
				.filter(registryEntry -> !RegistrySynchronization.NETWORKABLE_REGISTRIES.contains(registryEntry.key()))
				.forEach(registryEntry -> registryEntry.value().resetTags());
			this.applyTags(registryAccess, resourceKey -> true);
			refreshBuiltInTagDependentData();
		}
	}
}
