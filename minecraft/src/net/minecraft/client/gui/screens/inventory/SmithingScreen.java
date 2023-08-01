package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class SmithingScreen extends ItemCombinerScreen<SmithingMenu> {
	private static final ResourceLocation ERROR_SPRITE = new ResourceLocation("container/smithing/error");
	private static final ResourceLocation EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM = new ResourceLocation("item/empty_slot_smithing_template_armor_trim");
	private static final ResourceLocation EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE = new ResourceLocation(
		"item/empty_slot_smithing_template_netherite_upgrade"
	);
	private static final Component MISSING_TEMPLATE_TOOLTIP = Component.translatable("container.upgrade.missing_template_tooltip");
	private static final Component ERROR_TOOLTIP = Component.translatable("container.upgrade.error_tooltip");
	private static final List<ResourceLocation> EMPTY_SLOT_SMITHING_TEMPLATES = List.of(
		EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM, EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE
	);
	private static final int TITLE_LABEL_X = 44;
	private static final int TITLE_LABEL_Y = 15;
	private static final int ERROR_ICON_WIDTH = 28;
	private static final int ERROR_ICON_HEIGHT = 21;
	private static final int ERROR_ICON_X = 65;
	private static final int ERROR_ICON_Y = 46;
	private static final int TOOLTIP_WIDTH = 115;
	private static final int ARMOR_STAND_Y_ROT = 210;
	private static final int ARMOR_STAND_X_ROT = 25;
	private static final Vector3f ARMOR_STAND_TRANSLATION = new Vector3f();
	private static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, 0.0F, (float) Math.PI);
	private static final int ARMOR_STAND_SCALE = 25;
	private static final int ARMOR_STAND_OFFSET_Y = 75;
	private static final int ARMOR_STAND_OFFSET_X = 141;
	private final CyclingSlotBackground templateIcon = new CyclingSlotBackground(0);
	private final CyclingSlotBackground baseIcon = new CyclingSlotBackground(1);
	private final CyclingSlotBackground additionalIcon = new CyclingSlotBackground(2);
	@Nullable
	private ArmorStand armorStandPreview;

	public SmithingScreen(SmithingMenu smithingMenu, Inventory inventory, Component component) {
		super(smithingMenu, inventory, component, new ResourceLocation("textures/gui/container/smithing.png"));
		this.titleLabelX = 44;
		this.titleLabelY = 15;
	}

	@Override
	protected void subInit() {
		this.armorStandPreview = new ArmorStand(this.minecraft.level, 0.0, 0.0, 0.0);
		this.armorStandPreview.setNoBasePlate(true);
		this.armorStandPreview.setShowArms(true);
		this.armorStandPreview.yBodyRot = 210.0F;
		this.armorStandPreview.setXRot(25.0F);
		this.armorStandPreview.yHeadRot = this.armorStandPreview.getYRot();
		this.armorStandPreview.yHeadRotO = this.armorStandPreview.getYRot();
		this.updateArmorStandPreview(this.menu.getSlot(3).getItem());
	}

	@Override
	public void containerTick() {
		super.containerTick();
		Optional<SmithingTemplateItem> optional = this.getTemplateItem();
		this.templateIcon.tick(EMPTY_SLOT_SMITHING_TEMPLATES);
		this.baseIcon.tick((List<ResourceLocation>)optional.map(SmithingTemplateItem::getBaseSlotEmptyIcons).orElse(List.of()));
		this.additionalIcon.tick((List<ResourceLocation>)optional.map(SmithingTemplateItem::getAdditionalSlotEmptyIcons).orElse(List.of()));
	}

	private Optional<SmithingTemplateItem> getTemplateItem() {
		ItemStack itemStack = this.menu.getSlot(0).getItem();
		return !itemStack.isEmpty() && itemStack.getItem() instanceof SmithingTemplateItem smithingTemplateItem
			? Optional.of(smithingTemplateItem)
			: Optional.empty();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.renderOnboardingTooltips(guiGraphics, i, j);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		super.renderBg(guiGraphics, f, i, j);
		this.templateIcon.render(this.menu, guiGraphics, f, this.leftPos, this.topPos);
		this.baseIcon.render(this.menu, guiGraphics, f, this.leftPos, this.topPos);
		this.additionalIcon.render(this.menu, guiGraphics, f, this.leftPos, this.topPos);
		InventoryScreen.renderEntityInInventory(
			guiGraphics, (float)(this.leftPos + 141), (float)(this.topPos + 75), 25, ARMOR_STAND_TRANSLATION, ARMOR_STAND_ANGLE, null, this.armorStandPreview
		);
	}

	@Override
	public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
		if (i == 3) {
			this.updateArmorStandPreview(itemStack);
		}
	}

	private void updateArmorStandPreview(ItemStack itemStack) {
		if (this.armorStandPreview != null) {
			for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
				this.armorStandPreview.setItemSlot(equipmentSlot, ItemStack.EMPTY);
			}

			if (!itemStack.isEmpty()) {
				ItemStack itemStack2 = itemStack.copy();
				if (itemStack.getItem() instanceof ArmorItem armorItem) {
					this.armorStandPreview.setItemSlot(armorItem.getEquipmentSlot(), itemStack2);
				} else {
					this.armorStandPreview.setItemSlot(EquipmentSlot.OFFHAND, itemStack2);
				}
			}
		}
	}

	@Override
	protected void renderErrorIcon(GuiGraphics guiGraphics, int i, int j) {
		if (this.hasRecipeError()) {
			guiGraphics.blitSprite(ERROR_SPRITE, i + 65, j + 46, 28, 21);
		}
	}

	private void renderOnboardingTooltips(GuiGraphics guiGraphics, int i, int j) {
		Optional<Component> optional = Optional.empty();
		if (this.hasRecipeError() && this.isHovering(65, 46, 28, 21, (double)i, (double)j)) {
			optional = Optional.of(ERROR_TOOLTIP);
		}

		if (this.hoveredSlot != null) {
			ItemStack itemStack = this.menu.getSlot(0).getItem();
			ItemStack itemStack2 = this.hoveredSlot.getItem();
			if (itemStack.isEmpty()) {
				if (this.hoveredSlot.index == 0) {
					optional = Optional.of(MISSING_TEMPLATE_TOOLTIP);
				}
			} else if (itemStack.getItem() instanceof SmithingTemplateItem smithingTemplateItem && itemStack2.isEmpty()) {
				if (this.hoveredSlot.index == 1) {
					optional = Optional.of(smithingTemplateItem.getBaseSlotDescription());
				} else if (this.hoveredSlot.index == 2) {
					optional = Optional.of(smithingTemplateItem.getAdditionSlotDescription());
				}
			}
		}

		optional.ifPresent(component -> guiGraphics.renderTooltip(this.font, this.font.split(component, 115), i, j));
	}

	private boolean hasRecipeError() {
		return this.menu.getSlot(0).hasItem()
			&& this.menu.getSlot(1).hasItem()
			&& this.menu.getSlot(2).hasItem()
			&& !this.menu.getSlot(this.menu.getResultSlot()).hasItem();
	}
}
