package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class FishingRodHookedTrigger extends SimpleCriterionTrigger<FishingRodHookedTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("fishing_rod_hooked");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public FishingRodHookedTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<ItemPredicate> optional2 = ItemPredicate.fromJson(jsonObject.get("rod"));
		Optional<ContextAwarePredicate> optional3 = EntityPredicate.fromJson(jsonObject, "entity", deserializationContext);
		Optional<ItemPredicate> optional4 = ItemPredicate.fromJson(jsonObject.get("item"));
		return new FishingRodHookedTrigger.TriggerInstance(optional, optional2, optional3, optional4);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, FishingHook fishingHook, Collection<ItemStack> collection) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, (Entity)(fishingHook.getHookedIn() != null ? fishingHook.getHookedIn() : fishingHook));
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, lootContext, collection));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ItemPredicate> rod;
		private final Optional<ContextAwarePredicate> entity;
		private final Optional<ItemPredicate> item;

		public TriggerInstance(
			Optional<ContextAwarePredicate> optional, Optional<ItemPredicate> optional2, Optional<ContextAwarePredicate> optional3, Optional<ItemPredicate> optional4
		) {
			super(FishingRodHookedTrigger.ID, optional);
			this.rod = optional2;
			this.entity = optional3;
			this.item = optional4;
		}

		public static FishingRodHookedTrigger.TriggerInstance fishedItem(
			Optional<ItemPredicate> optional, Optional<EntityPredicate> optional2, Optional<ItemPredicate> optional3
		) {
			return new FishingRodHookedTrigger.TriggerInstance(Optional.empty(), optional, EntityPredicate.wrap(optional2), optional3);
		}

		public boolean matches(ItemStack itemStack, LootContext lootContext, Collection<ItemStack> collection) {
			if (this.rod.isPresent() && !((ItemPredicate)this.rod.get()).matches(itemStack)) {
				return false;
			} else if (this.entity.isPresent() && !((ContextAwarePredicate)this.entity.get()).matches(lootContext)) {
				return false;
			} else {
				if (this.item.isPresent()) {
					boolean bl = false;
					Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
					if (entity instanceof ItemEntity itemEntity && ((ItemPredicate)this.item.get()).matches(itemEntity.getItem())) {
						bl = true;
					}

					for (ItemStack itemStack2 : collection) {
						if (((ItemPredicate)this.item.get()).matches(itemStack2)) {
							bl = true;
							break;
						}
					}

					if (!bl) {
						return false;
					}
				}

				return true;
			}
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.rod.ifPresent(itemPredicate -> jsonObject.add("rod", itemPredicate.serializeToJson()));
			this.entity.ifPresent(contextAwarePredicate -> jsonObject.add("entity", contextAwarePredicate.toJson()));
			this.item.ifPresent(itemPredicate -> jsonObject.add("item", itemPredicate.serializeToJson()));
			return jsonObject;
		}
	}
}
