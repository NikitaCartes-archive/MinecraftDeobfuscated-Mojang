package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class StorageNbtProvider implements NbtProvider {
	final ResourceLocation id;

	StorageNbtProvider(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public LootNbtProviderType getType() {
		return NbtProviders.STORAGE;
	}

	@Nullable
	@Override
	public Tag get(LootContext lootContext) {
		return lootContext.getLevel().getServer().getCommandStorage().get(this.id);
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of();
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<StorageNbtProvider> {
		public void serialize(JsonObject jsonObject, StorageNbtProvider storageNbtProvider, JsonSerializationContext jsonSerializationContext) {
			jsonObject.addProperty("source", storageNbtProvider.id.toString());
		}

		public StorageNbtProvider deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			String string = GsonHelper.getAsString(jsonObject, "source");
			return new StorageNbtProvider(new ResourceLocation(string));
		}
	}
}
