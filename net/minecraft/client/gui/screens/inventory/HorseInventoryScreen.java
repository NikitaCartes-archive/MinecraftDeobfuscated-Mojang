/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;

@Environment(value=EnvType.CLIENT)
public class HorseInventoryScreen
extends AbstractContainerScreen<HorseInventoryMenu> {
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
        this.font.draw(this.title.getColoredString(), 8.0f, 6.0f, 0x404040);
        this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0f, this.imageHeight - 96 + 2, 0x404040);
    }

    @Override
    protected void renderBg(float f, int i, int j) {
        AbstractChestedHorse abstractChestedHorse;
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(HORSE_INVENTORY_LOCATION);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
        if (this.horse instanceof AbstractChestedHorse && (abstractChestedHorse = (AbstractChestedHorse)this.horse).hasChest()) {
            this.blit(k + 79, l + 17, 0, this.imageHeight, abstractChestedHorse.getInventoryColumns() * 18, 54);
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
        this.xMouse = i;
        this.yMouse = j;
        super.render(i, j, f);
        this.renderTooltip(i, j);
    }
}

