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
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "entity", deserializationContext);
		return new PickedUpItemTrigger.TriggerInstance(this.id, composite, itemPredicate, composite2);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, @Nullable Entity entity) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, itemStack, lootContext));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;
		private final EntityPredicate.Composite entity;

		public TriggerInstance(
			ResourceLocation resourceLocation, EntityPredicate.Composite composite, ItemPredicate itemPredicate, EntityPredicate.Composite composite2
		) {
			super(resourceLocation, composite);
			this.item = itemPredicate;
			this.entity = composite2;
		}

		public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByEntity(
			EntityPredicate.Composite composite, ItemPredicate itemPredicate, EntityPredicate.Composite composite2
		) {
			return new PickedUpItemTrigger.TriggerInstance(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.getId(), composite, itemPredicate, composite2);
		}

		public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByPlayer(
			EntityPredicate.Composite composite, ItemPredicate itemPredicate, EntityPredicate.Composite composite2
		) {
			return new PickedUpItemTrigger.TriggerInstance(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.getId(), composite, itemPredicate, composite2);
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
