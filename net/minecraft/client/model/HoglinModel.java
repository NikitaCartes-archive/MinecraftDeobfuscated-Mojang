/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Hoglin;

@Environment(value=EnvType.CLIENT)
public class HoglinModel
extends ListModel<Hoglin> {
    private final ModelPart head;
    private final ModelPart ear0;
    private final ModelPart ear1;
    private final ModelPart body;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;

    public HoglinModel() {
        this.texWidth = 128;
        this.texHeight = 128;
        this.body = new ModelPart(this);
        this.body.setPos(0.0f, 7.0f, 0.0f);
        this.body.texOffs(1, 1).addBox(-8.0f, -7.0f, -13.0f, 16.0f, 14.0f, 26.0f);
        ModelPart modelPart = new ModelPart(this);
        modelPart.setPos(0.0f, -14.0f, -7.0f);
        modelPart.texOffs(5, 67).addBox(0.0f, 0.0f, -9.0f, 0.0f, 10.0f, 19.0f, 0.001f);
        this.body.addChild(modelPart);
        this.head = new ModelPart(this);
        this.head.setPos(0.0f, 2.0f, -12.0f);
        this.head.texOffs(1, 42).addBox(-7.0f, -3.0f, -19.0f, 14.0f, 6.0f, 19.0f);
        this.ear0 = new ModelPart(this);
        this.ear0.setPos(-6.0f, -2.0f, -3.0f);
        this.ear0.texOffs(4, 16).addBox(-6.0f, -1.0f, -2.0f, 6.0f, 1.0f, 4.0f);
        this.ear0.zRot = -0.6981317f;
        this.head.addChild(this.ear0);
        this.ear1 = new ModelPart(this);
        this.ear1.setPos(6.0f, -2.0f, -3.0f);
        this.ear1.texOffs(4, 21).addBox(0.0f, -1.0f, -2.0f, 6.0f, 1.0f, 4.0f);
        this.ear1.zRot = 0.6981317f;
        this.head.addChild(this.ear1);
        ModelPart modelPart2 = new ModelPart(this);
        modelPart2.setPos(-7.0f, 2.0f, -12.0f);
        modelPart2.texOffs(6, 45).addBox(-1.0f, -11.0f, -1.0f, 2.0f, 11.0f, 2.0f);
        this.head.addChild(modelPart2);
        ModelPart modelPart3 = new ModelPart(this);
        modelPart3.setPos(7.0f, 2.0f, -12.0f);
        modelPart3.texOffs(6, 45).addBox(-1.0f, -11.0f, -1.0f, 2.0f, 11.0f, 2.0f);
        this.head.addChild(modelPart3);
        this.head.xRot = 0.87266463f;
        int i = 14;
        int j = 11;
        this.leg0 = new ModelPart(this);
        this.leg0.setPos(-4.0f, 10.0f, -8.5f);
        this.leg0.texOffs(46, 75).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 14.0f, 6.0f);
        this.leg1 = new ModelPart(this);
        this.leg1.setPos(4.0f, 10.0f, -8.5f);
        this.leg1.texOffs(71, 75).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 14.0f, 6.0f);
        this.leg2 = new ModelPart(this);
        this.leg2.setPos(-5.0f, 13.0f, 10.0f);
        this.leg2.texOffs(51, 43).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 11.0f, 5.0f);
        this.leg3 = new ModelPart(this);
        this.leg3.setPos(5.0f, 13.0f, 10.0f);
        this.leg3.texOffs(72, 43).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 11.0f, 5.0f);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.body, this.head, this.leg0, this.leg1, this.leg2, this.leg3);
    }

    @Override
    public void setupAnim(Hoglin hoglin, float f, float g, float h, float i, float j) {
        this.ear0.zRot = -0.6981317f - g * 2.5f * Mth.sin(f * 3.0f);
        this.ear1.zRot = 0.6981317f + g * 2.5f * Mth.sin(f * 3.0f);
        this.head.yRot = i * ((float)Math.PI / 180);
        this.leg0.xRot = Mth.cos(f * 1.5f) * 4.4f * g;
        this.leg1.xRot = Mth.cos(f * 1.5f + (float)Math.PI) * 4.4f * g;
        this.leg2.xRot = Mth.cos(f * 1.5f + (float)Math.PI) * 4.4f * g;
        this.leg3.xRot = Mth.cos(f * 1.5f) * 4.4f * g;
    }
}

