package net.minecraft.data.advancements.packs;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.PotatoRefinementTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.advancements.critereon.ThrowLubricatedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

public class PoisonousPotatoAdvancements implements AdvancementSubProvider {
	@Override
	public void generate(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer) {
		AdvancementHolder advancementHolder = potatoAdvancement("root")
			.display(Items.POISONOUS_POTATO.getDefaultInstance(), AdvancementType.TASK, false, false, false)
			.addCriterion("joined_world", PlayerTrigger.TriggerInstance.located(Optional.empty()))
			.save(consumer);
		potatoAdvancement("get_peeled")
			.parent(advancementHolder)
			.display(Items.POTATO_PEELS_MAP.get(DyeColor.WHITE).getDefaultInstance(), AdvancementType.TASK, true, true, false)
			.addCriterion("get_peeled", PlayerTrigger.TriggerInstance.getPeeled())
			.save(consumer);
		AdvancementHolder advancementHolder2 = potatoAdvancement("enter_the_potato")
			.parent(advancementHolder)
			.display(Items.POTATO_OF_KNOWLEDGE.getDefaultInstance(), AdvancementType.TASK, true, true, true)
			.addCriterion("entered_potato", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.POTATO))
			.save(consumer);
		VanillaAdventureAdvancements.addBiomes(potatoAdvancement("all_potatoed"), provider, MultiNoiseBiomeSourceParameterList.Preset.POTATO.usedBiomes().toList())
			.parent(advancementHolder2)
			.display(Items.GRAVTATER.getDefaultInstance(), AdvancementType.CHALLENGE, true, true, false)
			.save(consumer);
		potatoAdvancement("eat_armor")
			.parent(advancementHolder)
			.display(Items.POISONOUS_POTATO_CHESTPLATE.getDefaultInstance(), AdvancementType.TASK, true, true, true)
			.addCriterion("eat_armor", PlayerTrigger.TriggerInstance.eatArmor())
			.save(consumer);
		AdvancementHolder advancementHolder3 = potatoAdvancement("rumbled")
			.parent(advancementHolder)
			.display(Items.POISONOUS_POTATO_PLANT.getDefaultInstance(), AdvancementType.TASK, false, true, false)
			.addCriterion("rumble_plant", PlayerTrigger.TriggerInstance.rumbleThePlant())
			.save(consumer);
		potatoAdvancement("good_plant")
			.parent(advancementHolder3)
			.display(Items.POTATO_STAFF.getDefaultInstance(), AdvancementType.TASK, true, true, false)
			.addCriterion("compost_staff", PlayerTrigger.TriggerInstance.compostedStaff())
			.save(consumer);
		AdvancementHolder advancementHolder4 = potatoAdvancement("get_oily")
			.parent(advancementHolder)
			.display(Items.POTATO_OIL.getDefaultInstance(), AdvancementType.TASK, true, false, false)
			.addCriterion("refine_potato_oil", PotatoRefinementTrigger.TriggerInstance.refined(Items.POTATO_OIL))
			.save(consumer);
		AdvancementHolder advancementHolder5 = potatoAdvancement("lubricate")
			.parent(advancementHolder4)
			.display(Items.POTATO_OIL.getDefaultInstance(), AdvancementType.TASK, true, false, false)
			.addCriterion("lubricate_item", PotatoRefinementTrigger.TriggerInstance.lubricatedAtLeast(1))
			.save(consumer);
		potatoAdvancement("mega_lubricate")
			.parent(advancementHolder4)
			.display(Items.POTATO_OIL.getDefaultInstance().makeFoil(), AdvancementType.TASK, true, false, true)
			.addCriterion("mega_lubricate_item", PotatoRefinementTrigger.TriggerInstance.lubricatedAtLeast(10))
			.save(consumer);
		AdvancementHolder advancementHolder6 = potatoAdvancement("lubricate_whee")
			.parent(advancementHolder5)
			.display(Items.ICE.getDefaultInstance(), AdvancementType.TASK, true, true, true)
			.addCriterion("throw_lubricated_item", ThrowLubricatedTrigger.TriggerInstance.thrownWithAtLeast(1))
			.save(consumer);
		potatoAdvancement("mega_lubricate_whee")
			.parent(advancementHolder6)
			.display(Items.ICE.getDefaultInstance().makeFoil(), AdvancementType.TASK, true, true, true)
			.addCriterion("throw_mega_lubricated_item", ThrowLubricatedTrigger.TriggerInstance.thrownWithAtLeast(10))
			.save(consumer);
		potatoAdvancement("lubricate_boots")
			.parent(advancementHolder5)
			.display(Items.POISONOUS_POTA_TOES.getDefaultInstance(), AdvancementType.TASK, true, true, true)
			.addCriterion("lubricate_boots", PotatoRefinementTrigger.TriggerInstance.lubricatedAtLeast(ItemPredicate.Builder.item().of(ItemTags.FOOT_ARMOR).build(), 1))
			.save(consumer);
		potatoAdvancement("sweet_potato_talker")
			.parent(advancementHolder)
			.display(Items.POTATO_FLOWER.getDefaultInstance(), AdvancementType.TASK, true, true, false)
			.addCriterion("said_potato", PlayerTrigger.TriggerInstance.saidPotato(99))
			.save(consumer);
		potatoAdvancement("craft_poisonous_potato_sticks")
			.parent(advancementHolder)
			.display(Items.POISONOUS_POTATO_STICKS.getDefaultInstance(), AdvancementType.TASK, true, false, false)
			.addCriterion("poisonous_potato_sticks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.POISONOUS_POTATO_STICKS))
			.save(consumer);
		potatoAdvancement("craft_poisonous_potato_slices")
			.parent(advancementHolder)
			.display(Items.POISONOUS_POTATO_SLICES.getDefaultInstance(), AdvancementType.TASK, true, false, false)
			.addCriterion("poisonous_potato_slices", InventoryChangeTrigger.TriggerInstance.hasItems(Items.POISONOUS_POTATO_SLICES))
			.save(consumer);
		potatoAdvancement("craft_poisonous_potato_fries")
			.parent(advancementHolder)
			.display(Items.POISONOUS_POTATO_FRIES.getDefaultInstance(), AdvancementType.TASK, true, false, false)
			.addCriterion("poisonous_potato_fries", InventoryChangeTrigger.TriggerInstance.hasItems(Items.POISONOUS_POTATO_FRIES))
			.save(consumer);
		potatoAdvancement("craft_poisonous_potato_chips")
			.parent(advancementHolder)
			.display(Items.POISONOUS_POTATO_CHIPS.getDefaultInstance(), AdvancementType.TASK, true, false, false)
			.addCriterion("poisonous_potato_chips", InventoryChangeTrigger.TriggerInstance.hasItems(Items.POISONOUS_POTATO_CHIPS))
			.save(consumer);
		AdvancementHolder advancementHolder7 = potatoAdvancement("poisonous_potato_taster")
			.parent(advancementHolder)
			.display(Items.POISONOUS_POTATO_STICKS.getDefaultInstance(), AdvancementType.TASK, true, true, false)
			.addCriterion("ate_poisonous_potato_sticks", ConsumeItemTrigger.TriggerInstance.usedItem(Items.POISONOUS_POTATO_STICKS))
			.addCriterion("ate_poisonous_potato_slices", ConsumeItemTrigger.TriggerInstance.usedItem(Items.POISONOUS_POTATO_SLICES))
			.save(consumer);
		potatoAdvancement("poisonous_potato_gourmet")
			.parent(advancementHolder7)
			.display(Items.POISONOUS_POTATO_CHIPS.getDefaultInstance(), AdvancementType.TASK, true, true, false)
			.addCriterion("ate_poisonous_potato_sticks", ConsumeItemTrigger.TriggerInstance.usedItem(Items.POISONOUS_POTATO_STICKS))
			.addCriterion("ate_poisonous_potato_slices", ConsumeItemTrigger.TriggerInstance.usedItem(Items.POISONOUS_POTATO_SLICES))
			.addCriterion("ate_poisonous_potato_fries", ConsumeItemTrigger.TriggerInstance.usedItem(Items.POISONOUS_POTATO_FRIES))
			.addCriterion("ate_poisonous_potato_chips", ConsumeItemTrigger.TriggerInstance.usedItem(Items.POISONOUS_POTATO_CHIPS))
			.save(consumer);
		potatoAdvancement("bring_home_the_corruption")
			.parent(advancementHolder2)
			.display(Items.CORRUPTED_PEELGRASS_BLOCK.getDefaultInstance(), AdvancementType.TASK, true, true, true)
			.addCriterion("bring_home_the_corruption", PlayerTrigger.TriggerInstance.bringHomeCorruption())
			.save(consumer);
		AdvancementHolder advancementHolder8 = potatoAdvancement("potato_peeler")
			.parent(advancementHolder)
			.display(Items.POTATO_PEELER.getDefaultInstance(), AdvancementType.TASK, true, false, false)
			.addCriterion("potato_peeler", InventoryChangeTrigger.TriggerInstance.hasItems(Items.POTATO_PEELER))
			.save(consumer);
		potatoAdvancement("peel_all_the_things")
			.parent(advancementHolder8)
			.display(Items.POTATO_PEELER.getDefaultInstance(), AdvancementType.CHALLENGE, true, true, true)
			.addCriterion("peel_block", playerTrigger(CriteriaTriggers.PEEL_BLOCK))
			.addCriterion("peel_sheep", playerTrigger(CriteriaTriggers.PEEL_POTATO_SHEEP))
			.addCriterion("peel_armor", playerTrigger(CriteriaTriggers.PEEL_POTATO_ARMOR))
			.save(consumer);
		potatoAdvancement("well_done")
			.parent(advancementHolder)
			.display(Items.CHARCOAL.getDefaultInstance(), AdvancementType.TASK, true, true, false)
			.addCriterion("well_done", RecipeCraftedTrigger.TriggerInstance.craftedItem(new ResourceLocation("overcooked_potatoes")))
			.save(consumer);
	}

	private static PoisonousPotatoAdvancements.ExtendedBuilder potatoAdvancement(String string) {
		return new PoisonousPotatoAdvancements.ExtendedBuilder(string).sendsTelemetryEvent();
	}

	private static Criterion<PlayerTrigger.TriggerInstance> playerTrigger(PlayerTrigger playerTrigger) {
		return playerTrigger.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
	}

	static class ExtendedBuilder extends Advancement.Builder {
		private static final ResourceLocation BACKGROUND = new ResourceLocation("textures/gui/advancements/backgrounds/potato.png");
		private final String name;

		ExtendedBuilder(String string) {
			this.name = string;
		}

		public PoisonousPotatoAdvancements.ExtendedBuilder sendsTelemetryEvent() {
			return (PoisonousPotatoAdvancements.ExtendedBuilder)super.sendsTelemetryEvent();
		}

		public PoisonousPotatoAdvancements.ExtendedBuilder display(ItemStack itemStack, AdvancementType advancementType, boolean bl, boolean bl2, boolean bl3) {
			return (PoisonousPotatoAdvancements.ExtendedBuilder)this.display(
				itemStack,
				Component.translatable("advancements.potato." + this.name + ".title"),
				Component.translatable("advancements.potato." + this.name + ".description"),
				BACKGROUND,
				advancementType,
				bl,
				bl2,
				bl3
			);
		}

		public PoisonousPotatoAdvancements.ExtendedBuilder parent(AdvancementHolder advancementHolder) {
			return (PoisonousPotatoAdvancements.ExtendedBuilder)super.parent(advancementHolder);
		}

		public PoisonousPotatoAdvancements.ExtendedBuilder rewards(AdvancementRewards.Builder builder) {
			return (PoisonousPotatoAdvancements.ExtendedBuilder)super.rewards(builder);
		}

		public PoisonousPotatoAdvancements.ExtendedBuilder rewards(AdvancementRewards advancementRewards) {
			return (PoisonousPotatoAdvancements.ExtendedBuilder)super.rewards(advancementRewards);
		}

		public PoisonousPotatoAdvancements.ExtendedBuilder addCriterion(String string, Criterion<?> criterion) {
			return (PoisonousPotatoAdvancements.ExtendedBuilder)super.addCriterion(string, criterion);
		}

		public PoisonousPotatoAdvancements.ExtendedBuilder requirements(AdvancementRequirements.Strategy strategy) {
			return (PoisonousPotatoAdvancements.ExtendedBuilder)super.requirements(strategy);
		}

		public PoisonousPotatoAdvancements.ExtendedBuilder requirements(AdvancementRequirements advancementRequirements) {
			return (PoisonousPotatoAdvancements.ExtendedBuilder)super.requirements(advancementRequirements);
		}

		public AdvancementHolder save(Consumer<AdvancementHolder> consumer) {
			return this.save(consumer, "potato/" + this.name);
		}
	}
}
