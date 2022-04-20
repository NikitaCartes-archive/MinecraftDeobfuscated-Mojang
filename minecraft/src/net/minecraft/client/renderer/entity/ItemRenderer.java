package net.minecraft.client.renderer.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.List;
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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
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
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;

@Environment(EnvType.CLIENT)
public class ItemRenderer implements ResourceManagerReloadListener {
	public static final ResourceLocation ENCHANT_GLINT_LOCATION = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	private static final Set<Item> IGNORED = Sets.<Item>newHashSet(Items.AIR);
	private static final int GUI_SLOT_CENTER_X = 8;
	private static final int GUI_SLOT_CENTER_Y = 8;
	public static final int ITEM_COUNT_BLIT_OFFSET = 200;
	public static final float COMPASS_FOIL_UI_SCALE = 0.5F;
	public static final float COMPASS_FOIL_FIRST_PERSON_SCALE = 0.75F;
	public float blitOffset;
	private final ItemModelShaper itemModelShaper;
	private final TextureManager textureManager;
	private final ItemColors itemColors;
	private final BlockEntityWithoutLevelRenderer blockEntityRenderer;

	public ItemRenderer(
		TextureManager textureManager, ModelManager modelManager, ItemColors itemColors, BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer
	) {
		this.textureManager = textureManager;
		this.itemModelShaper = new ItemModelShaper(modelManager);
		this.blockEntityRenderer = blockEntityWithoutLevelRenderer;

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
		RandomSource randomSource = RandomSource.create();
		long l = 42L;

		for (Direction direction : Direction.values()) {
			randomSource.setSeed(42L);
			this.renderQuadList(poseStack, vertexConsumer, bakedModel.getQuads(null, direction, randomSource), itemStack, i, j);
		}

		randomSource.setSeed(42L);
		this.renderQuadList(poseStack, vertexConsumer, bakedModel.getQuads(null, null, randomSource), itemStack, i, j);
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
			boolean bl2 = transformType == ItemTransforms.TransformType.GUI
				|| transformType == ItemTransforms.TransformType.GROUND
				|| transformType == ItemTransforms.TransformType.FIXED;
			if (bl2) {
				if (itemStack.is(Items.TRIDENT)) {
					bakedModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
				} else if (itemStack.is(Items.SPYGLASS)) {
					bakedModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:spyglass#inventory"));
				}
			}

			bakedModel.getTransforms().getTransform(transformType).apply(bl, poseStack);
			poseStack.translate(-0.5, -0.5, -0.5);
			if (!bakedModel.isCustomRenderer() && (!itemStack.is(Items.TRIDENT) || bl2)) {
				boolean bl3;
				if (transformType != ItemTransforms.TransformType.GUI && !transformType.firstPerson() && itemStack.getItem() instanceof BlockItem) {
					Block block = ((BlockItem)itemStack.getItem()).getBlock();
					bl3 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
				} else {
					bl3 = true;
				}

				RenderType renderType = ItemBlockRenderTypes.getRenderType(itemStack, bl3);
				VertexConsumer vertexConsumer;
				if (itemStack.is(ItemTags.COMPASSES) && itemStack.hasFoil()) {
					poseStack.pushPose();
					PoseStack.Pose pose = poseStack.last();
					if (transformType == ItemTransforms.TransformType.GUI) {
						pose.pose().multiply(0.5F);
					} else if (transformType.firstPerson()) {
						pose.pose().multiply(0.75F);
					}

					if (bl3) {
						vertexConsumer = getCompassFoilBufferDirect(multiBufferSource, renderType, pose);
					} else {
						vertexConsumer = getCompassFoilBuffer(multiBufferSource, renderType, pose);
					}

					poseStack.popPose();
				} else if (bl3) {
					vertexConsumer = getFoilBufferDirect(multiBufferSource, renderType, true, itemStack.hasFoil());
				} else {
					vertexConsumer = getFoilBuffer(multiBufferSource, renderType, true, itemStack.hasFoil());
				}

				this.renderModelLists(bakedModel, itemStack, i, j, poseStack, vertexConsumer);
			} else {
				this.blockEntityRenderer.renderByItem(itemStack, transformType, poseStack, multiBufferSource, i, j);
			}

			poseStack.popPose();
		}
	}

	public static VertexConsumer getArmorFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl, boolean bl2) {
		return bl2
			? VertexMultiConsumer.create(
				multiBufferSource.getBuffer(bl ? RenderType.armorGlint() : RenderType.armorEntityGlint()), multiBufferSource.getBuffer(renderType)
			)
			: multiBufferSource.getBuffer(renderType);
	}

	public static VertexConsumer getCompassFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, PoseStack.Pose pose) {
		return VertexMultiConsumer.create(
			new SheetedDecalTextureGenerator(multiBufferSource.getBuffer(RenderType.glint()), pose.pose(), pose.normal()), multiBufferSource.getBuffer(renderType)
		);
	}

	public static VertexConsumer getCompassFoilBufferDirect(MultiBufferSource multiBufferSource, RenderType renderType, PoseStack.Pose pose) {
		return VertexMultiConsumer.create(
			new SheetedDecalTextureGenerator(multiBufferSource.getBuffer(RenderType.glintDirect()), pose.pose(), pose.normal()), multiBufferSource.getBuffer(renderType)
		);
	}

	public static VertexConsumer getFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl, boolean bl2) {
		if (bl2) {
			return Minecraft.useShaderTransparency() && renderType == Sheets.translucentItemSheet()
				? VertexMultiConsumer.create(multiBufferSource.getBuffer(RenderType.glintTranslucent()), multiBufferSource.getBuffer(renderType))
				: VertexMultiConsumer.create(multiBufferSource.getBuffer(bl ? RenderType.glint() : RenderType.entityGlint()), multiBufferSource.getBuffer(renderType));
		} else {
			return multiBufferSource.getBuffer(renderType);
		}
	}

	public static VertexConsumer getFoilBufferDirect(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl, boolean bl2) {
		return bl2
			? VertexMultiConsumer.create(
				multiBufferSource.getBuffer(bl ? RenderType.glintDirect() : RenderType.entityGlintDirect()), multiBufferSource.getBuffer(renderType)
			)
			: multiBufferSource.getBuffer(renderType);
	}

	private void renderQuadList(PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, ItemStack itemStack, int i, int j) {
		boolean bl = !itemStack.isEmpty();
		PoseStack.Pose pose = poseStack.last();

		for (BakedQuad bakedQuad : list) {
			int k = -1;
			if (bl && bakedQuad.isTinted()) {
				k = this.itemColors.getColor(itemStack, bakedQuad.getTintIndex());
			}

			float f = (float)(k >> 16 & 0xFF) / 255.0F;
			float g = (float)(k >> 8 & 0xFF) / 255.0F;
			float h = (float)(k & 0xFF) / 255.0F;
			vertexConsumer.putBulkData(pose, bakedQuad, f, g, h, i, j);
		}
	}

	public BakedModel getModel(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity, int i) {
		BakedModel bakedModel;
		if (itemStack.is(Items.TRIDENT)) {
			bakedModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
		} else if (itemStack.is(Items.SPYGLASS)) {
			bakedModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:spyglass_in_hand#inventory"));
		} else {
			bakedModel = this.itemModelShaper.getItemModel(itemStack);
		}

		ClientLevel clientLevel = level instanceof ClientLevel ? (ClientLevel)level : null;
		BakedModel bakedModel2 = bakedModel.getOverrides().resolve(bakedModel, itemStack, clientLevel, livingEntity, i);
		return bakedModel2 == null ? this.itemModelShaper.getModelManager().getMissingModel() : bakedModel2;
	}

	public void renderStatic(
		ItemStack itemStack, ItemTransforms.TransformType transformType, int i, int j, PoseStack poseStack, MultiBufferSource multiBufferSource, int k
	) {
		this.renderStatic(null, itemStack, transformType, false, poseStack, multiBufferSource, null, i, j, k);
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
		int j,
		int k
	) {
		if (!itemStack.isEmpty()) {
			BakedModel bakedModel = this.getModel(itemStack, level, livingEntity, k);
			this.render(itemStack, transformType, bl, poseStack, multiBufferSource, i, j, bakedModel);
		}
	}

	public void renderGuiItem(ItemStack itemStack, int i, int j) {
		this.renderGuiItem(itemStack, i, j, this.getModel(itemStack, null, null, 0));
	}

	protected void renderGuiItem(ItemStack itemStack, int i, int j, BakedModel bakedModel) {
		this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
		RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		PoseStack poseStack = RenderSystem.getModelViewStack();
		poseStack.pushPose();
		poseStack.translate((double)i, (double)j, (double)(100.0F + this.blitOffset));
		poseStack.translate(8.0, 8.0, 0.0);
		poseStack.scale(1.0F, -1.0F, 1.0F);
		poseStack.scale(16.0F, 16.0F, 16.0F);
		RenderSystem.applyModelViewMatrix();
		PoseStack poseStack2 = new PoseStack();
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		boolean bl = !bakedModel.usesBlockLight();
		if (bl) {
			Lighting.setupForFlatItems();
		}

		this.render(itemStack, ItemTransforms.TransformType.GUI, false, poseStack2, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
		bufferSource.endBatch();
		RenderSystem.enableDepthTest();
		if (bl) {
			Lighting.setupFor3DItems();
		}

		poseStack.popPose();
		RenderSystem.applyModelViewMatrix();
	}

	public void renderAndDecorateItem(ItemStack itemStack, int i, int j) {
		this.tryRenderGuiItem(Minecraft.getInstance().player, itemStack, i, j, 0);
	}

	public void renderAndDecorateItem(ItemStack itemStack, int i, int j, int k) {
		this.tryRenderGuiItem(Minecraft.getInstance().player, itemStack, i, j, k);
	}

	public void renderAndDecorateItem(ItemStack itemStack, int i, int j, int k, int l) {
		this.tryRenderGuiItem(Minecraft.getInstance().player, itemStack, i, j, k, l);
	}

	public void renderAndDecorateFakeItem(ItemStack itemStack, int i, int j) {
		this.tryRenderGuiItem(null, itemStack, i, j, 0);
	}

	public void renderAndDecorateItem(LivingEntity livingEntity, ItemStack itemStack, int i, int j, int k) {
		this.tryRenderGuiItem(livingEntity, itemStack, i, j, k);
	}

	private void tryRenderGuiItem(@Nullable LivingEntity livingEntity, ItemStack itemStack, int i, int j, int k) {
		this.tryRenderGuiItem(livingEntity, itemStack, i, j, k, 0);
	}

	private void tryRenderGuiItem(@Nullable LivingEntity livingEntity, ItemStack itemStack, int i, int j, int k, int l) {
		if (!itemStack.isEmpty()) {
			BakedModel bakedModel = this.getModel(itemStack, null, livingEntity, k);
			this.blitOffset = bakedModel.isGui3d() ? this.blitOffset + 50.0F + (float)l : this.blitOffset + 50.0F;

			try {
				this.renderGuiItem(itemStack, i, j, bakedModel);
			} catch (Throwable var11) {
				CrashReport crashReport = CrashReport.forThrowable(var11, "Rendering item");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
				crashReportCategory.setDetail("Item Type", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.getItem())));
				crashReportCategory.setDetail("Item Damage", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.getDamageValue())));
				crashReportCategory.setDetail("Item NBT", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.getTag())));
				crashReportCategory.setDetail("Item Foil", (CrashReportDetail<String>)(() -> String.valueOf(itemStack.hasFoil())));
				throw new ReportedException(crashReport);
			}

			this.blitOffset = bakedModel.isGui3d() ? this.blitOffset - 50.0F - (float)l : this.blitOffset - 50.0F;
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
					string2, (float)(i + 19 - 2 - font.width(string2)), (float)(j + 6 + 3), 16777215, true, poseStack.last().pose(), bufferSource, false, 0, 15728880
				);
				bufferSource.endBatch();
			}

			if (itemStack.isBarVisible()) {
				RenderSystem.disableDepthTest();
				RenderSystem.disableTexture();
				RenderSystem.disableBlend();
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferBuilder = tesselator.getBuilder();
				int k = itemStack.getBarWidth();
				int l = itemStack.getBarColor();
				this.fillRect(bufferBuilder, i + 2, j + 13, 13, 2, 0, 0, 0, 255);
				this.fillRect(bufferBuilder, i + 2, j + 13, k, 1, l >> 16 & 0xFF, l >> 8 & 0xFF, l & 0xFF, 255);
				RenderSystem.enableBlend();
				RenderSystem.enableTexture();
				RenderSystem.enableDepthTest();
			}

			LocalPlayer localPlayer = Minecraft.getInstance().player;
			float f = localPlayer == null ? 0.0F : localPlayer.getCooldowns().getCooldownPercent(itemStack.getItem(), Minecraft.getInstance().getFrameTime());
			if (f > 0.0F) {
				RenderSystem.disableDepthTest();
				RenderSystem.disableTexture();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				Tesselator tesselator2 = Tesselator.getInstance();
				BufferBuilder bufferBuilder2 = tesselator2.getBuilder();
				this.fillRect(bufferBuilder2, i, j + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
				RenderSystem.enableTexture();
				RenderSystem.enableDepthTest();
			}
		}
	}

	private void fillRect(BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, int o, int p) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex((double)(i + 0), (double)(j + 0), 0.0).color(m, n, o, p).endVertex();
		bufferBuilder.vertex((double)(i + 0), (double)(j + l), 0.0).color(m, n, o, p).endVertex();
		bufferBuilder.vertex((double)(i + k), (double)(j + l), 0.0).color(m, n, o, p).endVertex();
		bufferBuilder.vertex((double)(i + k), (double)(j + 0), 0.0).color(m, n, o, p).endVertex();
		bufferBuilder.end();
		BufferUploader.drawWithShader(bufferBuilder);
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.itemModelShaper.rebuildCache();
	}
}
