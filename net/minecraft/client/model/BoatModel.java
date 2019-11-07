/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

@Environment(value=EnvType.CLIENT)
public class BoatModel
extends ListModel<Boat> {
    private final ModelPart[] paddles = new ModelPart[2];
    private final ModelPart waterPatch;
    private final ImmutableList<ModelPart> parts;

    public BoatModel() {
        ModelPart[] modelParts = new ModelPart[]{new ModelPart(this, 0, 0).setTexSize(128, 64), new ModelPart(this, 0, 19).setTexSize(128, 64), new ModelPart(this, 0, 27).setTexSize(128, 64), new ModelPart(this, 0, 35).setTexSize(128, 64), new ModelPart(this, 0, 43).setTexSize(128, 64)};
        int i = 32;
        int j = 6;
        int k = 20;
        int l = 4;
        int m = 28;
        modelParts[0].addBox(-14.0f, -9.0f, -3.0f, 28.0f, 16.0f, 3.0f, 0.0f);
        modelParts[0].setPos(0.0f, 3.0f, 1.0f);
        modelParts[1].addBox(-13.0f, -7.0f, -1.0f, 18.0f, 6.0f, 2.0f, 0.0f);
        modelParts[1].setPos(-15.0f, 4.0f, 4.0f);
        modelParts[2].addBox(-8.0f, -7.0f, -1.0f, 16.0f, 6.0f, 2.0f, 0.0f);
        modelParts[2].setPos(15.0f, 4.0f, 0.0f);
        modelParts[3].addBox(-14.0f, -7.0f, -1.0f, 28.0f, 6.0f, 2.0f, 0.0f);
        modelParts[3].setPos(0.0f, 4.0f, -9.0f);
        modelParts[4].addBox(-14.0f, -7.0f, -1.0f, 28.0f, 6.0f, 2.0f, 0.0f);
        modelParts[4].setPos(0.0f, 4.0f, 9.0f);
        modelParts[0].xRot = 1.5707964f;
        modelParts[1].yRot = 4.712389f;
        modelParts[2].yRot = 1.5707964f;
        modelParts[3].yRot = (float)Math.PI;
        this.paddles[0] = this.makePaddle(true);
        this.paddles[0].setPos(3.0f, -5.0f, 9.0f);
        this.paddles[1] = this.makePaddle(false);
        this.paddles[1].setPos(3.0f, -5.0f, -9.0f);
        this.paddles[1].yRot = (float)Math.PI;
        this.paddles[0].zRot = 0.19634955f;
        this.paddles[1].zRot = 0.19634955f;
        this.waterPatch = new ModelPart(this, 0, 0).setTexSize(128, 64);
        this.waterPatch.addBox(-14.0f, -9.0f, -3.0f, 28.0f, 16.0f, 3.0f, 0.0f);
        this.waterPatch.setPos(0.0f, -3.0f, 1.0f);
        this.waterPatch.xRot = 1.5707964f;
        ImmutableList.Builder builder = ImmutableList.builder();
        builder.addAll(Arrays.asList(modelParts));
        builder.addAll(Arrays.asList(this.paddles));
        this.parts = builder.build();
    }

    @Override
    public void setupAnim(Boat boat, float f, float g, float h, float i, float j) {
        this.animatePaddle(boat, 0, f);
        this.animatePaddle(boat, 1, f);
    }

    public ImmutableList<ModelPart> parts() {
        return this.parts;
    }

    public ModelPart waterPatch() {
        return this.waterPatch;
    }

    protected ModelPart makePaddle(boolean bl) {
        ModelPart modelPart = new ModelPart(this, 62, bl ? 0 : 20).setTexSize(128, 64);
        int i = 20;
        int j = 7;
        int k = 6;
        float f = -5.0f;
        modelPart.addBox(-1.0f, 0.0f, -5.0f, 2.0f, 2.0f, 18.0f);
        modelPart.addBox(bl ? -1.001f : 0.001f, -3.0f, 8.0f, 1.0f, 6.0f, 7.0f);
        return modelPart;
    }

    protected void animatePaddle(Boat boat, int i, float f) {
        float g = boat.getRowingTime(i, f);
        ModelPart modelPart = this.paddles[i];
        modelPart.xRot = (float)Mth.clampedLerp(-1.0471975803375244, -0.2617993950843811, (Mth.sin(-g) + 1.0f) / 2.0f);
        modelPart.yRot = (float)Mth.clampedLerp(-0.7853981852531433, 0.7853981852531433, (Mth.sin(-g + 1.0f) + 1.0f) / 2.0f);
        if (i == 1) {
            modelPart.yRot = (float)Math.PI - modelPart.yRot;
        }
    }

    @Override
    public /* synthetic */ Iterable parts() {
        return this.parts();
    }
}

