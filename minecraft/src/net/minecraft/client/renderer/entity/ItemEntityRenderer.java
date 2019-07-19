package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemEntityRenderer extends EntityRenderer<ItemEntity> {
	private final ItemRenderer itemRenderer;
	private final Random random = new Random();

	public ItemEntityRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
		super(entityRenderDispatcher);
		this.itemRenderer = itemRenderer;
		this.shadowRadius = 0.15F;
		this.shadowStrength = 0.75F;
	}

	private int setupBobbingItem(ItemEntity itemEntity, double d, double e, double f, float g, BakedModel bakedModel) {
		ItemStack itemStack = itemEntity.getItem();
		Item item = itemStack.getItem();
		if (item == null) {
			return 0;
		} else {
			boolean bl = bakedModel.isGui3d();
			int i = this.getRenderAmount(itemStack);
			float h = 0.25F;
			float j = Mth.sin(((float)itemEntity.getAge() + g) / 10.0F + itemEntity.bobOffs) * 0.1F + 0.1F;
			float k = bakedModel.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).scale.y();
			GlStateManager.translatef((float)d, (float)e + j + 0.25F * k, (float)f);
			if (bl || this.entityRenderDispatcher.options != null) {
				float l = (((float)itemEntity.getAge() + g) / 20.0F + itemEntity.bobOffs) * (180.0F / (float)Math.PI);
				GlStateManager.rotatef(l, 0.0F, 1.0F, 0.0F);
			}

			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			return i;
		}
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

	public void render(ItemEntity itemEntity, double d, double e, double f, float g, float h) {
		ItemStack itemStack = itemEntity.getItem();
		int i = itemStack.isEmpty() ? 187 : Item.getId(itemStack.getItem()) + itemStack.getDamageValue();
		this.random.setSeed((long)i);
		boolean bl = false;
		if (this.bindTexture(itemEntity)) {
			this.entityRenderDispatcher.textureManager.getTexture(this.getTextureLocation(itemEntity)).pushFilter(false, false);
			bl = true;
		}

		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		Lighting.turnOn();
		GlStateManager.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		GlStateManager.pushMatrix();
		BakedModel bakedModel = this.itemRenderer.getModel(itemStack, itemEntity.level, null);
		int j = this.setupBobbingItem(itemEntity, d, e, f, h, bakedModel);
		float k = bakedModel.getTransforms().ground.scale.x();
		float l = bakedModel.getTransforms().ground.scale.y();
		float m = bakedModel.getTransforms().ground.scale.z();
		boolean bl2 = bakedModel.isGui3d();
		if (!bl2) {
			float n = -0.0F * (float)(j - 1) * 0.5F * k;
			float o = -0.0F * (float)(j - 1) * 0.5F * l;
			float p = -0.09375F * (float)(j - 1) * 0.5F * m;
			GlStateManager.translatef(n, o, p);
		}

		if (this.solidRender) {
			GlStateManager.enableColorMaterial();
			GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(itemEntity));
		}

		for (int q = 0; q < j; q++) {
			if (bl2) {
				GlStateManager.pushMatrix();
				if (q > 0) {
					float o = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
					float p = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
					float r = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
					GlStateManager.translatef(o, p, r);
				}

				bakedModel.getTransforms().apply(ItemTransforms.TransformType.GROUND);
				this.itemRenderer.render(itemStack, bakedModel);
				GlStateManager.popMatrix();
			} else {
				GlStateManager.pushMatrix();
				if (q > 0) {
					float o = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
					float p = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
					GlStateManager.translatef(o, p, 0.0F);
				}

				bakedModel.getTransforms().apply(ItemTransforms.TransformType.GROUND);
				this.itemRenderer.render(itemStack, bakedModel);
				GlStateManager.popMatrix();
				GlStateManager.translatef(0.0F * k, 0.0F * l, 0.09375F * m);
			}
		}

		if (this.solidRender) {
			GlStateManager.tearDownSolidRenderingTextureCombine();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		this.bindTexture(itemEntity);
		if (bl) {
			this.entityRenderDispatcher.textureManager.getTexture(this.getTextureLocation(itemEntity)).popFilter();
		}

		super.render(itemEntity, d, e, f, g, h);
	}

	protected ResourceLocation getTextureLocation(ItemEntity itemEntity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
