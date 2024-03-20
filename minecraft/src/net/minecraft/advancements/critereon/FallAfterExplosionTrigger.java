package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;

public class FallAfterExplosionTrigger extends SimpleCriterionTrigger<FallAfterExplosionTrigger.TriggerInstance> {
	@Override
	public Codec<FallAfterExplosionTrigger.TriggerInstance> codec() {
		return FallAfterExplosionTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, Vec3 vec3, @Nullable Entity entity) {
		Vec3 vec32 = serverPlayer.position();
		LootContext lootContext = entity != null ? EntityPredicate.createContext(serverPlayer, entity) : null;
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer.serverLevel(), vec3, vec32, lootContext));
	}

	public static record TriggerInstance(
		Optional<ContextAwarePredicate> player,
		Optional<LocationPredicate> startPosition,
		Optional<DistancePredicate> distance,
		Optional<ContextAwarePredicate> cause
	) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<FallAfterExplosionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(FallAfterExplosionTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "start_position").forGetter(FallAfterExplosionTrigger.TriggerInstance::startPosition),
						ExtraCodecs.strictOptionalField(DistancePredicate.CODEC, "distance").forGetter(FallAfterExplosionTrigger.TriggerInstance::distance),
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "cause").forGetter(FallAfterExplosionTrigger.TriggerInstance::cause)
					)
					.apply(instance, FallAfterExplosionTrigger.TriggerInstance::new)
		);

		public static Criterion<FallAfterExplosionTrigger.TriggerInstance> fallAfterExplosion(DistancePredicate distancePredicate, EntityPredicate.Builder builder) {
			return CriteriaTriggers.FALL_AFTER_EXPLOSION
				.createCriterion(
					new FallAfterExplosionTrigger.TriggerInstance(
						Optional.empty(), Optional.empty(), Optional.of(distancePredicate), Optional.of(EntityPredicate.wrap(builder))
					)
				);
		}

		@Override
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			criterionValidator.validateEntity(this.cause(), ".cause");
		}

		public boolean matches(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32, @Nullable LootContext lootContext) {
			if (this.startPosition.isPresent() && !((LocationPredicate)this.startPosition.get()).matches(serverLevel, vec3.x, vec3.y, vec3.z)) {
				return false;
			} else {
				return this.distance.isPresent() && !((DistancePredicate)this.distance.get()).matches(vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z)
					? false
					: !this.cause.isPresent() || lootContext != null && ((ContextAwarePredicate)this.cause.get()).matches(lootContext);
			}
		}
	}
}
