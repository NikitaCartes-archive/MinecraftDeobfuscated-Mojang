package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PickedUpItemTrigger extends SimpleCriterionTrigger<PickedUpItemTrigger.TriggerInstance> {
	protected PickedUpItemTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<ItemPredicate> optional2 = ItemPredicate.fromJson(jsonObject.get("item"));
		Optional<ContextAwarePredicate> optional3 = EntityPredicate.fromJson(jsonObject, "entity", deserializationContext);
		return new PickedUpItemTrigger.TriggerInstance(optional, optional2, optional3);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, @Nullable Entity entity) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, itemStack, lootContext));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ItemPredicate> item;
		private final Optional<ContextAwarePredicate> entity;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<ItemPredicate> optional2, Optional<ContextAwarePredicate> optional3) {
			super(optional);
			this.item = optional2;
			this.entity = optional3;
		}

		public static Criterion<PickedUpItemTrigger.TriggerInstance> thrownItemPickedUpByEntity(
			ContextAwarePredicate contextAwarePredicate, Optional<ItemPredicate> optional, Optional<ContextAwarePredicate> optional2
		) {
			return CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY
				.createCriterion(new PickedUpItemTrigger.TriggerInstance(Optional.of(contextAwarePredicate), optional, optional2));
		}

		public static Criterion<PickedUpItemTrigger.TriggerInstance> thrownItemPickedUpByPlayer(
			Optional<ContextAwarePredicate> optional, Optional<ItemPredicate> optional2, Optional<ContextAwarePredicate> optional3
		) {
			return CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.createCriterion(new PickedUpItemTrigger.TriggerInstance(optional, optional2, optional3));
		}

		public boolean matches(ServerPlayer serverPlayer, ItemStack itemStack, LootContext lootContext) {
			return this.item.isPresent() && !((ItemPredicate)this.item.get()).matches(itemStack)
				? false
				: !this.entity.isPresent() || ((ContextAwarePredicate)this.entity.get()).matches(lootContext);
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
