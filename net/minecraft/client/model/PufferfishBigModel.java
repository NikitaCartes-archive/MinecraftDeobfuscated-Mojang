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
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class PufferfishBigModel<T extends Entity>
extends ListModel<T> {
    private final ModelPart cube;
    private final ModelPart blueFin0;
    private final ModelPart blueFin1;
    private final ModelPart topFrontFin;
    private final ModelPart topMidFin;
    private final ModelPart topBackFin;
    private final ModelPart sideFrontFin0;
    private final ModelPart sideFrontFin1;
    private final ModelPart bottomFrontFin;
    private final ModelPart bottomBackFin;
    private final ModelPart bottomMidFin;
    private final ModelPart sideBackFin0;
    private final ModelPart sideBackFin1;

    public PufferfishBigModel() {
        this.texWidth = 32;
        this.texHeight = 32;
        int i = 22;
        this.cube = new ModelPart(this, 0, 0);
        this.cube.addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f);
        this.cube.setPos(0.0f, 22.0f, 0.0f);
        this.blueFin0 = new ModelPart(this, 24, 0);
        this.blueFin0.addBox(-2.0f, 0.0f, -1.0f, 2.0f, 1.0f, 2.0f);
        this.blueFin0.setPos(-4.0f, 15.0f, -2.0f);
        this.blueFin1 = new ModelPart(this, 24, 3);
        this.blueFin1.addBox(0.0f, 0.0f, -1.0f, 2.0f, 1.0f, 2.0f);
        this.blueFin1.setPos(4.0f, 15.0f, -2.0f);
        this.topFrontFin = new ModelPart(this, 15, 17);
        this.topFrontFin.addBox(-4.0f, -1.0f, 0.0f, 8.0f, 1.0f, 0.0f);
        this.topFrontFin.setPos(0.0f, 14.0f, -4.0f);
        this.topFrontFin.xRot = 0.7853982f;
        this.topMidFin = new ModelPart(this, 14, 16);
        this.topMidFin.addBox(-4.0f, -1.0f, 0.0f, 8.0f, 1.0f, 1.0f);
        this.topMidFin.setPos(0.0f, 14.0f, 0.0f);
        this.topBackFin = new ModelPart(this, 23, 18);
        this.topBackFin.addBox(-4.0f, -1.0f, 0.0f, 8.0f, 1.0f, 0.0f);
        this.topBackFin.setPos(0.0f, 14.0f, 4.0f);
        this.topBackFin.xRot = -0.7853982f;
        this.sideFrontFin0 = new ModelPart(this, 5, 17);
        this.sideFrontFin0.addBox(-1.0f, -8.0f, 0.0f, 1.0f, 8.0f, 0.0f);
        this.sideFrontFin0.setPos(-4.0f, 22.0f, -4.0f);
        this.sideFrontFin0.yRot = -0.7853982f;
        this.sideFrontFin1 = new ModelPart(this, 1, 17);
        this.sideFrontFin1.addBox(0.0f, -8.0f, 0.0f, 1.0f, 8.0f, 0.0f);
        this.sideFrontFin1.setPos(4.0f, 22.0f, -4.0f);
        this.sideFrontFin1.yRot = 0.7853982f;
        this.bottomFrontFin = new ModelPart(this, 15, 20);
        this.bottomFrontFin.addBox(-4.0f, 0.0f, 0.0f, 8.0f, 1.0f, 0.0f);
        this.bottomFrontFin.setPos(0.0f, 22.0f, -4.0f);
        this.bottomFrontFin.xRot = -0.7853982f;
        this.bottomMidFin = new ModelPart(this, 15, 20);
        this.bottomMidFin.addBox(-4.0f, 0.0f, 0.0f, 8.0f, 1.0f, 0.0f);
        this.bottomMidFin.setPos(0.0f, 22.0f, 0.0f);
        this.bottomBackFin = new ModelPart(this, 15, 20);
        this.bottomBackFin.addBox(-4.0f, 0.0f, 0.0f, 8.0f, 1.0f, 0.0f);
        this.bottomBackFin.setPos(0.0f, 22.0f, 4.0f);
        this.bottomBackFin.xRot = 0.7853982f;
        this.sideBackFin0 = new ModelPart(this, 9, 17);
        this.sideBackFin0.addBox(-1.0f, -8.0f, 0.0f, 1.0f, 8.0f, 0.0f);
        this.sideBackFin0.setPos(-4.0f, 22.0f, 4.0f);
        this.sideBackFin0.yRot = 0.7853982f;
        this.sideBackFin1 = new ModelPart(this, 9, 17);
        this.sideBackFin1.addBox(0.0f, -8.0f, 0.0f, 1.0f, 8.0f, 0.0f);
        this.sideBackFin1.setPos(4.0f, 22.0f, 4.0f);
        this.sideBackFin1.yRot = -0.7853982f;
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.cube, this.blueFin0, this.blueFin1, this.topFrontFin, this.topMidFin, this.topBackFin, this.sideFrontFin0, this.sideBackFin1, this.bottomFrontFin, this.bottomMidFin, this.bottomBackFin, this.sideBackFin0, new ModelPart[]{this.sideBackFin1});
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        this.blueFin0.zRot = -0.2f + 0.4f * Mth.sin(h * 0.2f);
        this.blueFin1.zRot = 0.2f - 0.4f * Mth.sin(h * 0.2f);
    }
}

