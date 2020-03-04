/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmithingMenu;

@Environment(value=EnvType.CLIENT)
public class SmithingScreen
extends ItemCombinerScreen<SmithingMenu> {
    private static final ResourceLocation SMITHING_LOCATION = new ResourceLocation("textures/gui/container/smithing.png");

    public SmithingScreen(SmithingMenu smithingMenu, Inventory inventory, Component component) {
        super(smithingMenu, inventory, component, SMITHING_LOCATION);
    }

    @Override
    protected void renderLabels(int i, int j) {
        RenderSystem.disableBlend();
        this.font.draw(this.title.getColoredString(), 40.0f, 20.0f, 0x404040);
    }
}

