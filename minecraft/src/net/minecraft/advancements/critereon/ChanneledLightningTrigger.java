package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class ChanneledLightningTrigger extends SimpleCriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {
	public ChanneledLightningTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		List<ContextAwarePredicate> list = EntityPredicate.fromJsonArray(jsonObject, "victims", deserializationContext);
		return new ChanneledLightningTrigger.TriggerInstance(optional, list);
	}

	public void trigger(ServerPlayer serverPlayer, Collection<? extends Entity> collection) {
		List<LootContext> list = (List<LootContext>)collection.stream()
			.map(entity -> EntityPredicate.createContext(serverPlayer, entity))
			.collect(Collectors.toList());
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(list));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final List<ContextAwarePredicate> victims;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, List<ContextAwarePredicate> list) {
			super(optional);
			this.victims = list;
		}

		public static Criterion<ChanneledLightningTrigger.TriggerInstance> channeledLightning(EntityPredicate.Builder... builders) {
			return CriteriaTriggers.CHANNELED_LIGHTNING.createCriterion(new ChanneledLightningTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(builders)));
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
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			jsonObject.add("victims", ContextAwarePredicate.toJson(this.victims));
			return jsonObject;
		}
	}
}
