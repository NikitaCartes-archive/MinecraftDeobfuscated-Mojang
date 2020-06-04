package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("bred_animals");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public BredAnimalsTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "parent", deserializationContext);
		EntityPredicate.Composite composite3 = EntityPredicate.Composite.fromJson(jsonObject, "partner", deserializationContext);
		EntityPredicate.Composite composite4 = EntityPredicate.Composite.fromJson(jsonObject, "child", deserializationContext);
		return new BredAnimalsTrigger.TriggerInstance(composite, composite2, composite3, composite4);
	}

	public void trigger(ServerPlayer serverPlayer, Animal animal, Animal animal2, @Nullable AgableMob agableMob) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, animal);
		LootContext lootContext2 = EntityPredicate.createContext(serverPlayer, animal2);
		LootContext lootContext3 = agableMob != null ? EntityPredicate.createContext(serverPlayer, agableMob) : null;
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, lootContext2, lootContext3));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate.Composite parent;
		private final EntityPredicate.Composite partner;
		private final EntityPredicate.Composite child;

		public TriggerInstance(
			EntityPredicate.Composite composite, EntityPredicate.Composite composite2, EntityPredicate.Composite composite3, EntityPredicate.Composite composite4
		) {
			super(BredAnimalsTrigger.ID, composite);
			this.parent = composite2;
			this.partner = composite3;
			this.child = composite4;
		}

		public static BredAnimalsTrigger.TriggerInstance bredAnimals() {
			return new BredAnimalsTrigger.TriggerInstance(
				EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY
			);
		}

		public static BredAnimalsTrigger.TriggerInstance bredAnimals(EntityPredicate.Builder builder) {
			return new BredAnimalsTrigger.TriggerInstance(
				EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(builder.build())
			);
		}

		public boolean matches(LootContext lootContext, LootContext lootContext2, @Nullable LootContext lootContext3) {
			return lootContext3 != null && !this.child.matches(lootContext3)
				? false
				: this.parent.matches(lootContext) && this.partner.matches(lootContext2) || this.parent.matches(lootContext2) && this.partner.matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("parent", this.parent.toJson(serializationContext));
			jsonObject.add("partner", this.partner.toJson(serializationContext));
			jsonObject.add("child", this.child.toJson(serializationContext));
			return jsonObject;
		}
	}
}
