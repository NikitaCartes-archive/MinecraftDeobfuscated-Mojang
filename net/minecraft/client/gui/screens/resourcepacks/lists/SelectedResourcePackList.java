/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.resourcepacks.lists;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.resourcepacks.lists.ResourcePackList;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(value=EnvType.CLIENT)
public class SelectedResourcePackList
extends ResourcePackList {
    public SelectedResourcePackList(Minecraft minecraft, int i, int j) {
        super(minecraft, i, j, new TranslatableComponent("resourcePack.selected.title", new Object[0]));
    }
}

