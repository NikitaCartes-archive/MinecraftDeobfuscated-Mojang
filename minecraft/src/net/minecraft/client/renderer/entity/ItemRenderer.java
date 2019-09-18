package net.minecraft.client.renderer.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.EntityBlockRenderer;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class ItemRenderer implements ResourceManagerReloadListener {
	public static final ResourceLocation ENCHANT_GLINT_LOCATION = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	private static final Set<Item> IGNORED = Sets.<Item>newHashSet(Items.AIR);
	public float blitOffset;
	private final ItemModelShaper itemModelShaper;
	private final TextureManager textureManager;
	private final ItemColors itemColors;

	public ItemRenderer(TextureManager textureManager, ModelManager modelManager, ItemColors itemColors) {
		this.textureManager = textureManager;
		this.itemModelShaper = new ItemModelShaper(modelManager);

		for (Item item : Registry.ITEM) {
			if (!IGNORED.contains(item)) {
				this.itemModelShaper.register(item, new ModelResourceLocation(Registry.ITEM.getKey(item), "inventory"));
			}
		}

		this.itemColors = itemColors;
	}

	public ItemModelShaper getItemModelShaper() {
		return this.itemModelShaper;
	}

	private void renderModelLists(BakedModel bakedModel, ItemStack itemStack) {
		this.renderModelLists(bakedModel, -1, itemStack);
	}

	private void renderModelLists(BakedModel bakedModel, int i) {
		this.renderModelLists(bakedModel, i, ItemStack.EMPTY);
	}

	private void renderModelLists(BakedModel bakedModel, int i, ItemStack itemStack) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.BLOCK);
		Random random = new Random();
		long l = 42L;

		for (Direction direction : Direction.values()) {
			random.setSeed(42L);
			this.renderQuadList(bufferBuilder, bakedModel.getQuads(null, direction, random), i, itemStack);
		}

		random.setSeed(42L);
		this.renderQuadList(bufferBuilder, bakedModel.getQuads(null, null, random), i, itemStack);
		tesselator.end();
	}

	public void render(ItemStack itemStack, BakedModel bakedModel) {
		if (!itemStack.isEmpty()) {
			RenderSystem.pushMatrix();
			RenderSystem.translatef(-0.5F, -0.5F, -0.5F);
			if (bakedModel.isCustomRenderer()) {
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.enableRescaleNormal();
				EntityBlockRenderer.instance.renderByItem(itemStack);
			} else {
				this.renderModelLists(bakedModel, itemStack);
				if (itemStack.hasFoil()) {
					renderFoilLayer(this.textureManager, () -> this.renderModelLists(bakedModel, -8372020), 8);
				}
			}

			RenderSystem.popMatrix();
		}
	}

	public static void renderFoilLayer(TextureManager textureManager, Runnable runnable, int i) {
		RenderSystem.depthMask(false);
		RenderSystem.depthFunc(514);
		RenderSystem.disableLighting();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
		textureManager.bind(ENCHANT_GLINT_LOCATION);
		RenderSystem.matrixMode(5890);
		RenderSystem.pushMatrix();
		RenderSystem.scalef((float)i, (float)i, (float)i);
		float f = (float)(Util.getMillis() % 3000L) / 3000.0F / (float)i;
		RenderSystem.translatef(f, 0.0F, 0.0F);
		RenderSystem.rotatef(-50.0F, 0.0F, 0.0F, 1.0F);
		runnable.run();
		RenderSystem.popMatrix();
		RenderSystem.pushMatrix();
		RenderSystem.scalef((float)i, (float)i, (float)i);
		float g = (float)(Util.getMillis() % 4873L) / 4873.0F / (float)i;
		RenderSystem.translatef(-g, 0.0F, 0.0F);
		RenderSystem.rotatef(10.0F, 0.0F, 0.0F, 1.0F);
		runnable.run();
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(5888);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.enableLighting();
		RenderSystem.depthFunc(515);
		RenderSystem.depthMask(true);
		textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
	}

	private void applyNormal(BufferBuilder bufferBuilder, BakedQuad bakedQuad) {
		Vec3i vec3i = bakedQuad.getDirection().getNormal();
		bufferBuilder.postNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
	}

	private void putQuadData(BufferBuilder bufferBuilder, BakedQuad bakedQuad, int i) {
		bufferBuilder.putBulkData(bakedQuad.getVertices());
		bufferBuilder.faceTex2(15728880, 15728880, 15728880, 15728880);
		bufferBuilder.fixupQuadColor(i);
		this.applyNormal(bufferBuilder, bakedQuad);
	}

	private void renderQuadList(BufferBuilder bufferBuilder, List<BakedQuad> list, int i, ItemStack itemStack) {
		boolean bl = i == -1 && !itemStack.isEmpty();
		int j = 0;

		for (int k = list.size(); j < k; j++) {
			BakedQuad bakedQuad = (BakedQuad)list.get(j);
			int l = i;
			if (bl && bakedQuad.isTinted()) {
				l = this.itemColors.getColor(itemStack, bakedQuad.getTintIndex());
				l |= -16777216;
			}

			this.putQuadData(bufferBuilder, bakedQuad, l);
		}
	}

	public boolean isGui3d(ItemStack itemStack) {
		BakedModel bakedModel = this.itemModelShaper.getItemModel(itemStack);
		return bakedModel == null ? false : bakedModel.isGui3d();
	}

	public void renderStatic(ItemStack itemStack, ItemTransforms.TransformType transformType) {
		if (!itemStack.isEmpty()) {
			BakedModel bakedModel = this.getModel(itemStack);
			this.renderStatic(itemStack, bakedModel, transformType, false);
		}
	}

	public BakedModel getModel(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity) {
		BakedModel bakedModel = this.itemModelShaper.getItemModel(itemStack);
		Item item = itemStack.getItem();
		return !item.hasProperties() ? bakedModel : this.resolveOverrides(bakedModel, itemStack, level, livingEntity);
	}

	public BakedModel getInHandModel(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		Item item = itemStack.getItem();
		BakedModel bakedModel;
		if (item == Items.TRIDENT) {
			bakedModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
		} else {
			bakedModel = this.itemModelShaper.getItemModel(itemStack);
		}

		return !item.hasProperties() ? bakedModel : this.resolveOverrides(bakedModel, itemStack, level, livingEntity);
	}

	public BakedModel getModel(ItemStack itemStack) {
		return this.getModel(itemStack, null, null);
	}

	private BakedModel resolveOverrides(BakedModel bakedModel, ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity) {
		BakedModel bakedModel2 = bakedModel.getOverrides().resolve(bakedModel, itemStack, level, livingEntity);
		return bakedModel2 == null ? this.itemModelShaper.getModelManager().getMissingModel() : bakedModel2;
	}

	public void renderWithMobState(ItemStack itemStack, LivingEntity livingEntity, ItemTransforms.TransformType transformType, boolean bl) {
		if (!itemStack.isEmpty() && livingEntity != null) {
			BakedModel bakedModel = this.getInHandModel(itemStack, livingEntity.level, livingEntity);
			this.renderStatic(itemStack, bakedModel, transformType, bl);
		}
	}

	protected void renderStatic(ItemStack itemStack, BakedModel bakedModel, ItemTransforms.TransformType transformType, boolean bl) {
		if (!itemStack.isEmpty()) {
			this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
			this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).pushFilter(false, false);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableRescaleNormal();
			RenderSystem.defaultAlphaFunc();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.pushMatrix();
			ItemTransforms itemTransforms = bakedModel.getTransforms();
			ItemTransforms.apply(itemTransforms.getTransform(transformType), bl);
			if (this.needsFlip(itemTransforms.getTransform(transformType))) {
				RenderSystem.cullFace(GlStateManager.CullFace.FRONT);
			}

			this.render(itemStack, bakedModel);
			RenderSystem.cullFace(GlStateManager.CullFace.BACK);
			RenderSystem.popMatrix();
			RenderSystem.disableRescaleNormal();
			RenderSystem.disableBlend();
			this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
			this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).popFilter();
		}
	}

	private boolean needsFlip(ItemTransform itemTransform) {
		return itemTransform.scale.x() < 0.0F ^ itemTransform.scale.y() < 0.0F ^ itemTransform.scale.z() < 0.0F;
	}

	public void renderGuiItem(ItemStack itemStack, int i, int j) {
		this.renderGuiItem(itemStack, i, j, this.getModel(itemStack));
	}

	protected void renderGuiItem(ItemStack itemStack, int i, int j, BakedModel bakedModel) {
		RenderSystem.pushMatrix();
		this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
		this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).pushFilter(false, false);
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.setupGuiItem(i, j, bakedModel.isGui3d());
		bakedModel.getTransforms().apply(ItemTransforms.TransformType.GUI);
		this.render(itemStack, bakedModel);
		RenderSystem.disableAlphaTest();
		RenderSystem.disableRescaleNormal();
		RenderSystem.disableLighting();
		RenderSystem.popMatrix();
		this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
		this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).popFilter();
	}

	private void setupGuiItem(int i, int j, boolean bl) {
		RenderSystem.translatef((float)i, (float)j, 100.0F + this.blitOffset);
		RenderSystem.translatef(8.0F, 8.0F, 0.0F);
		RenderSystem.scalef(1.0F, -1.0F, 1.0F);
		RenderSystem.scalef(16.0F, 16.0F, 16.0F);
		if (bl) {
			RenderSystem.enableLighting();
		} else {
			RenderSystem.disableLighting();
		}
	}

	public void renderAndDecorateItem(ItemStack itemStack, int i, int j) {
		this.renderAndDecorateItem(Minecraft.getInstance().player, itemStack, i, j);
	}

	public void renderAndDecorateItem(@Nullable LivingEntity livingEntity, ItemStack itemStack, int i, int j) {
		if (!itemStack.isEmpty()) {
			this.blitOffset += 50.0F;

			try {
				this.renderGuiItem(itemStack, i, j, this.getModel(itemStack, null, livingEntity));
			} catch (Throwable var8) {
				CrashReport crashReport = CrashReport.forThrowable(var8, "Rendering item");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
				crashReportCategory.setDetail("Item Type", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.getItem())));
				crashReportCategory.setDetail("Item Damage", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.getDamageValue())));
				crashReportCategory.setDetail("Item NBT", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.getTag())));
				crashReportCategory.setDetail("Item Foil", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.hasFoil())));
				throw new ReportedException(crashReport);
			}

			this.blitOffset -= 50.0F;
		}
	}

	public void renderGuiItemDecorations(Font font, ItemStack itemStack, int i, int j) {
		this.renderGuiItemDecorations(font, itemStack, i, j, null);
	}

	public void renderGuiItemDecorations(Font font, ItemStack itemStack, int i, int j, @Nullable String string) {
		if (!itemStack.isEmpty()) {
			if (itemStack.getCount() != 1 || string != null) {
				String string2 = string == null ? String.valueOf(itemStack.getCount()) : string;
				RenderSystem.disableLighting();
				RenderSystem.disableDepthTest();
				RenderSystem.disableBlend();
				font.drawShadow(string2, (float)(i + 19 - 2 - font.width(string2)), (float)(j + 6 + 3), 16777215);
				RenderSystem.enableBlend();
				RenderSystem.enableLighting();
				RenderSystem.enableDepthTest();
			}

			if (itemStack.isDamaged()) {
				RenderSystem.disableLighting();
				RenderSystem.disableDepthTest();
				RenderSystem.disableTexture();
				RenderSystem.disableAlphaTest();
				RenderSystem.disableBlend();
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferBuilder = tesselator.getBuilder();
				float f = (float)itemStack.getDamageValue();
				float g = (float)itemStack.getMaxDamage();
				float h = Math.max(0.0F, (g - f) / g);
				int k = Math.round(13.0F - f * 13.0F / g);
				int l = Mth.hsvToRgb(h / 3.0F, 1.0F, 1.0F);
				this.fillRect(bufferBuilder, i + 2, j + 13, 13, 2, 0, 0, 0, 255);
				this.fillRect(bufferBuilder, i + 2, j + 13, k, 1, l >> 16 & 0xFF, l >> 8 & 0xFF, l & 0xFF, 255);
				RenderSystem.enableBlend();
				RenderSystem.enableAlphaTest();
				RenderSystem.enableTexture();
				RenderSystem.enableLighting();
				RenderSystem.enableDepthTest();
			}

			LocalPlayer localPlayer = Minecraft.getInstance().player;
			float m = localPlayer == null ? 0.0F : localPlayer.getCooldowns().getCooldownPercent(itemStack.getItem(), Minecraft.getInstance().getFrameTime());
			if (m > 0.0F) {
				RenderSystem.disableLighting();
				RenderSystem.disableDepthTest();
				RenderSystem.disableTexture();
				Tesselator tesselator2 = Tesselator.getInstance();
				BufferBuilder bufferBuilder2 = tesselator2.getBuilder();
				this.fillRect(bufferBuilder2, i, j + Mth.floor(16.0F * (1.0F - m)), 16, Mth.ceil(16.0F * m), 255, 255, 255, 127);
				RenderSystem.enableTexture();
				RenderSystem.enableLighting();
				RenderSystem.enableDepthTest();
			}
		}
	}

	private void fillRect(BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, int o, int p) {
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex((double)(i + 0), (double)(j + 0), 0.0).color(m, n, o, p).endVertex();
		bufferBuilder.vertex((double)(i + 0), (double)(j + l), 0.0).color(m, n, o, p).endVertex();
		bufferBuilder.vertex((double)(i + k), (double)(j + l), 0.0).color(m, n, o, p).endVertex();
		bufferBuilder.vertex((double)(i + k), (double)(j + 0), 0.0).color(m, n, o, p).endVertex();
		Tesselator.getInstance().end();
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.itemModelShaper.rebuildCache();
	}
}
