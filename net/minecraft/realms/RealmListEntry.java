/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.ObjectSelectionList;

@Environment(value=EnvType.CLIENT)
public abstract class RealmListEntry
extends ObjectSelectionList.Entry<RealmListEntry> {
    @Override
    public abstract void render(int var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8, float var9);

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return false;
    }
}

