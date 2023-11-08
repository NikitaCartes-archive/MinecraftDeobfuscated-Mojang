package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerHurtEntityTrigger extends SimpleCriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {
	@Override
	public Codec<PlayerHurtEntityTrigger.TriggerInstance> codec() {
		return PlayerHurtEntityTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource, float f, float g, boolean bl) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, lootContext, damageSource, f, g, bl));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DamagePredicate> damage, Optional<ContextAwarePredicate> entity)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<PlayerHurtEntityTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(PlayerHurtEntityTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(DamagePredicate.CODEC, "damage").forGetter(PlayerHurtEntityTrigger.TriggerInstance::damage),
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "entity").forGetter(PlayerHurtEntityTrigger.TriggerInstance::entity)
					)
					.apply(instance, PlayerHurtEntityTrigger.TriggerInstance::new)
		);

		public static Criterion<PlayerHurtEntityTrigger.TriggerInstance> playerHurtEntity() {
			return CriteriaTriggers.PLAYER_HURT_ENTITY
				.createCriterion(new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
		}

		public static Criterion<PlayerHurtEntityTrigger.TriggerInstance> playerHurtEntityWithDamage(Optional<DamagePredicate> optional) {
			return CriteriaTriggers.PLAYER_HURT_ENTITY.createCriterion(new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), optional, Optional.empty()));
		}

		public static Criterion<PlayerHurtEntityTrigger.TriggerInstance> playerHurtEntityWithDamage(DamagePredicate.Builder builder) {
			return CriteriaTriggers.PLAYER_HURT_ENTITY
				.createCriterion(new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), Optional.of(builder.build()), Optional.empty()));
		}

		public static Criterion<PlayerHurtEntityTrigger.TriggerInstance> playerHurtEntity(Optional<EntityPredicate> optional) {
			return CriteriaTriggers.PLAYER_HURT_ENTITY
				.createCriterion(new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), Optional.empty(), EntityPredicate.wrap(optional)));
		}

		public static Criterion<PlayerHurtEntityTrigger.TriggerInstance> playerHurtEntity(Optional<DamagePredicate> optional, Optional<EntityPredicate> optional2) {
			return CriteriaTriggers.PLAYER_HURT_ENTITY
				.createCriterion(new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), optional, EntityPredicate.wrap(optional2)));
		}

		public static Criterion<PlayerHurtEntityTrigger.TriggerInstance> playerHurtEntity(DamagePredicate.Builder builder, Optional<EntityPredicate> optional) {
			return CriteriaTriggers.PLAYER_HURT_ENTITY
				.createCriterion(new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), Optional.of(builder.build()), EntityPredicate.wrap(optional)));
		}

		public boolean matches(ServerPlayer serverPlayer, LootContext lootContext, DamageSource damageSource, float f, float g, boolean bl) {
			return this.damage.isPresent() && !((DamagePredicate)this.damage.get()).matches(serverPlayer, damageSource, f, g, bl)
				? false
				: !this.entity.isPresent() || ((ContextAwarePredicate)this.entity.get()).matches(lootContext);
		}

		@Override
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			criterionValidator.validateEntity(this.entity, ".entity");
		}
	}
}
