package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

public class FishingRodHookedTrigger extends SimpleCriterionTrigger<FishingRodHookedTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("fishing_rod_hooked");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public FishingRodHookedTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("rod"));
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("entity"));
		ItemPredicate itemPredicate2 = ItemPredicate.fromJson(jsonObject.get("item"));
		return new FishingRodHookedTrigger.TriggerInstance(itemPredicate, entityPredicate, itemPredicate2);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, FishingHook fishingHook, Collection<ItemStack> collection) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, itemStack, fishingHook, collection));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate rod;
		private final EntityPredicate entity;
		private final ItemPredicate item;

		public TriggerInstance(ItemPredicate itemPredicate, EntityPredicate entityPredicate, ItemPredicate itemPredicate2) {
			super(FishingRodHookedTrigger.ID);
			this.rod = itemPredicate;
			this.entity = entityPredicate;
			this.item = itemPredicate2;
		}

		public static FishingRodHookedTrigger.TriggerInstance fishedItem(ItemPredicate itemPredicate, EntityPredicate entityPredicate, ItemPredicate itemPredicate2) {
			return new FishingRodHookedTrigger.TriggerInstance(itemPredicate, entityPredicate, itemPredicate2);
		}

		public boolean matches(ServerPlayer serverPlayer, ItemStack itemStack, FishingHook fishingHook, Collection<ItemStack> collection) {
			if (!this.rod.matches(itemStack)) {
				return false;
			} else {
				Entity entity = fishingHook.getHookedIn();
				if (!this.entity.matches(serverPlayer, entity)) {
					return false;
				} else {
					if (this.item != ItemPredicate.ANY) {
						boolean bl = false;
						if (entity instanceof ItemEntity) {
							ItemEntity itemEntity = (ItemEntity)entity;
							if (this.item.matches(itemEntity.getItem())) {
								bl = true;
							}
						}

						for (ItemStack itemStack2 : collection) {
							if (this.item.matches(itemStack2)) {
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
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("rod", this.rod.serializeToJson());
			jsonObject.add("entity", this.entity.serializeToJson());
			jsonObject.add("item", this.item.serializeToJson());
			return jsonObject;
		}
	}
}
