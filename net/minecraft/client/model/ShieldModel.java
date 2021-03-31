/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

@Environment(value=EnvType.CLIENT)
public class ShieldModel
extends Model {
    private static final String PLATE = "plate";
    private static final String HANDLE = "handle";
    private static final int SHIELD_WIDTH = 10;
    private static final int SHIELD_HEIGHT = 20;
    private final ModelPart root;
    private final ModelPart plate;
    private final ModelPart handle;

    public ShieldModel(ModelPart modelPart) {
        super(RenderType::entitySolid);
        this.root = modelPart;
        this.plate = modelPart.getChild(PLATE);
        this.handle = modelPart.getChild(HANDLE);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(PLATE, CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -11.0f, -2.0f, 12.0f, 22.0f, 1.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(HANDLE, CubeListBuilder.create().texOffs(26, 0).addBox(-1.0f, -3.0f, -1.0f, 2.0f, 6.0f, 6.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public ModelPart plate() {
        return this.plate;
    }

    public ModelPart handle() {
        return this.handle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        this.root.render(poseStack, vertexConsumer, i, j, f, g, h, k);
    }
}

