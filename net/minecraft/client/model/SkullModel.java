/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

@Environment(value=EnvType.CLIENT)
public class SkullModel
extends Model {
    protected final ModelPart head;

    public SkullModel() {
        this(0, 35, 64, 64);
    }

    public SkullModel(int i, int j, int k, int l) {
        this.texWidth = k;
        this.texHeight = l;
        this.head = new ModelPart(this, i, j);
        this.head.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, 0.0f);
        this.head.setPos(0.0f, 0.0f, 0.0f);
    }

    public void render(float f, float g, float h, float i, float j, float k) {
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.render(k);
    }
}

