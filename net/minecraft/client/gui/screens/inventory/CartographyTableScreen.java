/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CartographyTableScreen
extends AbstractContainerScreen<CartographyTableMenu> {
    private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/cartography_table.png");

    public CartographyTableScreen(CartographyTableMenu cartographyTableMenu, Inventory inventory, Component component) {
        super(cartographyTableMenu, inventory, component);
    }

    @Override
    public void render(int i, int j, float f) {
        super.render(i, j, f);
        this.renderTooltip(i, j);
    }

    @Override
    protected void renderLabels(int i, int j) {
        this.font.draw(this.title.getColoredString(), 8.0f, 4.0f, 0x404040);
        this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0f, this.imageHeight - 96 + 2, 0x404040);
    }

    @Override
    protected void renderBg(float f, int i, int j) {
        MapItemSavedData mapItemSavedData;
        this.renderBackground();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(BG_LOCATION);
        int k = this.leftPos;
        int l = this.topPos;
        this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
        Item item = ((CartographyTableMenu)this.menu).getSlot(1).getItem().getItem();
        boolean bl = item == Items.MAP;
        boolean bl2 = item == Items.PAPER;
        boolean bl3 = item == Items.GLASS_PANE;
        ItemStack itemStack = ((CartographyTableMenu)this.menu).getSlot(0).getItem();
        boolean bl4 = false;
        if (itemStack.getItem() == Items.FILLED_MAP) {
            mapItemSavedData = MapItem.getSavedData(itemStack, this.minecraft.level);
            if (mapItemSavedData != null) {
                if (mapItemSavedData.locked) {
                    bl4 = true;
                    if (bl2 || bl3) {
                        this.blit(k + 35, l + 31, this.imageWidth + 50, 132, 28, 21);
                    }
                }
                if (bl2 && mapItemSavedData.scale >= 4) {
                    bl4 = true;
                    this.blit(k + 35, l + 31, this.imageWidth + 50, 132, 28, 21);
                }
            }
        } else {
            mapItemSavedData = null;
        }
        this.renderResultingMap(mapItemSavedData, bl, bl2, bl3, bl4);
    }

    private void renderResultingMap(@Nullable MapItemSavedData mapItemSavedData, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        int i = this.leftPos;
        int j = this.topPos;
        if (bl2 && !bl4) {
            this.blit(i + 67, j + 13, this.imageWidth, 66, 66, 66);
            this.renderMap(mapItemSavedData, i + 85, j + 31, 0.226f);
        } else if (bl) {
            this.blit(i + 67 + 16, j + 13, this.imageWidth, 132, 50, 66);
            this.renderMap(mapItemSavedData, i + 86, j + 16, 0.34f);
            this.minecraft.getTextureManager().bind(BG_LOCATION);
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0f, 0.0f, 1.0f);
            this.blit(i + 67, j + 13 + 16, this.imageWidth, 132, 50, 66);
            this.renderMap(mapItemSavedData, i + 70, j + 32, 0.34f);
            RenderSystem.popMatrix();
        } else if (bl3) {
            this.blit(i + 67, j + 13, this.imageWidth, 0, 66, 66);
            this.renderMap(mapItemSavedData, i + 71, j + 17, 0.45f);
            this.minecraft.getTextureManager().bind(BG_LOCATION);
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0f, 0.0f, 1.0f);
            this.blit(i + 66, j + 12, 0, this.imageHeight, 66, 66);
            RenderSystem.popMatrix();
        } else {
            this.blit(i + 67, j + 13, this.imageWidth, 0, 66, 66);
            this.renderMap(mapItemSavedData, i + 71, j + 17, 0.45f);
        }
    }

    private void renderMap(@Nullable MapItemSavedData mapItemSavedData, int i, int j, float f) {
        if (mapItemSavedData != null) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef(i, j, 1.0f);
            RenderSystem.scalef(f, f, 1.0f);
            this.minecraft.gameRenderer.getMapRenderer().render(mapItemSavedData, true);
            RenderSystem.popMatrix();
        }
    }
}

