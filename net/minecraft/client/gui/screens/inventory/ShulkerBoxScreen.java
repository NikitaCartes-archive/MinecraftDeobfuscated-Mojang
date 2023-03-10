/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ShulkerBoxMenu;

@Environment(value=EnvType.CLIENT)
public class ShulkerBoxScreen
extends AbstractContainerScreen<ShulkerBoxMenu> {
    private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");

    public ShulkerBoxScreen(ShulkerBoxMenu shulkerBoxMenu, Inventory inventory, Component component) {
        super(shulkerBoxMenu, inventory, component);
        ++this.imageHeight;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        super.render(poseStack, i, j, f);
        this.renderTooltip(poseStack, i, j);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        RenderSystem.setShaderTexture(0, CONTAINER_TEXTURE);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        ShulkerBoxScreen.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
    }
}

