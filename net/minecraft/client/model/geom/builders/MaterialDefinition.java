/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model.geom.builders;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class MaterialDefinition {
    final int xTexSize;
    final int yTexSize;

    public MaterialDefinition(int i, int j) {
        this.xTexSize = i;
        this.yTexSize = j;
    }
}

