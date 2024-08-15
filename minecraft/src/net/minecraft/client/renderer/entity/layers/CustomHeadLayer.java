package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;

@Environment(EnvType.CLIENT)
public class CustomHeadLayer<S extends LivingEntityRenderState, M extends EntityModel<S> & HeadedModel> extends RenderLayer<S, M> {
	private static final float ITEM_SCALE = 0.625F;
	private static final float SKULL_SCALE = 1.1875F;
	private final CustomHeadLayer.Transforms transforms;
	private final Map<SkullBlock.Type, SkullModelBase> skullModels;
	private final ItemRenderer itemRenderer;

	public CustomHeadLayer(RenderLayerParent<S, M> renderLayerParent, EntityModelSet entityModelSet, ItemRenderer itemRenderer) {
		this(renderLayerParent, entityModelSet, CustomHeadLayer.Transforms.DEFAULT, itemRenderer);
	}

	public CustomHeadLayer(
		RenderLayerParent<S, M> renderLayerParent, EntityModelSet entityModelSet, CustomHeadLayer.Transforms transforms, ItemRenderer itemRenderer
	) {
		super(renderLayerParent);
		this.transforms = transforms;
		this.skullModels = SkullBlockRenderer.createSkullRenderers(entityModelSet);
		this.itemRenderer = itemRenderer;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S livingEntityRenderState, float f, float g) {
		ItemStack itemStack = livingEntityRenderState.headItem;
		BakedModel bakedModel = livingEntityRenderState.headItemModel;
		if (!itemStack.isEmpty() && bakedModel != null) {
			label26: {
				Item item = itemStack.getItem();
				poseStack.pushPose();
				poseStack.scale(this.transforms.horizontalScale(), 1.0F, this.transforms.horizontalScale());
				M entityModel = this.getParentModel();
				entityModel.root().translateAndRotate(poseStack);
				entityModel.getHead().translateAndRotate(poseStack);
				if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock abstractSkullBlock) {
					poseStack.translate(0.0F, this.transforms.skullYOffset(), 0.0F);
					poseStack.scale(1.1875F, -1.1875F, -1.1875F);
					ResolvableProfile resolvableProfile = itemStack.get(DataComponents.PROFILE);
					poseStack.translate(-0.5, 0.0, -0.5);
					SkullBlock.Type type = abstractSkullBlock.getType();
					SkullModelBase skullModelBase = (SkullModelBase)this.skullModels.get(type);
					RenderType renderType = SkullBlockRenderer.getRenderType(type, resolvableProfile);
					SkullBlockRenderer.renderSkull(null, 180.0F, livingEntityRenderState.wornHeadAnimationPos, poseStack, multiBufferSource, i, skullModelBase, renderType);
					break label26;
				}

				if (!(item instanceof ArmorItem armorItem) || armorItem.getEquipmentSlot() != EquipmentSlot.HEAD) {
					translateToHead(poseStack, this.transforms);
					this.itemRenderer.render(itemStack, ItemDisplayContext.HEAD, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, bakedModel);
				}
			}

			poseStack.popPose();
		}
	}

	public static void translateToHead(PoseStack poseStack, CustomHeadLayer.Transforms transforms) {
		poseStack.translate(0.0F, -0.25F + transforms.yOffset(), 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.scale(0.625F, -0.625F, -0.625F);
	}

	@Environment(EnvType.CLIENT)
	public static record Transforms(float yOffset, float skullYOffset, float horizontalScale) {
		public static final CustomHeadLayer.Transforms DEFAULT = new CustomHeadLayer.Transforms(0.0F, 0.0F, 1.0F);
	}
}
