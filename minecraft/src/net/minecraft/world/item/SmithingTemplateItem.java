package net.minecraft.world.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.armortrim.TrimPattern;

public class SmithingTemplateItem extends Item {
	private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
	private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
	private static final Component INGREDIENTS_TITLE = Component.translatable(
			Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.ingredients"))
		)
		.withStyle(TITLE_FORMAT);
	private static final Component APPLIES_TO_TITLE = Component.translatable(
			Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.applies_to"))
		)
		.withStyle(TITLE_FORMAT);
	private static final Component NETHERITE_UPGRADE = Component.translatable(
			Util.makeDescriptionId("upgrade", ResourceLocation.withDefaultNamespace("netherite_upgrade"))
		)
		.withStyle(TITLE_FORMAT);
	private static final Component ARMOR_TRIM_APPLIES_TO = Component.translatable(
			Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.armor_trim.applies_to"))
		)
		.withStyle(DESCRIPTION_FORMAT);
	private static final Component ARMOR_TRIM_INGREDIENTS = Component.translatable(
			Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.armor_trim.ingredients"))
		)
		.withStyle(DESCRIPTION_FORMAT);
	private static final Component ARMOR_TRIM_BASE_SLOT_DESCRIPTION = Component.translatable(
		Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.armor_trim.base_slot_description"))
	);
	private static final Component ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
		Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.armor_trim.additions_slot_description"))
	);
	private static final Component NETHERITE_UPGRADE_APPLIES_TO = Component.translatable(
			Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.netherite_upgrade.applies_to"))
		)
		.withStyle(DESCRIPTION_FORMAT);
	private static final Component NETHERITE_UPGRADE_INGREDIENTS = Component.translatable(
			Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.netherite_upgrade.ingredients"))
		)
		.withStyle(DESCRIPTION_FORMAT);
	private static final Component NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(
		Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.netherite_upgrade.base_slot_description"))
	);
	private static final Component NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
		Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.netherite_upgrade.additions_slot_description"))
	);
	private static final ResourceLocation EMPTY_SLOT_HELMET = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet");
	private static final ResourceLocation EMPTY_SLOT_CHESTPLATE = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate");
	private static final ResourceLocation EMPTY_SLOT_LEGGINGS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings");
	private static final ResourceLocation EMPTY_SLOT_BOOTS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots");
	private static final ResourceLocation EMPTY_SLOT_HOE = ResourceLocation.withDefaultNamespace("item/empty_slot_hoe");
	private static final ResourceLocation EMPTY_SLOT_AXE = ResourceLocation.withDefaultNamespace("item/empty_slot_axe");
	private static final ResourceLocation EMPTY_SLOT_SWORD = ResourceLocation.withDefaultNamespace("item/empty_slot_sword");
	private static final ResourceLocation EMPTY_SLOT_SHOVEL = ResourceLocation.withDefaultNamespace("item/empty_slot_shovel");
	private static final ResourceLocation EMPTY_SLOT_PICKAXE = ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe");
	private static final ResourceLocation EMPTY_SLOT_INGOT = ResourceLocation.withDefaultNamespace("item/empty_slot_ingot");
	private static final ResourceLocation EMPTY_SLOT_REDSTONE_DUST = ResourceLocation.withDefaultNamespace("item/empty_slot_redstone_dust");
	private static final ResourceLocation EMPTY_SLOT_QUARTZ = ResourceLocation.withDefaultNamespace("item/empty_slot_quartz");
	private static final ResourceLocation EMPTY_SLOT_EMERALD = ResourceLocation.withDefaultNamespace("item/empty_slot_emerald");
	private static final ResourceLocation EMPTY_SLOT_DIAMOND = ResourceLocation.withDefaultNamespace("item/empty_slot_diamond");
	private static final ResourceLocation EMPTY_SLOT_LAPIS_LAZULI = ResourceLocation.withDefaultNamespace("item/empty_slot_lapis_lazuli");
	private static final ResourceLocation EMPTY_SLOT_AMETHYST_SHARD = ResourceLocation.withDefaultNamespace("item/empty_slot_amethyst_shard");
	private final Component appliesTo;
	private final Component ingredients;
	private final Component upgradeDescription;
	private final Component baseSlotDescription;
	private final Component additionsSlotDescription;
	private final List<ResourceLocation> baseSlotEmptyIcons;
	private final List<ResourceLocation> additionalSlotEmptyIcons;

	public SmithingTemplateItem(
		Component component,
		Component component2,
		Component component3,
		Component component4,
		Component component5,
		List<ResourceLocation> list,
		List<ResourceLocation> list2,
		FeatureFlag... featureFlags
	) {
		super(new Item.Properties().requiredFeatures(featureFlags));
		this.appliesTo = component;
		this.ingredients = component2;
		this.upgradeDescription = component3;
		this.baseSlotDescription = component4;
		this.additionsSlotDescription = component5;
		this.baseSlotEmptyIcons = list;
		this.additionalSlotEmptyIcons = list2;
	}

	public static SmithingTemplateItem createArmorTrimTemplate(ResourceKey<TrimPattern> resourceKey, FeatureFlag... featureFlags) {
		return createArmorTrimTemplate(resourceKey.location(), featureFlags);
	}

	public static SmithingTemplateItem createArmorTrimTemplate(ResourceLocation resourceLocation, FeatureFlag... featureFlags) {
		return new SmithingTemplateItem(
			ARMOR_TRIM_APPLIES_TO,
			ARMOR_TRIM_INGREDIENTS,
			Component.translatable(Util.makeDescriptionId("trim_pattern", resourceLocation)).withStyle(TITLE_FORMAT),
			ARMOR_TRIM_BASE_SLOT_DESCRIPTION,
			ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION,
			createTrimmableArmorIconList(),
			createTrimmableMaterialIconList(),
			featureFlags
		);
	}

	public static SmithingTemplateItem createNetheriteUpgradeTemplate() {
		return new SmithingTemplateItem(
			NETHERITE_UPGRADE_APPLIES_TO,
			NETHERITE_UPGRADE_INGREDIENTS,
			NETHERITE_UPGRADE,
			NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION,
			NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
			createNetheriteUpgradeIconList(),
			createNetheriteUpgradeMaterialList()
		);
	}

	private static List<ResourceLocation> createTrimmableArmorIconList() {
		return List.of(EMPTY_SLOT_HELMET, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_BOOTS);
	}

	private static List<ResourceLocation> createTrimmableMaterialIconList() {
		return List.of(
			EMPTY_SLOT_INGOT, EMPTY_SLOT_REDSTONE_DUST, EMPTY_SLOT_LAPIS_LAZULI, EMPTY_SLOT_QUARTZ, EMPTY_SLOT_DIAMOND, EMPTY_SLOT_EMERALD, EMPTY_SLOT_AMETHYST_SHARD
		);
	}

	private static List<ResourceLocation> createNetheriteUpgradeIconList() {
		return List.of(
			EMPTY_SLOT_HELMET,
			EMPTY_SLOT_SWORD,
			EMPTY_SLOT_CHESTPLATE,
			EMPTY_SLOT_PICKAXE,
			EMPTY_SLOT_LEGGINGS,
			EMPTY_SLOT_AXE,
			EMPTY_SLOT_BOOTS,
			EMPTY_SLOT_HOE,
			EMPTY_SLOT_SHOVEL
		);
	}

	private static List<ResourceLocation> createNetheriteUpgradeMaterialList() {
		return List.of(EMPTY_SLOT_INGOT);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
		list.add(this.upgradeDescription);
		list.add(CommonComponents.EMPTY);
		list.add(APPLIES_TO_TITLE);
		list.add(CommonComponents.space().append(this.appliesTo));
		list.add(INGREDIENTS_TITLE);
		list.add(CommonComponents.space().append(this.ingredients));
	}

	public Component getBaseSlotDescription() {
		return this.baseSlotDescription;
	}

	public Component getAdditionSlotDescription() {
		return this.additionsSlotDescription;
	}

	public List<ResourceLocation> getBaseSlotEmptyIcons() {
		return this.baseSlotEmptyIcons;
	}

	public List<ResourceLocation> getAdditionalSlotEmptyIcons() {
		return this.additionalSlotEmptyIcons;
	}
}
