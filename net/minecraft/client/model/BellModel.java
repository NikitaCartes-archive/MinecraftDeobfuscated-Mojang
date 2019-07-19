/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

@Environment(value=EnvType.CLIENT)
public class BellModel
extends Model {
    private final ModelPart bellBody;
    private final ModelPart bellBase;

    public BellModel() {
        this.texWidth = 32;
        this.texHeight = 32;
        this.bellBody = new ModelPart(this, 0, 0);
        this.bellBody.addBox(-3.0f, -6.0f, -3.0f, 6, 7, 6);
        this.bellBody.setPos(8.0f, 12.0f, 8.0f);
        this.bellBase = new ModelPart(this, 0, 13);
        this.bellBase.addBox(4.0f, 4.0f, 4.0f, 8, 2, 8);
        this.bellBase.setPos(-8.0f, -12.0f, -8.0f);
        this.bellBody.addChild(this.bellBase);
    }

    public void render(float f, float g, float h) {
        this.bellBody.xRot = f;
        this.bellBody.zRot = g;
        this.bellBody.render(h);
    }
}

