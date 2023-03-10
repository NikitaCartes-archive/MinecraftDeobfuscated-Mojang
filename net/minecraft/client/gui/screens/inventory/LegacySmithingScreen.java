/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LegacySmithingMenu;

@Deprecated(forRemoval=true)
@Environment(value=EnvType.CLIENT)
public class LegacySmithingScreen
extends ItemCombinerScreen<LegacySmithingMenu> {
    private static final ResourceLocation SMITHING_LOCATION = new ResourceLocation("textures/gui/container/legacy_smithing.png");

    public LegacySmithingScreen(LegacySmithingMenu legacySmithingMenu, Inventory inventory, Component component) {
        super(legacySmithingMenu, inventory, component, SMITHING_LOCATION);
        this.titleLabelX = 60;
        this.titleLabelY = 18;
    }

    @Override
    protected void renderErrorIcon(PoseStack poseStack, int i, int j) {
        if ((((LegacySmithingMenu)this.menu).getSlot(0).hasItem() || ((LegacySmithingMenu)this.menu).getSlot(1).hasItem()) && !((LegacySmithingMenu)this.menu).getSlot(((LegacySmithingMenu)this.menu).getResultSlot()).hasItem()) {
            LegacySmithingScreen.blit(poseStack, i + 99, j + 45, this.imageWidth, 0, 28, 21);
        }
    }
}

