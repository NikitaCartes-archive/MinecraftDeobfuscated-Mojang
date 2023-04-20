package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

@Environment(EnvType.CLIENT)
public class CyclingSlotBackground {
	private static final int ICON_CHANGE_TICK_RATE = 30;
	private static final int ICON_SIZE = 16;
	private static final int ICON_TRANSITION_TICK_DURATION = 4;
	private final int slotIndex;
	private List<ResourceLocation> icons = List.of();
	private int tick;
	private int iconIndex;

	public CyclingSlotBackground(int i) {
		this.slotIndex = i;
	}

	public void tick(List<ResourceLocation> list) {
		if (!this.icons.equals(list)) {
			this.icons = list;
			this.iconIndex = 0;
		}

		if (!this.icons.isEmpty() && ++this.tick % 30 == 0) {
			this.iconIndex = (this.iconIndex + 1) % this.icons.size();
		}
	}

	public void render(AbstractContainerMenu abstractContainerMenu, GuiGraphics guiGraphics, float f, int i, int j) {
		Slot slot = abstractContainerMenu.getSlot(this.slotIndex);
		if (!this.icons.isEmpty() && !slot.hasItem()) {
			boolean bl = this.icons.size() > 1 && this.tick >= 30;
			float g = bl ? this.getIconTransitionTransparency(f) : 1.0F;
			if (g < 1.0F) {
				int k = Math.floorMod(this.iconIndex - 1, this.icons.size());
				this.renderIcon(slot, (ResourceLocation)this.icons.get(k), 1.0F - g, guiGraphics, i, j);
			}

			this.renderIcon(slot, (ResourceLocation)this.icons.get(this.iconIndex), g, guiGraphics, i, j);
		}
	}

	private void renderIcon(Slot slot, ResourceLocation resourceLocation, float f, GuiGraphics guiGraphics, int i, int j) {
		TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(resourceLocation);
		guiGraphics.blit(i + slot.x, j + slot.y, 0, 16, 16, textureAtlasSprite, 1.0F, 1.0F, 1.0F, f);
	}

	private float getIconTransitionTransparency(float f) {
		float g = (float)(this.tick % 30) + f;
		return Math.min(g, 4.0F) / 4.0F;
	}
}
