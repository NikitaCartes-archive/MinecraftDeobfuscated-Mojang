package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;

public class EntityHurtPlayerTrigger extends SimpleCriterionTrigger<EntityHurtPlayerTrigger.TriggerInstance> {
	@Override
	public Codec<EntityHurtPlayerTrigger.TriggerInstance> codec() {
		return EntityHurtPlayerTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, damageSource, f, g, bl));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DamagePredicate> damage)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<EntityHurtPlayerTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(EntityHurtPlayerTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(DamagePredicate.CODEC, "damage").forGetter(EntityHurtPlayerTrigger.TriggerInstance::damage)
					)
					.apply(instance, EntityHurtPlayerTrigger.TriggerInstance::new)
		);

		public static Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer() {
			return CriteriaTriggers.ENTITY_HURT_PLAYER.createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
		}

		public static Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer(DamagePredicate damagePredicate) {
			return CriteriaTriggers.ENTITY_HURT_PLAYER.createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.of(damagePredicate)));
		}

		public static Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer(DamagePredicate.Builder builder) {
			return CriteriaTriggers.ENTITY_HURT_PLAYER.createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.of(builder.build())));
		}

		public boolean matches(ServerPlayer serverPlayer, DamageSource damageSource, float f, float g, boolean bl) {
			return !this.damage.isPresent() || ((DamagePredicate)this.damage.get()).matches(serverPlayer, damageSource, f, g, bl);
		}
	}
}
