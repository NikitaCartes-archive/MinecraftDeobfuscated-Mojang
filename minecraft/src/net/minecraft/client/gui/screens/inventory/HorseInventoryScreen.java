package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;

@Environment(EnvType.CLIENT)
public class HorseInventoryScreen extends AbstractContainerScreen<HorseInventoryMenu> {
	private static final ResourceLocation CHEST_SLOTS_SPRITE = ResourceLocation.withDefaultNamespace("container/horse/chest_slots");
	private static final ResourceLocation SADDLE_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/horse/saddle_slot");
	private static final ResourceLocation LLAMA_ARMOR_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/horse/llama_armor_slot");
	private static final ResourceLocation ARMOR_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/horse/armor_slot");
	private static final ResourceLocation HORSE_INVENTORY_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/horse.png");
	private final AbstractHorse horse;
	private final int inventoryColumns;
	private float xMouse;
	private float yMouse;

	public HorseInventoryScreen(HorseInventoryMenu horseInventoryMenu, Inventory inventory, AbstractHorse abstractHorse, int i) {
		super(horseInventoryMenu, inventory, abstractHorse.getDisplayName());
		this.horse = abstractHorse;
		this.inventoryColumns = i;
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(RenderType::guiTextured, HORSE_INVENTORY_LOCATION, k, l, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
		if (this.inventoryColumns > 0) {
			guiGraphics.blitSprite(RenderType::guiTextured, CHEST_SLOTS_SPRITE, 90, 54, 0, 0, k + 79, l + 17, this.inventoryColumns * 18, 54);
		}

		if (this.horse.isSaddleable()) {
			guiGraphics.blitSprite(RenderType::guiTextured, SADDLE_SLOT_SPRITE, k + 7, l + 35 - 18, 18, 18);
		}

		if (this.horse.canUseSlot(EquipmentSlot.BODY)) {
			if (this.horse instanceof Llama) {
				guiGraphics.blitSprite(RenderType::guiTextured, LLAMA_ARMOR_SLOT_SPRITE, k + 7, l + 35, 18, 18);
			} else {
				guiGraphics.blitSprite(RenderType::guiTextured, ARMOR_SLOT_SPRITE, k + 7, l + 35, 18, 18);
			}
		}

		InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, k + 26, l + 18, k + 78, l + 70, 17, 0.25F, this.xMouse, this.yMouse, this.horse);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		this.xMouse = (float)i;
		this.yMouse = (float)j;
		super.render(guiGraphics, i, j, f);
		this.renderTooltip(guiGraphics, i, j);
	}
}
