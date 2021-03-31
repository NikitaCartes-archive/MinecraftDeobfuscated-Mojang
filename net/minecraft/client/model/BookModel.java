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
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class BookModel
extends Model {
    private static final String LEFT_PAGES = "left_pages";
    private static final String RIGHT_PAGES = "right_pages";
    private static final String FLIP_PAGE_1 = "flip_page1";
    private static final String FLIP_PAGE_2 = "flip_page2";
    private final ModelPart root;
    private final ModelPart leftLid;
    private final ModelPart rightLid;
    private final ModelPart leftPages;
    private final ModelPart rightPages;
    private final ModelPart flipPage1;
    private final ModelPart flipPage2;

    public BookModel(ModelPart modelPart) {
        super(RenderType::entitySolid);
        this.root = modelPart;
        this.leftLid = modelPart.getChild("left_lid");
        this.rightLid = modelPart.getChild("right_lid");
        this.leftPages = modelPart.getChild(LEFT_PAGES);
        this.rightPages = modelPart.getChild(RIGHT_PAGES);
        this.flipPage1 = modelPart.getChild(FLIP_PAGE_1);
        this.flipPage2 = modelPart.getChild(FLIP_PAGE_2);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("left_lid", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -5.0f, -0.005f, 6.0f, 10.0f, 0.005f), PartPose.offset(0.0f, 0.0f, -1.0f));
        partDefinition.addOrReplaceChild("right_lid", CubeListBuilder.create().texOffs(16, 0).addBox(0.0f, -5.0f, -0.005f, 6.0f, 10.0f, 0.005f), PartPose.offset(0.0f, 0.0f, 1.0f));
        partDefinition.addOrReplaceChild("seam", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0f, -5.0f, 0.0f, 2.0f, 10.0f, 0.005f), PartPose.rotation(0.0f, 1.5707964f, 0.0f));
        partDefinition.addOrReplaceChild(LEFT_PAGES, CubeListBuilder.create().texOffs(0, 10).addBox(0.0f, -4.0f, -0.99f, 5.0f, 8.0f, 1.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(RIGHT_PAGES, CubeListBuilder.create().texOffs(12, 10).addBox(0.0f, -4.0f, -0.01f, 5.0f, 8.0f, 1.0f), PartPose.ZERO);
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(24, 10).addBox(0.0f, -4.0f, 0.0f, 5.0f, 8.0f, 0.005f);
        partDefinition.addOrReplaceChild(FLIP_PAGE_1, cubeListBuilder, PartPose.ZERO);
        partDefinition.addOrReplaceChild(FLIP_PAGE_2, cubeListBuilder, PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        this.render(poseStack, vertexConsumer, i, j, f, g, h, k);
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        this.root.render(poseStack, vertexConsumer, i, j, f, g, h, k);
    }

    public void setupAnim(float f, float g, float h, float i) {
        float j = (Mth.sin(f * 0.02f) * 0.1f + 1.25f) * i;
        this.leftLid.yRot = (float)Math.PI + j;
        this.rightLid.yRot = -j;
        this.leftPages.yRot = j;
        this.rightPages.yRot = -j;
        this.flipPage1.yRot = j - j * 2.0f * g;
        this.flipPage2.yRot = j - j * 2.0f * h;
        this.leftPages.x = Mth.sin(j);
        this.rightPages.x = Mth.sin(j);
        this.flipPage1.x = Mth.sin(j);
        this.flipPage2.x = Mth.sin(j);
    }
}

