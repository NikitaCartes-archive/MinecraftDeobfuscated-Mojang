/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class ItemCombinerScreen<T extends ItemCombinerMenu>
extends AbstractContainerScreen<T>
implements ContainerListener {
    private ResourceLocation menuResource;

    public ItemCombinerScreen(T itemCombinerMenu, Inventory inventory, Component component, ResourceLocation resourceLocation) {
        super(itemCombinerMenu, inventory, component);
        this.menuResource = resourceLocation;
    }

    protected void subInit() {
    }

    @Override
    protected void init() {
        super.init();
        this.subInit();
        ((ItemCombinerMenu)this.menu).addSlotListener(this);
    }

    @Override
    public void removed() {
        super.removed();
        ((ItemCombinerMenu)this.menu).removeSlotListener(this);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        super.render(i, j, f);
        RenderSystem.disableBlend();
        this.renderFg(i, j, f);
        this.renderTooltip(i, j);
    }

    protected void renderFg(int i, int j, float f) {
    }

    @Override
    protected void renderBg(float f, int i, int j) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(this.menuResource);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
        this.blit(k + 59, l + 20, 0, this.imageHeight + (((ItemCombinerMenu)this.menu).getSlot(0).hasItem() ? 0 : 16), 110, 16);
        if ((((ItemCombinerMenu)this.menu).getSlot(0).hasItem() || ((ItemCombinerMenu)this.menu).getSlot(1).hasItem()) && !((ItemCombinerMenu)this.menu).getSlot(2).hasItem()) {
            this.blit(k + 99, l + 45, this.imageWidth, 0, 28, 21);
        }
    }

    @Override
    public void refreshContainer(AbstractContainerMenu abstractContainerMenu, NonNullList<ItemStack> nonNullList) {
        this.slotChanged(abstractContainerMenu, 0, abstractContainerMenu.getSlot(0).getItem());
    }

    @Override
    public void setContainerData(AbstractContainerMenu abstractContainerMenu, int i, int j) {
    }

    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
    }
}

