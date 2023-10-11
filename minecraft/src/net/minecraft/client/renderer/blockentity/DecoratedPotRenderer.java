package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nullable;
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
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;

@Environment(EnvType.CLIENT)
public class DecoratedPotRenderer implements BlockEntityRenderer<DecoratedPotBlockEntity> {
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
	private final Material baseMaterial = (Material)Objects.requireNonNull(Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.BASE));
	private static final float WOBBLE_AMPLITUDE = 0.125F;

	public DecoratedPotRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart modelPart = context.bakeLayer(ModelLayers.DECORATED_POT_BASE);
		this.neck = modelPart.getChild("neck");
		this.top = modelPart.getChild("top");
		this.bottom = modelPart.getChild("bottom");
		ModelPart modelPart2 = context.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
		this.frontSide = modelPart2.getChild("front");
		this.backSide = modelPart2.getChild("back");
		this.leftSide = modelPart2.getChild("left");
		this.rightSide = modelPart2.getChild("right");
	}

	public static LayerDefinition createBaseLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		CubeDeformation cubeDeformation = new CubeDeformation(0.2F);
		CubeDeformation cubeDeformation2 = new CubeDeformation(-0.1F);
		partDefinition.addOrReplaceChild(
			"neck",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(4.0F, 17.0F, 4.0F, 8.0F, 3.0F, 8.0F, cubeDeformation2)
				.texOffs(0, 5)
				.addBox(5.0F, 20.0F, 5.0F, 6.0F, 1.0F, 6.0F, cubeDeformation),
			PartPose.offsetAndRotation(0.0F, 37.0F, 16.0F, (float) Math.PI, 0.0F, 0.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(-14, 13).addBox(0.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F);
		partDefinition.addOrReplaceChild("top", cubeListBuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, 0.0F, 0.0F));
		partDefinition.addOrReplaceChild("bottom", cubeListBuilder, PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F));
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	public static LayerDefinition createSidesLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(1, 0).addBox(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 0.0F, EnumSet.of(Direction.NORTH));
		partDefinition.addOrReplaceChild("back", cubeListBuilder, PartPose.offsetAndRotation(15.0F, 16.0F, 1.0F, 0.0F, 0.0F, (float) Math.PI));
		partDefinition.addOrReplaceChild("left", cubeListBuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, (float) (-Math.PI / 2), (float) Math.PI));
		partDefinition.addOrReplaceChild("right", cubeListBuilder, PartPose.offsetAndRotation(15.0F, 16.0F, 15.0F, 0.0F, (float) (Math.PI / 2), (float) Math.PI));
		partDefinition.addOrReplaceChild("front", cubeListBuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 15.0F, (float) Math.PI, 0.0F, 0.0F));
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

	public void render(DecoratedPotBlockEntity decoratedPotBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		poseStack.pushPose();
		Direction direction = decoratedPotBlockEntity.getDirection();
		poseStack.translate(0.5, 0.0, 0.5);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - direction.toYRot()));
		poseStack.translate(-0.5, 0.0, -0.5);
		DecoratedPotBlockEntity.WobbleStyle wobbleStyle = decoratedPotBlockEntity.lastWobbleStyle;
		if (wobbleStyle != null && decoratedPotBlockEntity.getLevel() != null) {
			float g = ((float)(decoratedPotBlockEntity.getLevel().getGameTime() - decoratedPotBlockEntity.wobbleStartedAtTick) + f) / (float)wobbleStyle.duration;
			if (g >= 0.0F && g <= 1.0F) {
				if (wobbleStyle == DecoratedPotBlockEntity.WobbleStyle.POSITIVE) {
					float h = 0.015625F;
					float k = g * (float) (Math.PI * 2);
					float l = -1.5F * (Mth.cos(k) + 0.5F) * Mth.sin(k / 2.0F);
					poseStack.rotateAround(Axis.XP.rotation(l * 0.015625F), 0.5F, 0.0F, 0.5F);
					float m = Mth.sin(k);
					poseStack.rotateAround(Axis.ZP.rotation(m * 0.015625F), 0.5F, 0.0F, 0.5F);
				} else {
					float h = Mth.sin(-g * 3.0F * (float) Math.PI) * 0.125F;
					float k = 1.0F - g;
					poseStack.rotateAround(Axis.YP.rotation(h * k), 0.5F, 0.0F, 0.5F);
				}
			}
		}

		VertexConsumer vertexConsumer = this.baseMaterial.buffer(multiBufferSource, RenderType::entitySolid);
		this.neck.render(poseStack, vertexConsumer, i, j);
		this.top.render(poseStack, vertexConsumer, i, j);
		this.bottom.render(poseStack, vertexConsumer, i, j);
		DecoratedPotBlockEntity.Decorations decorations = decoratedPotBlockEntity.getDecorations();
		this.renderSide(this.frontSide, poseStack, multiBufferSource, i, j, getMaterial(decorations.front()));
		this.renderSide(this.backSide, poseStack, multiBufferSource, i, j, getMaterial(decorations.back()));
		this.renderSide(this.leftSide, poseStack, multiBufferSource, i, j, getMaterial(decorations.left()));
		this.renderSide(this.rightSide, poseStack, multiBufferSource, i, j, getMaterial(decorations.right()));
		poseStack.popPose();
	}

	private void renderSide(ModelPart modelPart, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, @Nullable Material material) {
		if (material == null) {
			material = getMaterial(Items.BRICK);
		}

		if (material != null) {
			modelPart.render(poseStack, material.buffer(multiBufferSource, RenderType::entitySolid), i, j);
		}
	}
}
