/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

@Environment(value=EnvType.CLIENT)
public class BannerModel
extends Model {
    private final ModelPart flag;
    private final ModelPart pole;
    private final ModelPart bar;

    public BannerModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        this.flag = new ModelPart(this, 0, 0);
        this.flag.addBox(-10.0f, 0.0f, -2.0f, 20, 40, 1, 0.0f);
        this.pole = new ModelPart(this, 44, 0);
        this.pole.addBox(-1.0f, -30.0f, -1.0f, 2, 42, 2, 0.0f);
        this.bar = new ModelPart(this, 0, 42);
        this.bar.addBox(-10.0f, -32.0f, -1.0f, 20, 2, 2, 0.0f);
    }

    public void render() {
        this.flag.y = -32.0f;
        this.flag.render(0.0625f);
        this.pole.render(0.0625f);
        this.bar.render(0.0625f);
    }

    public ModelPart getPole() {
        return this.pole;
    }

    public ModelPart getFlag() {
        return this.flag;
    }
}

