package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
		this.passEvents = false;
	}

	@Override
	protected void renderLabels(int i, int j) {
		this.font.draw(this.title.getColoredString(), 8.0F, 6.0F, 4210752);
		this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
	}

	@Override
	protected void renderBg(float f, int i, int j) {
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(HORSE_INVENTORY_LOCATION);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
		if (this.horse instanceof AbstractChestedHorse) {
			AbstractChestedHorse abstractChestedHorse = (AbstractChestedHorse)this.horse;
			if (abstractChestedHorse.hasChest()) {
				this.blit(k + 79, l + 17, 0, this.imageHeight, abstractChestedHorse.getInventoryColumns() * 18, 54);
			}
		}

		if (this.horse.canBeSaddled()) {
			this.blit(k + 7, l + 35 - 18, 18, this.imageHeight + 54, 18, 18);
		}

		if (this.horse.wearsArmor()) {
			if (this.horse instanceof Llama) {
				this.blit(k + 7, l + 35, 36, this.imageHeight + 54, 18, 18);
			} else {
				this.blit(k + 7, l + 35, 0, this.imageHeight + 54, 18, 18);
			}
		}

		InventoryScreen.renderPlayerModel(k + 51, l + 60, 17, (float)(k + 51) - this.xMouse, (float)(l + 75 - 50) - this.yMouse, this.horse);
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.xMouse = (float)i;
		this.yMouse = (float)j;
		super.render(i, j, f);
		this.renderTooltip(i, j);
	}
}
