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
	static final ResourceLocation ID = new ResourceLocation("channeled_lightning");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public ChanneledLightningTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ContextAwarePredicate[] contextAwarePredicates = EntityPredicate.fromJsonArray(jsonObject, "victims", deserializationContext);
		return new ChanneledLightningTrigger.TriggerInstance(contextAwarePredicate, contextAwarePredicates);
	}

	public void trigger(ServerPlayer serverPlayer, Collection<? extends Entity> collection) {
		List<LootContext> list = (List<LootContext>)collection.stream()
			.map(entity -> EntityPredicate.createContext(serverPlayer, entity))
			.collect(Collectors.toList());
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(list));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ContextAwarePredicate[] victims;

		public TriggerInstance(ContextAwarePredicate contextAwarePredicate, ContextAwarePredicate[] contextAwarePredicates) {
			super(ChanneledLightningTrigger.ID, contextAwarePredicate);
			this.victims = contextAwarePredicates;
		}

		public static ChanneledLightningTrigger.TriggerInstance channeledLightning(EntityPredicate... entityPredicates) {
			return new ChanneledLightningTrigger.TriggerInstance(
				ContextAwarePredicate.ANY, (ContextAwarePredicate[])Stream.of(entityPredicates).map(EntityPredicate::wrap).toArray(ContextAwarePredicate[]::new)
			);
		}

		public boolean matches(Collection<? extends LootContext> collection) {
			for (ContextAwarePredicate contextAwarePredicate : this.victims) {
				boolean bl = false;

				for (LootContext lootContext : collection) {
					if (contextAwarePredicate.matches(lootContext)) {
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
			jsonObject.add("victims", ContextAwarePredicate.toJson(this.victims, serializationContext));
			return jsonObject;
		}
	}
}
