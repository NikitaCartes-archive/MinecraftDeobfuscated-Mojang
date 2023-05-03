package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PickedUpItemTrigger extends SimpleCriterionTrigger<PickedUpItemTrigger.TriggerInstance> {
	private final ResourceLocation id;

	public PickedUpItemTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	protected PickedUpItemTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		ContextAwarePredicate contextAwarePredicate2 = EntityPredicate.fromJson(jsonObject, "entity", deserializationContext);
		return new PickedUpItemTrigger.TriggerInstance(this.id, contextAwarePredicate, itemPredicate, contextAwarePredicate2);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, @Nullable Entity entity) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, itemStack, lootContext));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;
		private final ContextAwarePredicate entity;

		public TriggerInstance(
			ResourceLocation resourceLocation, ContextAwarePredicate contextAwarePredicate, ItemPredicate itemPredicate, ContextAwarePredicate contextAwarePredicate2
		) {
			super(resourceLocation, contextAwarePredicate);
			this.item = itemPredicate;
			this.entity = contextAwarePredicate2;
		}

		public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByEntity(
			ContextAwarePredicate contextAwarePredicate, ItemPredicate itemPredicate, ContextAwarePredicate contextAwarePredicate2
		) {
			return new PickedUpItemTrigger.TriggerInstance(
				CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.getId(), contextAwarePredicate, itemPredicate, contextAwarePredicate2
			);
		}

		public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByPlayer(
			ContextAwarePredicate contextAwarePredicate, ItemPredicate itemPredicate, ContextAwarePredicate contextAwarePredicate2
		) {
			return new PickedUpItemTrigger.TriggerInstance(
				CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.getId(), contextAwarePredicate, itemPredicate, contextAwarePredicate2
			);
		}

		public boolean matches(ServerPlayer serverPlayer, ItemStack itemStack, LootContext lootContext) {
			return !this.item.matches(itemStack) ? false : this.entity.matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("item", this.item.serializeToJson());
			jsonObject.add("entity", this.entity.toJson(serializationContext));
			return jsonObject;
		}
	}
}
