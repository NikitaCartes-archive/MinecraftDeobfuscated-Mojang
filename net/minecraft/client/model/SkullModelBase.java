/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;

@Environment(value=EnvType.CLIENT)
public abstract class SkullModelBase
extends Model {
    public SkullModelBase() {
        super(RenderType::entityTranslucent);
    }

    public abstract void setupAnim(float var1, float var2, float var3);
}

