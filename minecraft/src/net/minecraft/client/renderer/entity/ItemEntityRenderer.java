package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemEntityRenderer extends EntityRenderer<ItemEntity> {
	private static final float ITEM_BUNDLE_OFFSET_SCALE = 0.15F;
	private static final int ITEM_COUNT_FOR_5_BUNDLE = 48;
	private static final int ITEM_COUNT_FOR_4_BUNDLE = 32;
	private static final int ITEM_COUNT_FOR_3_BUNDLE = 16;
	private static final int ITEM_COUNT_FOR_2_BUNDLE = 1;
	private static final float FLAT_ITEM_BUNDLE_OFFSET_X = 0.0F;
	private static final float FLAT_ITEM_BUNDLE_OFFSET_Y = 0.0F;
	private static final float FLAT_ITEM_BUNDLE_OFFSET_Z = 0.09375F;
	private final ItemRenderer itemRenderer;
	private final Random random = new Random();

	public ItemEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
		this.shadowRadius = 0.15F;
		this.shadowStrength = 0.75F;
	}

	private int getRenderAmount(ItemStack itemStack) {
		int i = 1;
		if (itemStack.getCount() > 48) {
			i = 5;
		} else if (itemStack.getCount() > 32) {
			i = 4;
		} else if (itemStack.getCount() > 16) {
			i = 3;
		} else if (itemStack.getCount() > 1) {
			i = 2;
		}

		return i;
	}

	public void render(ItemEntity itemEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		ItemStack itemStack = itemEntity.getItem();
		int j = itemStack.isEmpty() ? 187 : Item.getId(itemStack.getItem()) + itemStack.getDamageValue();
		this.random.setSeed((long)j);
		BakedModel bakedModel = this.itemRenderer.getModel(itemStack, itemEntity.level, null, itemEntity.getId());
		boolean bl = bakedModel.isGui3d();
		int k = this.getRenderAmount(itemStack);
		float h = 0.25F;
		float l = Mth.sin(((float)itemEntity.getAge() + g) / 10.0F + itemEntity.bobOffs) * 0.1F + 0.1F;
		float m = bakedModel.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).scale.y();
		poseStack.translate(0.0, (double)(l + 0.25F * m), 0.0);
		float n = itemEntity.getSpin(g);
		poseStack.mulPose(Vector3f.YP.rotation(n));
		float o = bakedModel.getTransforms().ground.scale.x();
		float p = bakedModel.getTransforms().ground.scale.y();
		float q = bakedModel.getTransforms().ground.scale.z();
		if (!bl) {
			float r = -0.0F * (float)(k - 1) * 0.5F * o;
			float s = -0.0F * (float)(k - 1) * 0.5F * p;
			float t = -0.09375F * (float)(k - 1) * 0.5F * q;
			poseStack.translate((double)r, (double)s, (double)t);
		}

		for (int u = 0; u < k; u++) {
			poseStack.pushPose();
			if (u > 0) {
				if (bl) {
					float s = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
					float t = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
					float v = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
					poseStack.translate((double)s, (double)t, (double)v);
				} else {
					float s = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
					float t = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
					poseStack.translate((double)s, (double)t, 0.0);
				}
			}

			this.itemRenderer.render(itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, bakedModel);
			poseStack.popPose();
			if (!bl) {
				poseStack.translate((double)(0.0F * o), (double)(0.0F * p), (double)(0.09375F * q));
			}
		}

		poseStack.popPose();
		super.render(itemEntity, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(ItemEntity itemEntity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
