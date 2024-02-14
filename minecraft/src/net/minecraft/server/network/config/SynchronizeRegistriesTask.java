package net.minecraft.server.network.config;

import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.tags.TagNetworkSerialization;

public class SynchronizeRegistriesTask implements ConfigurationTask {
	public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type("synchronize_registries");
	private final List<KnownPack> requestedPacks;
	private final LayeredRegistryAccess<RegistryLayer> registries;

	public SynchronizeRegistriesTask(List<KnownPack> list, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
		this.requestedPacks = list;
		this.registries = layeredRegistryAccess;
	}

	@Override
	public void start(Consumer<Packet<?>> consumer) {
		consumer.accept(new ClientboundSelectKnownPacks(this.requestedPacks));
	}

	private void sendRegistries(Consumer<Packet<?>> consumer, Set<KnownPack> set) {
		DynamicOps<Tag> dynamicOps = this.registries.compositeAccess().createSerializationContext(NbtOps.INSTANCE);
		RegistrySynchronization.packRegistries(
			dynamicOps,
			this.registries.getAccessFrom(RegistryLayer.WORLDGEN),
			set,
			(resourceKey, list) -> consumer.accept(new ClientboundRegistryDataPacket(resourceKey, list))
		);
		consumer.accept(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
	}

	public void handleResponse(List<KnownPack> list, Consumer<Packet<?>> consumer) {
		if (list.equals(this.requestedPacks)) {
			this.sendRegistries(consumer, Set.copyOf(this.requestedPacks));
		} else {
			this.sendRegistries(consumer, Set.of());
		}
	}

	@Override
	public ConfigurationTask.Type type() {
		return TYPE;
	}
}
