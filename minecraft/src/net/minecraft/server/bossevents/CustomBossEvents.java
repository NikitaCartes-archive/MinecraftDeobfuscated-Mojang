package net.minecraft.server.bossevents;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class CustomBossEvents {
	private final Map<ResourceLocation, CustomBossEvent> events = Maps.<ResourceLocation, CustomBossEvent>newHashMap();

	@Nullable
	public CustomBossEvent get(ResourceLocation resourceLocation) {
		return (CustomBossEvent)this.events.get(resourceLocation);
	}

	public CustomBossEvent create(ResourceLocation resourceLocation, Component component) {
		CustomBossEvent customBossEvent = new CustomBossEvent(resourceLocation, component);
		this.events.put(resourceLocation, customBossEvent);
		return customBossEvent;
	}

	public void remove(CustomBossEvent customBossEvent) {
		this.events.remove(customBossEvent.getTextId());
	}

	public Collection<ResourceLocation> getIds() {
		return this.events.keySet();
	}

	public Collection<CustomBossEvent> getEvents() {
		return this.events.values();
	}

	public CompoundTag save(HolderLookup.Provider provider) {
		CompoundTag compoundTag = new CompoundTag();

		for (CustomBossEvent customBossEvent : this.events.values()) {
			compoundTag.put(customBossEvent.getTextId().toString(), customBossEvent.save(provider));
		}

		return compoundTag;
	}

	public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		for (String string : compoundTag.getAllKeys()) {
			ResourceLocation resourceLocation = ResourceLocation.parse(string);
			this.events.put(resourceLocation, CustomBossEvent.load(compoundTag.getCompound(string), resourceLocation, provider));
		}
	}

	public void onPlayerConnect(ServerPlayer serverPlayer) {
		for (CustomBossEvent customBossEvent : this.events.values()) {
			customBossEvent.onPlayerConnect(serverPlayer);
		}
	}

	public void onPlayerDisconnect(ServerPlayer serverPlayer) {
		for (CustomBossEvent customBossEvent : this.events.values()) {
			customBossEvent.onPlayerDisconnect(serverPlayer);
		}
	}
}
