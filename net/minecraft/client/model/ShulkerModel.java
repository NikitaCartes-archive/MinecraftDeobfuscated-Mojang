/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Shulker;

@Environment(value=EnvType.CLIENT)
public class ShulkerModel<T extends Shulker>
extends ListModel<T> {
    private final ModelPart base;
    private final ModelPart lid;
    private final ModelPart head;

    public ShulkerModel(ModelPart modelPart) {
        super(RenderType::entityCutoutNoCullZOffset);
        this.lid = modelPart.getChild("lid");
        this.base = modelPart.getChild("base");
        this.head = modelPart.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -16.0f, -8.0f, 16.0f, 12.0f, 16.0f), PartPose.offset(0.0f, 24.0f, 0.0f));
        partDefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 28).addBox(-8.0f, -8.0f, -8.0f, 16.0f, 8.0f, 16.0f), PartPose.offset(0.0f, 24.0f, 0.0f));
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 52).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 6.0f, 6.0f), PartPose.offset(0.0f, 12.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(T shulker, float f, float g, float h, float i, float j) {
        float k = h - (float)((Shulker)shulker).tickCount;
        float l = (0.5f + ((Shulker)shulker).getClientPeekAmount(k)) * (float)Math.PI;
        float m = -1.0f + Mth.sin(l);
        float n = 0.0f;
        if (l > (float)Math.PI) {
            n = Mth.sin(h * 0.1f) * 0.7f;
        }
        this.lid.setPos(0.0f, 16.0f + Mth.sin(l) * 8.0f + n, 0.0f);
        this.lid.yRot = ((Shulker)shulker).getClientPeekAmount(k) > 0.3f ? m * m * m * m * (float)Math.PI * 0.125f : 0.0f;
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = (((Shulker)shulker).yHeadRot - 180.0f - ((Shulker)shulker).yBodyRot) * ((float)Math.PI / 180);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.base, this.lid);
    }

    public ModelPart getLid() {
        return this.lid;
    }

    public ModelPart getHead() {
        return this.head;
    }
}

