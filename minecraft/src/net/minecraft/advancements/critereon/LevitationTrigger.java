package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger extends SimpleCriterionTrigger<LevitationTrigger.TriggerInstance> {
	@Override
	public Codec<LevitationTrigger.TriggerInstance> codec() {
		return LevitationTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, Vec3 vec3, int i) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, vec3, i));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DistancePredicate> distance, MinMaxBounds.Ints duration)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<LevitationTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(LevitationTrigger.TriggerInstance::player),
						DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(LevitationTrigger.TriggerInstance::distance),
						MinMaxBounds.Ints.CODEC.optionalFieldOf("duration", MinMaxBounds.Ints.ANY).forGetter(LevitationTrigger.TriggerInstance::duration)
					)
					.apply(instance, LevitationTrigger.TriggerInstance::new)
		);

		public static Criterion<LevitationTrigger.TriggerInstance> levitated(DistancePredicate distancePredicate) {
			return CriteriaTriggers.LEVITATION
				.createCriterion(new LevitationTrigger.TriggerInstance(Optional.empty(), Optional.of(distancePredicate), MinMaxBounds.Ints.ANY));
		}

		public boolean matches(ServerPlayer serverPlayer, Vec3 vec3, int i) {
			return this.distance.isPresent()
					&& !((DistancePredicate)this.distance.get()).matches(vec3.x, vec3.y, vec3.z, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ())
				? false
				: this.duration.matches(i);
		}
	}
}
