package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends SimpleCriterionTrigger<PlayerTrigger.TriggerInstance> {
	public PlayerTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		return new PlayerTrigger.TriggerInstance(optional);
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer, triggerInstance -> true);
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public TriggerInstance(Optional<ContextAwarePredicate> optional) {
			super(optional);
		}

		public static Criterion<PlayerTrigger.TriggerInstance> located(LocationPredicate.Builder builder) {
			return CriteriaTriggers.LOCATION
				.createCriterion(new PlayerTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().located(builder)))));
		}

		public static Criterion<PlayerTrigger.TriggerInstance> located(EntityPredicate.Builder builder) {
			return CriteriaTriggers.LOCATION.createCriterion(new PlayerTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(builder.build()))));
		}

		public static Criterion<PlayerTrigger.TriggerInstance> located(Optional<EntityPredicate> optional) {
			return CriteriaTriggers.LOCATION.createCriterion(new PlayerTrigger.TriggerInstance(EntityPredicate.wrap(optional)));
		}

		public static Criterion<PlayerTrigger.TriggerInstance> sleptInBed() {
			return CriteriaTriggers.SLEPT_IN_BED.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
		}

		public static Criterion<PlayerTrigger.TriggerInstance> raidWon() {
			return CriteriaTriggers.RAID_WIN.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
		}

		public static Criterion<PlayerTrigger.TriggerInstance> avoidVibration() {
			return CriteriaTriggers.AVOID_VIBRATION.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
		}

		public static Criterion<PlayerTrigger.TriggerInstance> tick() {
			return CriteriaTriggers.TICK.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
		}

		public static Criterion<PlayerTrigger.TriggerInstance> walkOnBlockWithEquipment(Block block, Item item) {
			return located(
				EntityPredicate.Builder.entity()
					.equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(item)))
					.steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block)))
			);
		}
	}
}
