package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends SimpleCriterionTrigger<PlayerTrigger.TriggerInstance> {
	@Override
	public Codec<PlayerTrigger.TriggerInstance> codec() {
		return PlayerTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer, triggerInstance -> true);
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<PlayerTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PlayerTrigger.TriggerInstance::player))
					.apply(instance, PlayerTrigger.TriggerInstance::new)
		);

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

		public static Criterion<PlayerTrigger.TriggerInstance> walkOnBlockWithEquipment(
			HolderGetter<Block> holderGetter, HolderGetter<Item> holderGetter2, Block block, Item item
		) {
			return located(
				EntityPredicate.Builder.entity()
					.equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(holderGetter2, item)))
					.steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(holderGetter, block)))
			);
		}
	}
}
