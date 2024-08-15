package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;

@Environment(EnvType.CLIENT)
public class ArmorStandRenderer extends LivingEntityRenderer<ArmorStand, ArmorStandRenderState, ArmorStandArmorModel> {
	public static final ResourceLocation DEFAULT_SKIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/armorstand/wood.png");
	private final ArmorStandArmorModel bigModel = this.getModel();
	private final ArmorStandArmorModel smallModel;

	public ArmorStandRenderer(EntityRendererProvider.Context context) {
		super(context, new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND)), 0.0F);
		this.smallModel = new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND_SMALL));
		this.addLayer(
			new HumanoidArmorLayer<>(
				this,
				new ArmorStandArmorModel(context.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)),
				new ArmorStandArmorModel(context.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR)),
				new ArmorStandArmorModel(context.bakeLayer(ModelLayers.ARMOR_STAND_SMALL_INNER_ARMOR)),
				new ArmorStandArmorModel(context.bakeLayer(ModelLayers.ARMOR_STAND_SMALL_OUTER_ARMOR)),
				context.getModelManager()
			)
		);
		this.addLayer(new ItemInHandLayer<>(this, context.getItemRenderer()));
		this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemRenderer()));
	}

	public ResourceLocation getTextureLocation(ArmorStandRenderState armorStandRenderState) {
		return DEFAULT_SKIN_LOCATION;
	}

	public ArmorStandRenderState createRenderState() {
		return new ArmorStandRenderState();
	}

	public void extractRenderState(ArmorStand armorStand, ArmorStandRenderState armorStandRenderState, float f) {
		super.extractRenderState(armorStand, armorStandRenderState, f);
		HumanoidMobRenderer.extractHumanoidRenderState(armorStand, armorStandRenderState, f);
		armorStandRenderState.yRot = Mth.rotLerp(f, armorStand.yRotO, armorStand.getYRot());
		armorStandRenderState.isMarker = armorStand.isMarker();
		armorStandRenderState.isSmall = armorStand.isSmall();
		armorStandRenderState.showArms = armorStand.showArms();
		armorStandRenderState.showBasePlate = armorStand.showBasePlate();
		armorStandRenderState.bodyPose = armorStand.getBodyPose();
		armorStandRenderState.headPose = armorStand.getHeadPose();
		armorStandRenderState.leftArmPose = armorStand.getLeftArmPose();
		armorStandRenderState.rightArmPose = armorStand.getRightArmPose();
		armorStandRenderState.leftLegPose = armorStand.getLeftLegPose();
		armorStandRenderState.rightLegPose = armorStand.getRightLegPose();
		armorStandRenderState.wiggle = (float)(armorStand.level().getGameTime() - armorStand.lastHit) + f;
	}

	public void render(ArmorStandRenderState armorStandRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		this.model = armorStandRenderState.isSmall ? this.smallModel : this.bigModel;
		super.render(armorStandRenderState, poseStack, multiBufferSource, i);
	}

	protected void setupRotations(ArmorStandRenderState armorStandRenderState, PoseStack poseStack, float f, float g) {
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
		if (armorStandRenderState.wiggle < 5.0F) {
			poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(armorStandRenderState.wiggle / 1.5F * (float) Math.PI) * 3.0F));
		}
	}

	protected boolean shouldShowName(ArmorStand armorStand, double d) {
		return armorStand.isCustomNameVisible();
	}

	@Nullable
	protected RenderType getRenderType(ArmorStandRenderState armorStandRenderState, boolean bl, boolean bl2, boolean bl3) {
		if (!armorStandRenderState.isMarker) {
			return super.getRenderType(armorStandRenderState, bl, bl2, bl3);
		} else {
			ResourceLocation resourceLocation = this.getTextureLocation(armorStandRenderState);
			if (bl2) {
				return RenderType.entityTranslucent(resourceLocation, false);
			} else {
				return bl ? RenderType.entityCutoutNoCull(resourceLocation, false) : null;
			}
		}
	}
}
