package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger extends SimpleCriterionTrigger<DistanceTrigger.TriggerInstance> {
	@Override
	public Codec<DistanceTrigger.TriggerInstance> codec() {
		return DistanceTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, Vec3 vec3) {
		Vec3 vec32 = serverPlayer.position();
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer.serverLevel(), vec3, vec32));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<DistanceTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(DistanceTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "start_position").forGetter(DistanceTrigger.TriggerInstance::startPosition),
						ExtraCodecs.strictOptionalField(DistancePredicate.CODEC, "distance").forGetter(DistanceTrigger.TriggerInstance::distance)
					)
					.apply(instance, DistanceTrigger.TriggerInstance::new)
		);

		public static Criterion<DistanceTrigger.TriggerInstance> fallFromHeight(
			EntityPredicate.Builder builder, DistancePredicate distancePredicate, LocationPredicate.Builder builder2
		) {
			return CriteriaTriggers.FALL_FROM_HEIGHT
				.createCriterion(
					new DistanceTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(builder)), Optional.of(builder2.build()), Optional.of(distancePredicate))
				);
		}

		public static Criterion<DistanceTrigger.TriggerInstance> rideEntityInLava(EntityPredicate.Builder builder, DistancePredicate distancePredicate) {
			return CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER
				.createCriterion(new DistanceTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(builder)), Optional.empty(), Optional.of(distancePredicate)));
		}

		public static Criterion<DistanceTrigger.TriggerInstance> travelledThroughNether(DistancePredicate distancePredicate) {
			return CriteriaTriggers.NETHER_TRAVEL
				.createCriterion(new DistanceTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(distancePredicate)));
		}

		public boolean matches(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32) {
			return this.startPosition.isPresent() && !((LocationPredicate)this.startPosition.get()).matches(serverLevel, vec3.x, vec3.y, vec3.z)
				? false
				: !this.distance.isPresent() || ((DistancePredicate)this.distance.get()).matches(vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z);
		}
	}
}
