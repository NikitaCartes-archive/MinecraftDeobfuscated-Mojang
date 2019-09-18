package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ChanneledLightningTrigger extends SimpleCriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("channeled_lightning");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public ChanneledLightningTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate[] entityPredicates = EntityPredicate.fromJsonArray(jsonObject.get("victims"));
		return new ChanneledLightningTrigger.TriggerInstance(entityPredicates);
	}

	public void trigger(ServerPlayer serverPlayer, Collection<? extends Entity> collection) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, collection));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate[] victims;

		public TriggerInstance(EntityPredicate[] entityPredicates) {
			super(ChanneledLightningTrigger.ID);
			this.victims = entityPredicates;
		}

		public static ChanneledLightningTrigger.TriggerInstance channeledLightning(EntityPredicate... entityPredicates) {
			return new ChanneledLightningTrigger.TriggerInstance(entityPredicates);
		}

		public boolean matches(ServerPlayer serverPlayer, Collection<? extends Entity> collection) {
			for (EntityPredicate entityPredicate : this.victims) {
				boolean bl = false;

				for (Entity entity : collection) {
					if (entityPredicate.matches(serverPlayer, entity)) {
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
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("victims", EntityPredicate.serializeArrayToJson(this.victims));
			return jsonObject;
		}
	}
}
