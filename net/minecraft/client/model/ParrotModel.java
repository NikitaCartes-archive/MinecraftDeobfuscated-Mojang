/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;

@Environment(value=EnvType.CLIENT)
public class ParrotModel
extends ListModel<Parrot> {
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart head;
    private final ModelPart head2;
    private final ModelPart beak1;
    private final ModelPart beak2;
    private final ModelPart feather;
    private final ModelPart legLeft;
    private final ModelPart legRight;

    public ParrotModel() {
        this.texWidth = 32;
        this.texHeight = 32;
        this.body = new ModelPart(this, 2, 8);
        this.body.addBox(-1.5f, 0.0f, -1.5f, 3.0f, 6.0f, 3.0f);
        this.body.setPos(0.0f, 16.5f, -3.0f);
        this.tail = new ModelPart(this, 22, 1);
        this.tail.addBox(-1.5f, -1.0f, -1.0f, 3.0f, 4.0f, 1.0f);
        this.tail.setPos(0.0f, 21.07f, 1.16f);
        this.wingLeft = new ModelPart(this, 19, 8);
        this.wingLeft.addBox(-0.5f, 0.0f, -1.5f, 1.0f, 5.0f, 3.0f);
        this.wingLeft.setPos(1.5f, 16.94f, -2.76f);
        this.wingRight = new ModelPart(this, 19, 8);
        this.wingRight.addBox(-0.5f, 0.0f, -1.5f, 1.0f, 5.0f, 3.0f);
        this.wingRight.setPos(-1.5f, 16.94f, -2.76f);
        this.head = new ModelPart(this, 2, 2);
        this.head.addBox(-1.0f, -1.5f, -1.0f, 2.0f, 3.0f, 2.0f);
        this.head.setPos(0.0f, 15.69f, -2.76f);
        this.head2 = new ModelPart(this, 10, 0);
        this.head2.addBox(-1.0f, -0.5f, -2.0f, 2.0f, 1.0f, 4.0f);
        this.head2.setPos(0.0f, -2.0f, -1.0f);
        this.head.addChild(this.head2);
        this.beak1 = new ModelPart(this, 11, 7);
        this.beak1.addBox(-0.5f, -1.0f, -0.5f, 1.0f, 2.0f, 1.0f);
        this.beak1.setPos(0.0f, -0.5f, -1.5f);
        this.head.addChild(this.beak1);
        this.beak2 = new ModelPart(this, 16, 7);
        this.beak2.addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f);
        this.beak2.setPos(0.0f, -1.75f, -2.45f);
        this.head.addChild(this.beak2);
        this.feather = new ModelPart(this, 2, 18);
        this.feather.addBox(0.0f, -4.0f, -2.0f, 0.0f, 5.0f, 4.0f);
        this.feather.setPos(0.0f, -2.15f, 0.15f);
        this.head.addChild(this.feather);
        this.legLeft = new ModelPart(this, 14, 18);
        this.legLeft.addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f);
        this.legLeft.setPos(1.0f, 22.0f, -1.05f);
        this.legRight = new ModelPart(this, 14, 18);
        this.legRight.addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f);
        this.legRight.setPos(-1.0f, 22.0f, -1.05f);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.body, this.wingLeft, this.wingRight, this.tail, this.head, this.legLeft, this.legRight);
    }

    @Override
    public void setupAnim(Parrot parrot, float f, float g, float h, float i, float j, float k) {
        this.setupAnim(ParrotModel.getState(parrot), parrot.tickCount, f, g, h, i, j);
    }

    @Override
    public void prepareMobModel(Parrot parrot, float f, float g, float h) {
        this.prepare(ParrotModel.getState(parrot));
    }

    public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, float f, float g, float h, float j, float k, int l) {
        this.prepare(State.ON_SHOULDER);
        this.setupAnim(State.ON_SHOULDER, l, f, g, 0.0f, h, j);
        this.parts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, k, i, null));
    }

    private void setupAnim(State state, int i, float f, float g, float h, float j, float k) {
        this.head.xRot = k * ((float)Math.PI / 180);
        this.head.yRot = j * ((float)Math.PI / 180);
        this.head.zRot = 0.0f;
        this.head.x = 0.0f;
        this.body.x = 0.0f;
        this.tail.x = 0.0f;
        this.wingRight.x = -1.5f;
        this.wingLeft.x = 1.5f;
        switch (state) {
            case SITTING: {
                break;
            }
            case PARTY: {
                float l = Mth.cos(i);
                float m = Mth.sin(i);
                this.head.x = l;
                this.head.y = 15.69f + m;
                this.head.xRot = 0.0f;
                this.head.yRot = 0.0f;
                this.head.zRot = Mth.sin(i) * 0.4f;
                this.body.x = l;
                this.body.y = 16.5f + m;
                this.wingLeft.zRot = -0.0873f - h;
                this.wingLeft.x = 1.5f + l;
                this.wingLeft.y = 16.94f + m;
                this.wingRight.zRot = 0.0873f + h;
                this.wingRight.x = -1.5f + l;
                this.wingRight.y = 16.94f + m;
                this.tail.x = l;
                this.tail.y = 21.07f + m;
                break;
            }
            case STANDING: {
                this.legLeft.xRot += Mth.cos(f * 0.6662f) * 1.4f * g;
                this.legRight.xRot += Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
            }
            default: {
                float n = h * 0.3f;
                this.head.y = 15.69f + n;
                this.tail.xRot = 1.015f + Mth.cos(f * 0.6662f) * 0.3f * g;
                this.tail.y = 21.07f + n;
                this.body.y = 16.5f + n;
                this.wingLeft.zRot = -0.0873f - h;
                this.wingLeft.y = 16.94f + n;
                this.wingRight.zRot = 0.0873f + h;
                this.wingRight.y = 16.94f + n;
                this.legLeft.y = 22.0f + n;
                this.legRight.y = 22.0f + n;
            }
        }
    }

    private void prepare(State state) {
        this.feather.xRot = -0.2214f;
        this.body.xRot = 0.4937f;
        this.wingLeft.xRot = -0.6981f;
        this.wingLeft.yRot = (float)(-Math.PI);
        this.wingRight.xRot = -0.6981f;
        this.wingRight.yRot = (float)(-Math.PI);
        this.legLeft.xRot = -0.0299f;
        this.legRight.xRot = -0.0299f;
        this.legLeft.y = 22.0f;
        this.legRight.y = 22.0f;
        this.legLeft.zRot = 0.0f;
        this.legRight.zRot = 0.0f;
        switch (state) {
            case FLYING: {
                this.legLeft.xRot += 0.6981317f;
                this.legRight.xRot += 0.6981317f;
                break;
            }
            case SITTING: {
                float f = 1.9f;
                this.head.y = 17.59f;
                this.tail.xRot = 1.5388988f;
                this.tail.y = 22.97f;
                this.body.y = 18.4f;
                this.wingLeft.zRot = -0.0873f;
                this.wingLeft.y = 18.84f;
                this.wingRight.zRot = 0.0873f;
                this.wingRight.y = 18.84f;
                this.legLeft.y += 1.9f;
                this.legRight.y += 1.9f;
                this.legLeft.xRot += 1.5707964f;
                this.legRight.xRot += 1.5707964f;
                break;
            }
            case PARTY: {
                this.legLeft.zRot = -0.34906584f;
                this.legRight.zRot = 0.34906584f;
                break;
            }
        }
    }

    private static State getState(Parrot parrot) {
        if (parrot.isPartyParrot()) {
            return State.PARTY;
        }
        if (parrot.isSitting()) {
            return State.SITTING;
        }
        if (parrot.isFlying()) {
            return State.FLYING;
        }
        return State.STANDING;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum State {
        FLYING,
        STANDING,
        SITTING,
        PARTY,
        ON_SHOULDER;

    }
}

