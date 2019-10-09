package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.EntityBlockRenderer;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
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

	private void renderModelLists(BakedModel bakedModel, ItemStack itemStack, int i, int j, PoseStack poseStack, VertexConsumer vertexConsumer) {
		Random random = new Random();
		long l = 42L;

		for (Direction direction : Direction.values()) {
			random.setSeed(42L);
			this.renderQuadList(poseStack, vertexConsumer, bakedModel.getQuads(null, direction, random), itemStack, i, j);
		}

		random.setSeed(42L);
		this.renderQuadList(poseStack, vertexConsumer, bakedModel.getQuads(null, null, random), itemStack, i, j);
	}

	public void render(
		ItemStack itemStack,
		ItemTransforms.TransformType transformType,
		boolean bl,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j,
		BakedModel bakedModel
	) {
		if (!itemStack.isEmpty()) {
			poseStack.pushPose();
			if (itemStack.getItem() == Items.TRIDENT && transformType == ItemTransforms.TransformType.GUI) {
				bakedModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
			}

			bakedModel.getTransforms().getTransform(transformType).apply(bl, poseStack);
			poseStack.translate(-0.5, -0.5, -0.5);
			if (!bakedModel.isCustomRenderer() && (itemStack.getItem() != Items.TRIDENT || transformType == ItemTransforms.TransformType.GUI)) {
				VertexConsumer vertexConsumer = getFoilBuffer(multiBufferSource, RenderType.getRenderType(itemStack), true, itemStack.hasFoil());
				this.renderModelLists(bakedModel, itemStack, i, j, poseStack, vertexConsumer);
			} else {
				EntityBlockRenderer.instance.renderByItem(itemStack, poseStack, multiBufferSource, i, j);
			}

			poseStack.popPose();
		}
	}

	public static VertexConsumer getFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl, boolean bl2) {
		return (VertexConsumer)(bl2
			? new VertexMultiConsumer(
				ImmutableList.of(multiBufferSource.getBuffer(bl ? RenderType.glint() : RenderType.entityGlint()), multiBufferSource.getBuffer(renderType))
			)
			: multiBufferSource.getBuffer(renderType));
	}

	private void renderQuadList(PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, ItemStack itemStack, int i, int j) {
		boolean bl = !itemStack.isEmpty();
		Matrix4f matrix4f = poseStack.getPose();
		Matrix3f matrix3f = poseStack.getNormal();

		for (BakedQuad bakedQuad : list) {
			int k = -1;
			if (bl && bakedQuad.isTinted()) {
				k = this.itemColors.getColor(itemStack, bakedQuad.getTintIndex());
			}

			float f = (float)(k >> 16 & 0xFF) / 255.0F;
			float g = (float)(k >> 8 & 0xFF) / 255.0F;
			float h = (float)(k & 0xFF) / 255.0F;
			vertexConsumer.putBulkData(matrix4f, matrix3f, bakedQuad, f, g, h, i, j);
		}
	}

	public BakedModel getModel(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity) {
		Item item = itemStack.getItem();
		BakedModel bakedModel;
		if (item == Items.TRIDENT) {
			bakedModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
		} else {
			bakedModel = this.itemModelShaper.getItemModel(itemStack);
		}

		return !item.hasProperties() ? bakedModel : this.resolveOverrides(bakedModel, itemStack, level, livingEntity);
	}

	private BakedModel resolveOverrides(BakedModel bakedModel, ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity) {
		BakedModel bakedModel2 = bakedModel.getOverrides().resolve(bakedModel, itemStack, level, livingEntity);
		return bakedModel2 == null ? this.itemModelShaper.getModelManager().getMissingModel() : bakedModel2;
	}

	public void renderStatic(
		ItemStack itemStack, ItemTransforms.TransformType transformType, int i, int j, PoseStack poseStack, MultiBufferSource multiBufferSource
	) {
		this.renderStatic(null, itemStack, transformType, false, poseStack, multiBufferSource, null, i, j);
	}

	public void renderStatic(
		@Nullable LivingEntity livingEntity,
		ItemStack itemStack,
		ItemTransforms.TransformType transformType,
		boolean bl,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		@Nullable Level level,
		int i,
		int j
	) {
		if (!itemStack.isEmpty()) {
			BakedModel bakedModel = this.getModel(itemStack, level, livingEntity);
			this.render(itemStack, transformType, bl, poseStack, multiBufferSource, i, j, bakedModel);
		}
	}

	public void renderGuiItem(ItemStack itemStack, int i, int j) {
		this.renderGuiItem(itemStack, i, j, this.getModel(itemStack, null, null));
	}

	protected void renderGuiItem(ItemStack itemStack, int i, int j, BakedModel bakedModel) {
		RenderSystem.pushMatrix();
		this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
		this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.translatef((float)i, (float)j, 100.0F + this.blitOffset);
		RenderSystem.translatef(8.0F, 8.0F, 0.0F);
		RenderSystem.scalef(1.0F, -1.0F, 1.0F);
		RenderSystem.scalef(16.0F, 16.0F, 16.0F);
		PoseStack poseStack = new PoseStack();
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		this.render(itemStack, ItemTransforms.TransformType.GUI, false, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
		bufferSource.endBatch();
		RenderSystem.disableAlphaTest();
		RenderSystem.disableRescaleNormal();
		RenderSystem.popMatrix();
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
			PoseStack poseStack = new PoseStack();
			if (itemStack.getCount() != 1 || string != null) {
				String string2 = string == null ? String.valueOf(itemStack.getCount()) : string;
				poseStack.translate(0.0, 0.0, (double)(this.blitOffset + 200.0F));
				MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
				font.drawInBatch(
					string2, (float)(i + 19 - 2 - font.width(string2)), (float)(j + 6 + 3), 16777215, true, poseStack.getPose(), bufferSource, false, 0, 15728880
				);
				bufferSource.endBatch();
			}

			if (itemStack.isDamaged()) {
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
				RenderSystem.enableDepthTest();
			}

			LocalPlayer localPlayer = Minecraft.getInstance().player;
			float m = localPlayer == null ? 0.0F : localPlayer.getCooldowns().getCooldownPercent(itemStack.getItem(), Minecraft.getInstance().getFrameTime());
			if (m > 0.0F) {
				RenderSystem.disableDepthTest();
				RenderSystem.disableTexture();
				Tesselator tesselator2 = Tesselator.getInstance();
				BufferBuilder bufferBuilder2 = tesselator2.getBuilder();
				this.fillRect(bufferBuilder2, i, j + Mth.floor(16.0F * (1.0F - m)), 16, Mth.ceil(16.0F * m), 255, 255, 255, 127);
				RenderSystem.enableTexture();
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
