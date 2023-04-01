package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends SimpleCriterionTrigger<PlayerTrigger.TriggerInstance> {
	final ResourceLocation id;

	public PlayerTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	public PlayerTrigger.TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
		return new PlayerTrigger.TriggerInstance(this.id, composite);
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer, triggerInstance -> true);
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public TriggerInstance(ResourceLocation resourceLocation, EntityPredicate.Composite composite) {
			super(resourceLocation, composite);
		}

		public static PlayerTrigger.TriggerInstance located(LocationPredicate locationPredicate) {
			return new PlayerTrigger.TriggerInstance(
				CriteriaTriggers.LOCATION.id, EntityPredicate.Composite.wrap(EntityPredicate.Builder.entity().located(locationPredicate).build())
			);
		}

		public static PlayerTrigger.TriggerInstance located(EntityPredicate entityPredicate) {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.Composite.wrap(entityPredicate));
		}

		public static PlayerTrigger.TriggerInstance sleptInBed() {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, EntityPredicate.Composite.ANY);
		}

		public static PlayerTrigger.TriggerInstance raidWon() {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, EntityPredicate.Composite.ANY);
		}

		public static PlayerTrigger.TriggerInstance avoidVibration() {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.AVOID_VIBRATION.id, EntityPredicate.Composite.ANY);
		}

		public static PlayerTrigger.TriggerInstance tick() {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.TICK.id, EntityPredicate.Composite.ANY);
		}

		public static PlayerTrigger.TriggerInstance vote(MinMaxBounds.Ints ints) {
			return new PlayerTrigger.TriggerInstance(
				CriteriaTriggers.VOTED.id,
				EntityPredicate.Composite.wrap(
					EntityPredicate.Builder.entity().subPredicate(PlayerPredicate.Builder.player().addStat(Stats.CUSTOM.get(Stats.VOTED), ints).build()).build()
				)
			);
		}

		public static PlayerTrigger.TriggerInstance walkOnBlockWithEquipment(Block block, Item item) {
			return located(
				EntityPredicate.Builder.entity()
					.equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(item).build()).build())
					.steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block).build()).build())
					.build()
			);
		}
	}
}
