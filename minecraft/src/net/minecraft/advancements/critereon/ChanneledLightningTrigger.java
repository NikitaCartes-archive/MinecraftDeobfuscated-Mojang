package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class ChanneledLightningTrigger extends SimpleCriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("channeled_lightning");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public ChanneledLightningTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		EntityPredicate.Composite[] composites = EntityPredicate.Composite.fromJsonArray(jsonObject, "victims", deserializationContext);
		return new ChanneledLightningTrigger.TriggerInstance(composite, composites);
	}

	public void trigger(ServerPlayer serverPlayer, Collection<? extends Entity> collection) {
		List<LootContext> list = (List<LootContext>)collection.stream()
			.map(entity -> EntityPredicate.createContext(serverPlayer, entity))
			.collect(Collectors.toList());
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(list));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate.Composite[] victims;

		public TriggerInstance(EntityPredicate.Composite composite, EntityPredicate.Composite[] composites) {
			super(ChanneledLightningTrigger.ID, composite);
			this.victims = composites;
		}

		public static ChanneledLightningTrigger.TriggerInstance channeledLightning(EntityPredicate... entityPredicates) {
			return new ChanneledLightningTrigger.TriggerInstance(
				EntityPredicate.Composite.ANY,
				(EntityPredicate.Composite[])Stream.of(entityPredicates).map(EntityPredicate.Composite::wrap).toArray(EntityPredicate.Composite[]::new)
			);
		}

		public boolean matches(Collection<? extends LootContext> collection) {
			for (EntityPredicate.Composite composite : this.victims) {
				boolean bl = false;

				for (LootContext lootContext : collection) {
					if (composite.matches(lootContext)) {
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

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("victims", EntityPredicate.Composite.toJson(this.victims, serializationContext));
			return jsonObject;
		}
	}
}
