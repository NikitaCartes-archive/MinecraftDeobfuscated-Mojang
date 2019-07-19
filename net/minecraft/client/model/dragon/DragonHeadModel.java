/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model.dragon;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelPart;

@Environment(value=EnvType.CLIENT)
public class DragonHeadModel
extends SkullModel {
    private final ModelPart head;
    private final ModelPart jaw;

    public DragonHeadModel(float f) {
        this.texWidth = 256;
        this.texHeight = 256;
        float g = -16.0f;
        this.head = new ModelPart(this, "head");
        this.head.addBox("upperlip", -6.0f, -1.0f, -24.0f, 12, 5, 16, f, 176, 44);
        this.head.addBox("upperhead", -8.0f, -8.0f, -10.0f, 16, 16, 16, f, 112, 30);
        this.head.mirror = true;
        this.head.addBox("scale", -5.0f, -12.0f, -4.0f, 2, 4, 6, f, 0, 0);
        this.head.addBox("nostril", -5.0f, -3.0f, -22.0f, 2, 2, 4, f, 112, 0);
        this.head.mirror = false;
        this.head.addBox("scale", 3.0f, -12.0f, -4.0f, 2, 4, 6, f, 0, 0);
        this.head.addBox("nostril", 3.0f, -3.0f, -22.0f, 2, 2, 4, f, 112, 0);
        this.jaw = new ModelPart(this, "jaw");
        this.jaw.setPos(0.0f, 4.0f, -8.0f);
        this.jaw.addBox("jaw", -6.0f, 0.0f, -16.0f, 12, 4, 16, f, 176, 65);
        this.head.addChild(this.jaw);
    }

    @Override
    public void render(float f, float g, float h, float i, float j, float k) {
        this.jaw.xRot = (float)(Math.sin(f * (float)Math.PI * 0.2f) + 1.0) * 0.2f;
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        GlStateManager.translatef(0.0f, -0.374375f, 0.0f);
        GlStateManager.scalef(0.75f, 0.75f, 0.75f);
        this.head.render(k);
    }
}

