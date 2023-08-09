package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class TradeTrigger extends SimpleCriterionTrigger<TradeTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("villager_trade");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public TradeTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<ContextAwarePredicate> optional2 = EntityPredicate.fromJson(jsonObject, "villager", deserializationContext);
		Optional<ItemPredicate> optional3 = ItemPredicate.fromJson(jsonObject.get("item"));
		return new TradeTrigger.TriggerInstance(optional, optional2, optional3);
	}

	public void trigger(ServerPlayer serverPlayer, AbstractVillager abstractVillager, ItemStack itemStack) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, abstractVillager);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ContextAwarePredicate> villager;
		private final Optional<ItemPredicate> item;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional2, Optional<ItemPredicate> optional3) {
			super(TradeTrigger.ID, optional);
			this.villager = optional2;
			this.item = optional3;
		}

		public static TradeTrigger.TriggerInstance tradedWithVillager() {
			return new TradeTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty());
		}

		public static TradeTrigger.TriggerInstance tradedWithVillager(EntityPredicate.Builder builder) {
			return new TradeTrigger.TriggerInstance(EntityPredicate.wrap(builder), Optional.empty(), Optional.empty());
		}

		public boolean matches(LootContext lootContext, ItemStack itemStack) {
			return this.villager.isPresent() && !((ContextAwarePredicate)this.villager.get()).matches(lootContext)
				? false
				: !this.item.isPresent() || ((ItemPredicate)this.item.get()).matches(itemStack);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.item.ifPresent(itemPredicate -> jsonObject.add("item", itemPredicate.serializeToJson()));
			this.villager.ifPresent(contextAwarePredicate -> jsonObject.add("villager", contextAwarePredicate.toJson()));
			return jsonObject;
		}
	}
}
