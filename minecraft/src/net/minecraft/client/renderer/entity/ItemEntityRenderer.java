package net.minecraft.client.renderer.entity;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class ItemEntityRenderer extends EntityRenderer<ItemEntity, ItemEntityRenderState> {
	private static final float ITEM_BUNDLE_OFFSET_SCALE = 0.15F;
	private static final float FLAT_ITEM_BUNDLE_OFFSET_X = 0.0F;
	private static final float FLAT_ITEM_BUNDLE_OFFSET_Y = 0.0F;
	private static final float FLAT_ITEM_BUNDLE_OFFSET_Z = 0.09375F;
	private final ItemRenderer itemRenderer;
	private final RandomSource random = RandomSource.create();

	public ItemEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
		this.shadowRadius = 0.15F;
		this.shadowStrength = 0.75F;
	}

	public ItemEntityRenderState createRenderState() {
		return new ItemEntityRenderState();
	}

	public void extractRenderState(ItemEntity itemEntity, ItemEntityRenderState itemEntityRenderState, float f) {
		super.extractRenderState(itemEntity, itemEntityRenderState, f);
		itemEntityRenderState.ageInTicks = (float)itemEntity.getAge() + f;
		itemEntityRenderState.bobOffset = itemEntity.bobOffs;
		ItemStack itemStack = itemEntity.getItem();
		itemEntityRenderState.item = itemStack.copy();
		itemEntityRenderState.itemModel = this.itemRenderer.getModel(itemStack, itemEntity.level(), null, itemEntity.getId());
	}

	public void render(ItemEntityRenderState itemEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		BakedModel bakedModel = itemEntityRenderState.itemModel;
		if (bakedModel != null) {
			poseStack.pushPose();
			ItemStack itemStack = itemEntityRenderState.item;
			this.random.setSeed((long)getSeedForItemStack(itemStack));
			boolean bl = bakedModel.isGui3d();
			float f = 0.25F;
			float g = Mth.sin(itemEntityRenderState.ageInTicks / 10.0F + itemEntityRenderState.bobOffset) * 0.1F + 0.1F;
			float h = bakedModel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
			poseStack.translate(0.0F, g + 0.25F * h, 0.0F);
			float j = ItemEntity.getSpin(itemEntityRenderState.ageInTicks, itemEntityRenderState.bobOffset);
			poseStack.mulPose(Axis.YP.rotation(j));
			renderMultipleFromCount(this.itemRenderer, poseStack, multiBufferSource, i, itemStack, bakedModel, bl, this.random);
			poseStack.popPose();
			super.render(itemEntityRenderState, poseStack, multiBufferSource, i);
		}
	}

	public static int getSeedForItemStack(ItemStack itemStack) {
		return itemStack.isEmpty() ? 187 : Item.getId(itemStack.getItem()) + itemStack.getDamageValue();
	}

	@VisibleForTesting
	static int getRenderedAmount(int i) {
		if (i <= 1) {
			return 1;
		} else if (i <= 16) {
			return 2;
		} else if (i <= 32) {
			return 3;
		} else {
			return i <= 48 ? 4 : 5;
		}
	}

	public static void renderMultipleFromCount(
		ItemRenderer itemRenderer, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ItemStack itemStack, RandomSource randomSource, Level level
	) {
		BakedModel bakedModel = itemRenderer.getModel(itemStack, level, null, 0);
		renderMultipleFromCount(itemRenderer, poseStack, multiBufferSource, i, itemStack, bakedModel, bakedModel.isGui3d(), randomSource);
	}

	public static void renderMultipleFromCount(
		ItemRenderer itemRenderer,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		ItemStack itemStack,
		BakedModel bakedModel,
		boolean bl,
		RandomSource randomSource
	) {
		int j = getRenderedAmount(itemStack.getCount());
		float f = bakedModel.getTransforms().ground.scale.x();
		float g = bakedModel.getTransforms().ground.scale.y();
		float h = bakedModel.getTransforms().ground.scale.z();
		if (!bl) {
			float k = -0.0F * (float)(j - 1) * 0.5F * f;
			float l = -0.0F * (float)(j - 1) * 0.5F * g;
			float m = -0.09375F * (float)(j - 1) * 0.5F * h;
			poseStack.translate(k, l, m);
		}

		for (int n = 0; n < j; n++) {
			poseStack.pushPose();
			if (n > 0) {
				if (bl) {
					float l = (randomSource.nextFloat() * 2.0F - 1.0F) * 0.15F;
					float m = (randomSource.nextFloat() * 2.0F - 1.0F) * 0.15F;
					float o = (randomSource.nextFloat() * 2.0F - 1.0F) * 0.15F;
					poseStack.translate(l, m, o);
				} else {
					float l = (randomSource.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
					float m = (randomSource.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
					poseStack.translate(l, m, 0.0F);
				}
			}

			itemRenderer.render(itemStack, ItemDisplayContext.GROUND, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, bakedModel);
			poseStack.popPose();
			if (!bl) {
				poseStack.translate(0.0F * f, 0.0F * g, 0.09375F * h);
			}
		}
	}
}
