/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

@Environment(value=EnvType.CLIENT)
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

    public void render(AbstractContainerMenu abstractContainerMenu, PoseStack poseStack, float f, int i, int j) {
        float g;
        Slot slot = abstractContainerMenu.getSlot(this.slotIndex);
        if (this.icons.isEmpty() || slot.hasItem()) {
            return;
        }
        boolean bl = this.icons.size() > 1 && this.tick >= 30;
        float f2 = g = bl ? this.getIconTransitionTransparency(f) : 1.0f;
        if (g < 1.0f) {
            int k = Math.floorMod(this.iconIndex - 1, this.icons.size());
            this.renderIcon(slot, this.icons.get(k), 1.0f - g, poseStack, i, j);
        }
        this.renderIcon(slot, this.icons.get(this.iconIndex), g, poseStack, i, j);
    }

    private void renderIcon(Slot slot, ResourceLocation resourceLocation, float f, PoseStack poseStack, int i, int j) {
        TextureAtlasSprite textureAtlasSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(resourceLocation);
        RenderSystem.setShaderTexture(0, textureAtlasSprite.atlasLocation());
        GuiComponent.blit(poseStack, i + slot.x, j + slot.y, 0, 16, 16, textureAtlasSprite, 1.0f, 1.0f, 1.0f, f);
    }

    private float getIconTransitionTransparency(float f) {
        float g = (float)(this.tick % 30) + f;
        return Math.min(g, 4.0f) / 4.0f;
    }
}

