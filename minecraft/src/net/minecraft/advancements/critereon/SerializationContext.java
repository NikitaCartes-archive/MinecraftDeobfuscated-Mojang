package net.minecraft.advancements.critereon;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SerializationContext {
	public static final SerializationContext INSTANCE = new SerializationContext();
	private final Gson predicateGson = Deserializers.createConditionSerializer().create();

	public final JsonElement serializeConditions(LootItemCondition[] lootItemConditions) {
		return this.predicateGson.toJsonTree(lootItemConditions);
	}
}
