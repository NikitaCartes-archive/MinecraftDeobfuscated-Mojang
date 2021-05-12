package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class LootTableTrigger extends SimpleCriterionTrigger<LootTableTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("player_generates_container_loot");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	protected LootTableTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "loot_table"));
		return new LootTableTrigger.TriggerInstance(composite, resourceLocation);
	}

	public void trigger(ServerPlayer serverPlayer, ResourceLocation resourceLocation) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(resourceLocation));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ResourceLocation lootTable;

		public TriggerInstance(EntityPredicate.Composite composite, ResourceLocation resourceLocation) {
			super(LootTableTrigger.ID, composite);
			this.lootTable = resourceLocation;
		}

		public static LootTableTrigger.TriggerInstance lootTableUsed(ResourceLocation resourceLocation) {
			return new LootTableTrigger.TriggerInstance(EntityPredicate.Composite.ANY, resourceLocation);
		}

		public boolean matches(ResourceLocation resourceLocation) {
			return this.lootTable.equals(resourceLocation);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.addProperty("loot_table", this.lootTable.toString());
			return jsonObject;
		}
	}
}
