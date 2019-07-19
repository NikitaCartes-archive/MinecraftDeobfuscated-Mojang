package net.minecraft.tags;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SynchronizableTagCollection<T> extends TagCollection<T> {
	private final Registry<T> registry;

	public SynchronizableTagCollection(Registry<T> registry, String string, String string2) {
		super(registry::getOptional, string, false, string2);
		this.registry = registry;
	}

	public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		Map<ResourceLocation, Tag<T>> map = this.getAllTags();
		friendlyByteBuf.writeVarInt(map.size());

		for (Entry<ResourceLocation, Tag<T>> entry : map.entrySet()) {
			friendlyByteBuf.writeResourceLocation((ResourceLocation)entry.getKey());
			friendlyByteBuf.writeVarInt(((Tag)entry.getValue()).getValues().size());

			for (T object : ((Tag)entry.getValue()).getValues()) {
				friendlyByteBuf.writeVarInt(this.registry.getId(object));
			}
		}
	}

	public void loadFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		Map<ResourceLocation, Tag<T>> map = Maps.<ResourceLocation, Tag<T>>newHashMap();
		int i = friendlyByteBuf.readVarInt();

		for (int j = 0; j < i; j++) {
			ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
			int k = friendlyByteBuf.readVarInt();
			Tag.Builder<T> builder = Tag.Builder.tag();

			for (int l = 0; l < k; l++) {
				builder.add(this.registry.byId(friendlyByteBuf.readVarInt()));
			}

			map.put(resourceLocation, builder.build(resourceLocation));
		}

		this.replace(map);
	}
}
