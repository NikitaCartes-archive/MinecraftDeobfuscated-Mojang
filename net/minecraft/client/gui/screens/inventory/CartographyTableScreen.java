/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CartographyTableScreen
extends AbstractContainerScreen<CartographyTableMenu> {
    private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/cartography_table.png");

    public CartographyTableScreen(CartographyTableMenu cartographyTableMenu, Inventory inventory, Component component) {
        super(cartographyTableMenu, inventory, component);
        this.titleLabelY -= 2;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        super.render(poseStack, i, j, f);
        this.renderTooltip(poseStack, i, j);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        MapItemSavedData mapItemSavedData;
        Integer integer;
        this.renderBackground(poseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BG_LOCATION);
        int k = this.leftPos;
        int l = this.topPos;
        this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
        ItemStack itemStack = ((CartographyTableMenu)this.menu).getSlot(1).getItem();
        boolean bl = itemStack.is(Items.MAP);
        boolean bl2 = itemStack.is(Items.PAPER);
        boolean bl3 = itemStack.is(Items.GLASS_PANE);
        ItemStack itemStack2 = ((CartographyTableMenu)this.menu).getSlot(0).getItem();
        boolean bl4 = false;
        if (itemStack2.is(Items.FILLED_MAP)) {
            integer = MapItem.getMapId(itemStack2);
            mapItemSavedData = MapItem.getSavedData(integer, (Level)this.minecraft.level);
            if (mapItemSavedData != null) {
                if (mapItemSavedData.locked) {
                    bl4 = true;
                    if (bl2 || bl3) {
                        this.blit(poseStack, k + 35, l + 31, this.imageWidth + 50, 132, 28, 21);
                    }
                }
                if (bl2 && mapItemSavedData.scale >= 4) {
                    bl4 = true;
                    this.blit(poseStack, k + 35, l + 31, this.imageWidth + 50, 132, 28, 21);
                }
            }
        } else {
            integer = null;
            mapItemSavedData = null;
        }
        this.renderResultingMap(poseStack, integer, mapItemSavedData, bl, bl2, bl3, bl4);
    }

    private void renderResultingMap(PoseStack poseStack, @Nullable Integer integer, @Nullable MapItemSavedData mapItemSavedData, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        int i = this.leftPos;
        int j = this.topPos;
        if (bl2 && !bl4) {
            this.blit(poseStack, i + 67, j + 13, this.imageWidth, 66, 66, 66);
            this.renderMap(poseStack, integer, mapItemSavedData, i + 85, j + 31, 0.226f);
        } else if (bl) {
            this.blit(poseStack, i + 67 + 16, j + 13, this.imageWidth, 132, 50, 66);
            this.renderMap(poseStack, integer, mapItemSavedData, i + 86, j + 16, 0.34f);
            RenderSystem.setShaderTexture(0, BG_LOCATION);
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 1.0f);
            this.blit(poseStack, i + 67, j + 13 + 16, this.imageWidth, 132, 50, 66);
            this.renderMap(poseStack, integer, mapItemSavedData, i + 70, j + 32, 0.34f);
            poseStack.popPose();
        } else if (bl3) {
            this.blit(poseStack, i + 67, j + 13, this.imageWidth, 0, 66, 66);
            this.renderMap(poseStack, integer, mapItemSavedData, i + 71, j + 17, 0.45f);
            RenderSystem.setShaderTexture(0, BG_LOCATION);
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 1.0f);
            this.blit(poseStack, i + 66, j + 12, 0, this.imageHeight, 66, 66);
            poseStack.popPose();
        } else {
            this.blit(poseStack, i + 67, j + 13, this.imageWidth, 0, 66, 66);
            this.renderMap(poseStack, integer, mapItemSavedData, i + 71, j + 17, 0.45f);
        }
    }

    private void renderMap(PoseStack poseStack, @Nullable Integer integer, @Nullable MapItemSavedData mapItemSavedData, int i, int j, float f) {
        if (integer != null && mapItemSavedData != null) {
            poseStack.pushPose();
            poseStack.translate(i, j, 1.0f);
            poseStack.scale(f, f, 1.0f);
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            this.minecraft.gameRenderer.getMapRenderer().render(poseStack, bufferSource, integer, mapItemSavedData, true, 0xF000F0);
            bufferSource.endBatch();
            poseStack.popPose();
        }
    }
}

