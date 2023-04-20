package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;

@Environment(EnvType.CLIENT)
public class HorseInventoryScreen extends AbstractContainerScreen<HorseInventoryMenu> {
	private static final ResourceLocation HORSE_INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/horse.png");
	private final AbstractHorse horse;
	private float xMouse;
	private float yMouse;

	public HorseInventoryScreen(HorseInventoryMenu horseInventoryMenu, Inventory inventory, AbstractHorse abstractHorse) {
		super(horseInventoryMenu, inventory, abstractHorse.getDisplayName());
		this.horse = abstractHorse;
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(HORSE_INVENTORY_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
		if (this.horse instanceof AbstractChestedHorse abstractChestedHorse && abstractChestedHorse.hasChest()) {
			guiGraphics.blit(HORSE_INVENTORY_LOCATION, k + 79, l + 17, 0, this.imageHeight, abstractChestedHorse.getInventoryColumns() * 18, 54);
		}

		if (this.horse.isSaddleable()) {
			guiGraphics.blit(HORSE_INVENTORY_LOCATION, k + 7, l + 35 - 18, 18, this.imageHeight + 54, 18, 18);
		}

		if (this.horse.canWearArmor()) {
			if (this.horse instanceof Llama) {
				guiGraphics.blit(HORSE_INVENTORY_LOCATION, k + 7, l + 35, 36, this.imageHeight + 54, 18, 18);
			} else {
				guiGraphics.blit(HORSE_INVENTORY_LOCATION, k + 7, l + 35, 0, this.imageHeight + 54, 18, 18);
			}
		}

		InventoryScreen.renderEntityInInventoryFollowsMouse(
			guiGraphics, k + 51, l + 60, 17, (float)(k + 51) - this.xMouse, (float)(l + 75 - 50) - this.yMouse, this.horse
		);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderBackground(guiGraphics);
		this.xMouse = (float)i;
		this.yMouse = (float)j;
		super.render(guiGraphics, i, j, f);
		this.renderTooltip(guiGraphics, i, j);
	}
}
