/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

@Environment(value=EnvType.CLIENT)
public class ShieldModel
extends Model {
    private final ModelPart plate;
    private final ModelPart handle;

    public ShieldModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        this.plate = new ModelPart(this, 0, 0);
        this.plate.addBox(-6.0f, -11.0f, -2.0f, 12, 22, 1, 0.0f);
        this.handle = new ModelPart(this, 26, 0);
        this.handle.addBox(-1.0f, -3.0f, -1.0f, 2, 6, 6, 0.0f);
    }

    public void render() {
        this.plate.render(0.0625f);
        this.handle.render(0.0625f);
    }
}

