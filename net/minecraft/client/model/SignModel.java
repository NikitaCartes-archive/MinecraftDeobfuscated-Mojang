/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

@Environment(value=EnvType.CLIENT)
public class SignModel
extends Model {
    private final ModelPart sign = new ModelPart(this, 0, 0);
    private final ModelPart stick;

    public SignModel() {
        this.sign.addBox(-12.0f, -14.0f, -1.0f, 24, 12, 2, 0.0f);
        this.stick = new ModelPart(this, 0, 14);
        this.stick.addBox(-1.0f, -2.0f, -1.0f, 2, 14, 2, 0.0f);
    }

    public void render() {
        this.sign.render(0.0625f);
        this.stick.render(0.0625f);
    }

    public ModelPart getStick() {
        return this.stick;
    }
}

