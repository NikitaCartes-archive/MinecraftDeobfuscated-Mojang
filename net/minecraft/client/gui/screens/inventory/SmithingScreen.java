/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public class SmithingScreen
extends ItemCombinerScreen<SmithingMenu> {
    private static final ResourceLocation SMITHING_LOCATION = new ResourceLocation("textures/gui/container/smithing.png");
    private static final ResourceLocation EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM = new ResourceLocation("item/empty_slot_smithing_template_armor_trim");
    private static final ResourceLocation EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE = new ResourceLocation("item/empty_slot_smithing_template_netherite_upgrade");
    private static final Component MISSING_TEMPLATE_TOOLTIP = Component.translatable("container.upgrade.missing_template_tooltip");
    private static final Component ERROR_TOOLTIP = Component.translatable("container.upgrade.error_tooltip");
    private static final List<ResourceLocation> EMPTY_SLOT_SMITHING_TEMPLATES = List.of(EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM, EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE);
    private static final int TITLE_LABEL_X = 44;
    private static final int TITLE_LABEL_Y = 15;
    private static final int ERROR_ICON_WIDTH = 28;
    private static final int ERROR_ICON_HEIGHT = 21;
    private static final int ERROR_ICON_X = 65;
    private static final int ERROR_ICON_Y = 46;
    private static final int TOOLTIP_WIDTH = 115;
    public static final int ARMOR_STAND_Y_ROT = 210;
    public static final int ARMOR_STAND_X_ROT = 25;
    public static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232f, 0.0f, (float)Math.PI);
    public static final int ARMOR_STAND_SCALE = 25;
    public static final int ARMOR_STAND_OFFSET_Y = 75;
    public static final int ARMOR_STAND_OFFSET_X = 141;
    private final CyclingSlotBackground templateIcon = new CyclingSlotBackground(0);
    private final CyclingSlotBackground baseIcon = new CyclingSlotBackground(1);
    private final CyclingSlotBackground additionalIcon = new CyclingSlotBackground(2);
    @Nullable
    private ArmorStand armorStandPreview;

    public SmithingScreen(SmithingMenu smithingMenu, Inventory inventory, Component component) {
        super(smithingMenu, inventory, component, SMITHING_LOCATION);
        this.titleLabelX = 44;
        this.titleLabelY = 15;
    }

    @Override
    protected void subInit() {
        this.armorStandPreview = new ArmorStand(this.minecraft.level, 0.0, 0.0, 0.0);
        this.armorStandPreview.setNoBasePlate(true);
        this.armorStandPreview.setShowArms(true);
        this.armorStandPreview.yBodyRot = 210.0f;
        this.armorStandPreview.setXRot(25.0f);
        this.armorStandPreview.yHeadRot = this.armorStandPreview.getYRot();
        this.armorStandPreview.yHeadRotO = this.armorStandPreview.getYRot();
        this.updateArmorStandPreview(((SmithingMenu)this.menu).getSlot(3).getItem());
    }

    @Override
    public void containerTick() {
        super.containerTick();
        Optional<SmithingTemplateItem> optional = this.getTemplateItem();
        this.templateIcon.tick(EMPTY_SLOT_SMITHING_TEMPLATES);
        this.baseIcon.tick(optional.map(SmithingTemplateItem::getBaseSlotEmptyIcons).orElse(List.of()));
        this.additionalIcon.tick(optional.map(SmithingTemplateItem::getAdditionalSlotEmptyIcons).orElse(List.of()));
    }

    private Optional<SmithingTemplateItem> getTemplateItem() {
        Item item;
        ItemStack itemStack = ((SmithingMenu)this.menu).getSlot(0).getItem();
        if (!itemStack.isEmpty() && (item = itemStack.getItem()) instanceof SmithingTemplateItem) {
            SmithingTemplateItem smithingTemplateItem = (SmithingTemplateItem)item;
            return Optional.of(smithingTemplateItem);
        }
        return Optional.empty();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        super.render(poseStack, i, j, f);
        this.renderOnboardingTooltips(poseStack, i, j);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        super.renderBg(poseStack, f, i, j);
        this.templateIcon.render(this.menu, poseStack, f, this.leftPos, this.topPos);
        this.baseIcon.render(this.menu, poseStack, f, this.leftPos, this.topPos);
        this.additionalIcon.render(this.menu, poseStack, f, this.leftPos, this.topPos);
        InventoryScreen.renderEntityInInventory(poseStack, this.leftPos + 141, this.topPos + 75, 25, ARMOR_STAND_ANGLE, null, this.armorStandPreview);
    }

    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
        if (i == 3) {
            this.updateArmorStandPreview(itemStack);
        }
    }

    private void updateArmorStandPreview(ItemStack itemStack) {
        if (this.armorStandPreview == null) {
            return;
        }
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            this.armorStandPreview.setItemSlot(equipmentSlot, ItemStack.EMPTY);
        }
        if (!itemStack.isEmpty()) {
            ItemStack itemStack2 = itemStack.copy();
            Item item = itemStack.getItem();
            if (item instanceof ArmorItem) {
                ArmorItem armorItem = (ArmorItem)item;
                this.armorStandPreview.setItemSlot(armorItem.getEquipmentSlot(), itemStack2);
            } else {
                this.armorStandPreview.setItemSlot(EquipmentSlot.OFFHAND, itemStack2);
            }
        }
    }

    @Override
    protected void renderErrorIcon(PoseStack poseStack, int i, int j) {
        if (this.hasRecipeError()) {
            SmithingScreen.blit(poseStack, i + 65, j + 46, this.imageWidth, 0, 28, 21);
        }
    }

    private void renderOnboardingTooltips(PoseStack poseStack, int i, int j) {
        Optional<Component> optional = Optional.empty();
        if (this.hasRecipeError() && this.isHovering(65, 46, 28, 21, i, j)) {
            optional = Optional.of(ERROR_TOOLTIP);
        }
        if (this.hoveredSlot != null) {
            ItemStack itemStack = ((SmithingMenu)this.menu).getSlot(0).getItem();
            ItemStack itemStack2 = this.hoveredSlot.getItem();
            if (itemStack.isEmpty()) {
                if (this.hoveredSlot.index == 0) {
                    optional = Optional.of(MISSING_TEMPLATE_TOOLTIP);
                }
            } else {
                Item item = itemStack.getItem();
                if (item instanceof SmithingTemplateItem) {
                    SmithingTemplateItem smithingTemplateItem = (SmithingTemplateItem)item;
                    if (itemStack2.isEmpty()) {
                        if (this.hoveredSlot.index == 1) {
                            optional = Optional.of(smithingTemplateItem.getBaseSlotDescription());
                        } else if (this.hoveredSlot.index == 2) {
                            optional = Optional.of(smithingTemplateItem.getAdditionSlotDescription());
                        }
                    }
                }
            }
        }
        optional.ifPresent(component -> this.renderTooltip(poseStack, this.font.split((FormattedText)component, 115), i, j));
    }

    private boolean hasRecipeError() {
        return ((SmithingMenu)this.menu).getSlot(0).hasItem() && ((SmithingMenu)this.menu).getSlot(1).hasItem() && ((SmithingMenu)this.menu).getSlot(2).hasItem() && !((SmithingMenu)this.menu).getSlot(((SmithingMenu)this.menu).getResultSlot()).hasItem();
    }
}

