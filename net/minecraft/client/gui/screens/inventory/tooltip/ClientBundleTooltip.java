/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class ClientBundleTooltip
implements ClientTooltipComponent {
    private final NonNullList<ItemStack> items;
    private final boolean showExtensionSlot;

    public ClientBundleTooltip(BundleTooltip bundleTooltip) {
        this.items = bundleTooltip.getItems();
        this.showExtensionSlot = bundleTooltip.showEmptySlot();
    }

    @Override
    public int getHeight() {
        return 18 * (1 + (this.getSlotCount() - 1) / this.itemsPerRow()) + 4;
    }

    @Override
    public int getWidth(Font font) {
        return this.itemsPerRow() * 18;
    }

    private int getSlotCount() {
        return this.items.size() + (this.showExtensionSlot ? 1 : 0);
    }

    @Override
    public void renderImage(Font font, int i, int j, PoseStack poseStack, ItemRenderer itemRenderer, int k, TextureManager textureManager) {
        int l = 0;
        int m = 0;
        int n = this.itemsPerRow();
        for (ItemStack itemStack : this.items) {
            this.blitSlotBg(poseStack, l + i - 1, m + j - 1, k, textureManager, false);
            itemRenderer.renderAndDecorateItem(itemStack, i + l, j + m);
            itemRenderer.renderGuiItemDecorations(font, itemStack, i + l, j + m);
            if ((l += 18) < 18 * n) continue;
            l = 0;
            m += 18;
        }
        if (this.showExtensionSlot) {
            this.blitSlotBg(poseStack, l + i - 1, m + j - 1, k, textureManager, true);
        }
    }

    private void blitSlotBg(PoseStack poseStack, int i, int j, int k, TextureManager textureManager, boolean bl) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        textureManager.bind(GuiComponent.STATS_ICON_LOCATION);
        GuiComponent.blit(poseStack, i, j, k, 0.0f, bl ? 36.0f : 0.0f, 18, 18, 128, 128);
    }

    private int itemsPerRow() {
        return Mth.ceil(Math.sqrt(this.getSlotCount()));
    }
}

