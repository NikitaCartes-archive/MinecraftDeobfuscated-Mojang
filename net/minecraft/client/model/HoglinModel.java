/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

@Environment(value=EnvType.CLIENT)
public class HoglinModel
extends AgeableListModel<Hoglin> {
    private final ModelPart head;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart body;
    private final ModelPart frontRightLeg;
    private final ModelPart frontLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart backLeftLeg;

    public HoglinModel() {
        super(true, 8.0f, 6.0f, 1.9f, 2.0f, 24.0f);
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
        this.rightEar = new ModelPart(this);
        this.rightEar.setPos(-6.0f, -2.0f, -3.0f);
        this.rightEar.texOffs(4, 16).addBox(-6.0f, -1.0f, -2.0f, 6.0f, 1.0f, 4.0f);
        this.rightEar.zRot = -0.6981317f;
        this.head.addChild(this.rightEar);
        this.leftEar = new ModelPart(this);
        this.leftEar.setPos(6.0f, -2.0f, -3.0f);
        this.leftEar.texOffs(4, 21).addBox(0.0f, -1.0f, -2.0f, 6.0f, 1.0f, 4.0f);
        this.leftEar.zRot = 0.6981317f;
        this.head.addChild(this.leftEar);
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
        this.frontRightLeg = new ModelPart(this);
        this.frontRightLeg.setPos(-4.0f, 10.0f, -8.5f);
        this.frontRightLeg.texOffs(46, 75).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 14.0f, 6.0f);
        this.frontLeftLeg = new ModelPart(this);
        this.frontLeftLeg.setPos(4.0f, 10.0f, -8.5f);
        this.frontLeftLeg.texOffs(71, 75).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 14.0f, 6.0f);
        this.backRightLeg = new ModelPart(this);
        this.backRightLeg.setPos(-5.0f, 13.0f, 10.0f);
        this.backRightLeg.texOffs(51, 43).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 11.0f, 5.0f);
        this.backLeftLeg = new ModelPart(this);
        this.backLeftLeg.setPos(5.0f, 13.0f, 10.0f);
        this.backLeftLeg.texOffs(72, 43).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 11.0f, 5.0f);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.frontRightLeg, this.frontLeftLeg, this.backRightLeg, this.backLeftLeg);
    }

    @Override
    public void setupAnim(Hoglin hoglin, float f, float g, float h, float i, float j) {
        this.rightEar.zRot = -0.6981317f - g * Mth.sin(f);
        this.leftEar.zRot = 0.6981317f + g * Mth.sin(f);
        this.head.yRot = i * ((float)Math.PI / 180);
        int k = hoglin.getAttackAnimationRemainingTicks();
        if (k > 0) {
            int l = 10 - k;
            float m = Mth.triangleWave(l, 10.0f);
            float n = (-m + 1.0f) / 2.0f;
            float o = -1.2217305f * n;
            this.head.xRot = 0.87266463f + o;
        } else {
            this.head.xRot = 0.87266463f;
        }
        float p = 1.2f;
        this.frontRightLeg.xRot = Mth.cos(f) * 1.2f * g;
        this.backRightLeg.xRot = this.frontLeftLeg.xRot = Mth.cos(f + (float)Math.PI) * 1.2f * g;
        this.backLeftLeg.xRot = this.frontRightLeg.xRot;
    }
}

