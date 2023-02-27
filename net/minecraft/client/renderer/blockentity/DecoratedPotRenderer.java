/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DecoratedPotRenderer
implements BlockEntityRenderer<DecoratedPotBlockEntity> {
    private static final String NECK = "neck";
    private static final String FRONT = "front";
    private static final String BACK = "back";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";
    private static final String BOTTOM = "bottom";
    private final ModelPart neck;
    private final ModelPart frontSide;
    private final ModelPart backSide;
    private final ModelPart leftSide;
    private final ModelPart rightSide;
    private final ModelPart top;
    private final ModelPart bottom;
    private final Material baseMaterial = Objects.requireNonNull(Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.BASE));

    public DecoratedPotRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelPart = context.bakeLayer(ModelLayers.DECORATED_POT_BASE);
        this.neck = modelPart.getChild(NECK);
        this.top = modelPart.getChild(TOP);
        this.bottom = modelPart.getChild(BOTTOM);
        ModelPart modelPart2 = context.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
        this.frontSide = modelPart2.getChild(FRONT);
        this.backSide = modelPart2.getChild(BACK);
        this.leftSide = modelPart2.getChild(LEFT);
        this.rightSide = modelPart2.getChild(RIGHT);
    }

    public static LayerDefinition createBaseLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeDeformation cubeDeformation = new CubeDeformation(0.2f);
        CubeDeformation cubeDeformation2 = new CubeDeformation(-0.1f);
        partDefinition.addOrReplaceChild(NECK, CubeListBuilder.create().texOffs(0, 0).addBox(4.0f, 17.0f, 4.0f, 8.0f, 3.0f, 8.0f, cubeDeformation2).texOffs(0, 5).addBox(5.0f, 20.0f, 5.0f, 6.0f, 1.0f, 6.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 37.0f, 16.0f, (float)Math.PI, 0.0f, 0.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(-14, 13).addBox(0.0f, 0.0f, 0.0f, 14.0f, 0.0f, 14.0f);
        partDefinition.addOrReplaceChild(TOP, cubeListBuilder, PartPose.offsetAndRotation(1.0f, 16.0f, 1.0f, 0.0f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(BOTTOM, cubeListBuilder, PartPose.offsetAndRotation(1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    public static LayerDefinition createSidesLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(1, 0).addBox(0.0f, 0.0f, 0.0f, 14.0f, 16.0f, 0.0f, EnumSet.of(Direction.NORTH));
        partDefinition.addOrReplaceChild(BACK, cubeListBuilder, PartPose.offsetAndRotation(15.0f, 16.0f, 1.0f, 0.0f, 0.0f, (float)Math.PI));
        partDefinition.addOrReplaceChild(LEFT, cubeListBuilder, PartPose.offsetAndRotation(1.0f, 16.0f, 1.0f, 0.0f, -1.5707964f, (float)Math.PI));
        partDefinition.addOrReplaceChild(RIGHT, cubeListBuilder, PartPose.offsetAndRotation(15.0f, 16.0f, 15.0f, 0.0f, 1.5707964f, (float)Math.PI));
        partDefinition.addOrReplaceChild(FRONT, cubeListBuilder, PartPose.offsetAndRotation(1.0f, 16.0f, 15.0f, (float)Math.PI, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    @Nullable
    private static Material getMaterial(Item item) {
        Material material = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getResourceKey(item));
        if (material == null) {
            material = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getResourceKey(Items.BRICK));
        }
        return material;
    }

    @Override
    public void render(DecoratedPotBlockEntity decoratedPotBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        poseStack.pushPose();
        Direction direction = decoratedPotBlockEntity.getDirection();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - direction.toYRot()));
        poseStack.translate(-0.5, 0.0, -0.5);
        VertexConsumer vertexConsumer = this.baseMaterial.buffer(multiBufferSource, RenderType::entitySolid);
        this.neck.render(poseStack, vertexConsumer, i, j);
        this.top.render(poseStack, vertexConsumer, i, j);
        this.bottom.render(poseStack, vertexConsumer, i, j);
        List<Item> list = decoratedPotBlockEntity.getShards();
        this.renderSide(this.frontSide, poseStack, multiBufferSource, i, j, DecoratedPotRenderer.getMaterial(list.get(3)));
        this.renderSide(this.backSide, poseStack, multiBufferSource, i, j, DecoratedPotRenderer.getMaterial(list.get(0)));
        this.renderSide(this.leftSide, poseStack, multiBufferSource, i, j, DecoratedPotRenderer.getMaterial(list.get(1)));
        this.renderSide(this.rightSide, poseStack, multiBufferSource, i, j, DecoratedPotRenderer.getMaterial(list.get(2)));
        poseStack.popPose();
    }

    private void renderSide(ModelPart modelPart, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, @Nullable Material material) {
        if (material == null) {
            material = DecoratedPotRenderer.getMaterial(Items.BRICK);
        }
        if (material != null) {
            modelPart.render(poseStack, material.buffer(multiBufferSource, RenderType::entitySolid), i, j);
        }
    }
}

