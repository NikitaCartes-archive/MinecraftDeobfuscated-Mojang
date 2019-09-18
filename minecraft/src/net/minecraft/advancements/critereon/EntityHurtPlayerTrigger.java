package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class EntityHurtPlayerTrigger extends SimpleCriterionTrigger<EntityHurtPlayerTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("entity_hurt_player");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public EntityHurtPlayerTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		DamagePredicate damagePredicate = DamagePredicate.fromJson(jsonObject.get("damage"));
		return new EntityHurtPlayerTrigger.TriggerInstance(damagePredicate);
	}

	public void trigger(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, damageSource, f, g, bl));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final DamagePredicate damage;

		public TriggerInstance(DamagePredicate damagePredicate) {
			super(EntityHurtPlayerTrigger.ID);
			this.damage = damagePredicate;
		}

		public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer(DamagePredicate.Builder builder) {
			return new EntityHurtPlayerTrigger.TriggerInstance(builder.build());
		}

		public boolean matches(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
			return this.damage.matches(serverPlayer, damageSource, f, g, bl);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("damage", this.damage.serializeToJson());
			return jsonObject;
		}
	}
}
