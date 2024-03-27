package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledTrigger extends SimpleCriterionTrigger<KilledTrigger.TriggerInstance> {
	@Override
	public Codec<KilledTrigger.TriggerInstance> codec() {
		return KilledTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, lootContext, damageSource));
	}

	public static record TriggerInstance(
		Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entityPredicate, Optional<DamageSourcePredicate> killingBlow
	) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<KilledTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(KilledTrigger.TriggerInstance::player),
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(KilledTrigger.TriggerInstance::entityPredicate),
						DamageSourcePredicate.CODEC.optionalFieldOf("killing_blow").forGetter(KilledTrigger.TriggerInstance::killingBlow)
					)
					.apply(instance, KilledTrigger.TriggerInstance::new)
		);

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(Optional<EntityPredicate> optional) {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(EntityPredicate.Builder builder) {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity() {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(Optional<EntityPredicate> optional, Optional<DamageSourcePredicate> optional2) {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), optional2));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(EntityPredicate.Builder builder, Optional<DamageSourcePredicate> optional) {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), optional));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(Optional<EntityPredicate> optional, DamageSourcePredicate.Builder builder) {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), Optional.of(builder.build())));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
			return CriteriaTriggers.PLAYER_KILLED_ENTITY
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), Optional.of(builder2.build())));
		}

		public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntityNearSculkCatalyst() {
			return CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> optional) {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(EntityPredicate.Builder builder) {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer() {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> optional, Optional<DamageSourcePredicate> optional2) {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), optional2));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(EntityPredicate.Builder builder, Optional<DamageSourcePredicate> optional) {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), optional));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> optional, DamageSourcePredicate.Builder builder) {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(optional), Optional.of(builder.build())));
		}

		public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
			return CriteriaTriggers.ENTITY_KILLED_PLAYER
				.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(builder)), Optional.of(builder2.build())));
		}

		public boolean matches(ServerPlayer serverPlayer, LootContext lootContext, DamageSource damageSource) {
			return this.killingBlow.isPresent() && !((DamageSourcePredicate)this.killingBlow.get()).matches(serverPlayer, damageSource)
				? false
				: this.entityPredicate.isEmpty() || ((ContextAwarePredicate)this.entityPredicate.get()).matches(lootContext);
		}

		@Override
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			criterionValidator.validateEntity(this.entityPredicate, ".entity");
		}
	}
}
