package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerInteractTrigger extends SimpleCriterionTrigger<PlayerInteractTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("player_interacted_with_entity");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	protected PlayerInteractTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<ItemPredicate> optional2 = ItemPredicate.fromJson(jsonObject.get("item"));
		Optional<ContextAwarePredicate> optional3 = EntityPredicate.fromJson(jsonObject, "entity", deserializationContext);
		return new PlayerInteractTrigger.TriggerInstance(optional, optional2, optional3);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, Entity entity) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, lootContext));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ItemPredicate> item;
		private final Optional<ContextAwarePredicate> entity;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<ItemPredicate> optional2, Optional<ContextAwarePredicate> optional3) {
			super(PlayerInteractTrigger.ID, optional);
			this.item = optional2;
			this.entity = optional3;
		}

		public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(
			Optional<ContextAwarePredicate> optional, ItemPredicate.Builder builder, Optional<ContextAwarePredicate> optional2
		) {
			return new PlayerInteractTrigger.TriggerInstance(optional, builder.build(), optional2);
		}

		public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(ItemPredicate.Builder builder, Optional<ContextAwarePredicate> optional) {
			return itemUsedOnEntity(Optional.empty(), builder, optional);
		}

		public boolean matches(ItemStack itemStack, LootContext lootContext) {
			return this.item.isPresent() && !((ItemPredicate)this.item.get()).matches(itemStack)
				? false
				: this.entity.isEmpty() || ((ContextAwarePredicate)this.entity.get()).matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.item.ifPresent(itemPredicate -> jsonObject.add("item", itemPredicate.serializeToJson()));
			this.entity.ifPresent(contextAwarePredicate -> jsonObject.add("entity", contextAwarePredicate.toJson()));
			return jsonObject;
		}
	}
}
