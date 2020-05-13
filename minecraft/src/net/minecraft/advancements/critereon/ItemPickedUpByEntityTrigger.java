package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class ItemPickedUpByEntityTrigger extends SimpleCriterionTrigger<ItemPickedUpByEntityTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("thrown_item_picked_up_by_entity");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	protected ItemPickedUpByEntityTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "entity", deserializationContext);
		return new ItemPickedUpByEntityTrigger.TriggerInstance(composite, itemPredicate, composite2);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, Entity entity) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, itemStack, lootContext));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;
		private final EntityPredicate.Composite entity;

		public TriggerInstance(EntityPredicate.Composite composite, ItemPredicate itemPredicate, EntityPredicate.Composite composite2) {
			super(ItemPickedUpByEntityTrigger.ID, composite);
			this.item = itemPredicate;
			this.entity = composite2;
		}

		public static ItemPickedUpByEntityTrigger.TriggerInstance itemPickedUpByEntity(
			EntityPredicate.Composite composite, ItemPredicate.Builder builder, EntityPredicate.Composite composite2
		) {
			return new ItemPickedUpByEntityTrigger.TriggerInstance(composite, builder.build(), composite2);
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
