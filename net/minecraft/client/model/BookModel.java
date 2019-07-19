/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class BookModel
extends Model {
    private final ModelPart leftLid = new ModelPart(this).texOffs(0, 0).addBox(-6.0f, -5.0f, 0.0f, 6, 10, 0);
    private final ModelPart rightLid = new ModelPart(this).texOffs(16, 0).addBox(0.0f, -5.0f, 0.0f, 6, 10, 0);
    private final ModelPart leftPages;
    private final ModelPart rightPages;
    private final ModelPart flipPage1;
    private final ModelPart flipPage2;
    private final ModelPart seam = new ModelPart(this).texOffs(12, 0).addBox(-1.0f, -5.0f, 0.0f, 2, 10, 0);

    public BookModel() {
        this.leftPages = new ModelPart(this).texOffs(0, 10).addBox(0.0f, -4.0f, -0.99f, 5, 8, 1);
        this.rightPages = new ModelPart(this).texOffs(12, 10).addBox(0.0f, -4.0f, -0.01f, 5, 8, 1);
        this.flipPage1 = new ModelPart(this).texOffs(24, 10).addBox(0.0f, -4.0f, 0.0f, 5, 8, 0);
        this.flipPage2 = new ModelPart(this).texOffs(24, 10).addBox(0.0f, -4.0f, 0.0f, 5, 8, 0);
        this.leftLid.setPos(0.0f, 0.0f, -1.0f);
        this.rightLid.setPos(0.0f, 0.0f, 1.0f);
        this.seam.yRot = 1.5707964f;
    }

    public void render(float f, float g, float h, float i, float j, float k) {
        this.setupAnim(f, g, h, i, j, k);
        this.leftLid.render(k);
        this.rightLid.render(k);
        this.seam.render(k);
        this.leftPages.render(k);
        this.rightPages.render(k);
        this.flipPage1.render(k);
        this.flipPage2.render(k);
    }

    private void setupAnim(float f, float g, float h, float i, float j, float k) {
        float l = (Mth.sin(f * 0.02f) * 0.1f + 1.25f) * i;
        this.leftLid.yRot = (float)Math.PI + l;
        this.rightLid.yRot = -l;
        this.leftPages.yRot = l;
        this.rightPages.yRot = -l;
        this.flipPage1.yRot = l - l * 2.0f * g;
        this.flipPage2.yRot = l - l * 2.0f * h;
        this.leftPages.x = Mth.sin(l);
        this.rightPages.x = Mth.sin(l);
        this.flipPage1.x = Mth.sin(l);
        this.flipPage2.x = Mth.sin(l);
    }
}

