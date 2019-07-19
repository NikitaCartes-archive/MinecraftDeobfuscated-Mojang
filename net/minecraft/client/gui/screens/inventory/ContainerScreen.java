/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

@Environment(value=EnvType.CLIENT)
public class ContainerScreen
extends AbstractContainerScreen<ChestMenu>
implements MenuAccess<ChestMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");
    private final int containerRows;

    public ContainerScreen(ChestMenu chestMenu, Inventory inventory, Component component) {
        super(chestMenu, inventory, component);
        this.passEvents = false;
        int i = 222;
        int j = 114;
        this.containerRows = chestMenu.getRowCount();
        this.imageHeight = 114 + this.containerRows * 18;
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        super.render(i, j, f);
        this.renderTooltip(i, j);
    }

    @Override
    protected void renderLabels(int i, int j) {
        this.font.draw(this.title.getColoredString(), 8.0f, 6.0f, 0x404040);
        this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0f, this.imageHeight - 96 + 2, 0x404040);
    }

    @Override
    protected void renderBg(float f, int i, int j) {
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(CONTAINER_BACKGROUND);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(k, l, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        this.blit(k, l + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }
}

