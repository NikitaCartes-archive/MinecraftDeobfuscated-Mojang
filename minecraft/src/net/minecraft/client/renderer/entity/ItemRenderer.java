package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.MatrixUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class ItemRenderer implements ResourceManagerReloadListener {
	public static final ResourceLocation ENCHANTED_GLINT_ENTITY = ResourceLocation.withDefaultNamespace("textures/misc/enchanted_glint_entity.png");
	public static final ResourceLocation ENCHANTED_GLINT_ITEM = ResourceLocation.withDefaultNamespace("textures/misc/enchanted_glint_item.png");
	public static final int GUI_SLOT_CENTER_X = 8;
	public static final int GUI_SLOT_CENTER_Y = 8;
	public static final int ITEM_COUNT_BLIT_OFFSET = 200;
	public static final float COMPASS_FOIL_UI_SCALE = 0.5F;
	public static final float COMPASS_FOIL_FIRST_PERSON_SCALE = 0.75F;
	public static final float COMPASS_FOIL_TEXTURE_SCALE = 0.0078125F;
	public static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.inventory(ResourceLocation.withDefaultNamespace("trident"));
	public static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.inventory(ResourceLocation.withDefaultNamespace("spyglass"));
	private final ModelManager modelManager;
	private final ItemModelShaper itemModelShaper;
	private final ItemColors itemColors;
	private final BlockEntityWithoutLevelRenderer blockEntityRenderer;

	public ItemRenderer(ModelManager modelManager, ItemColors itemColors, BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer) {
		this.modelManager = modelManager;
		this.itemModelShaper = new ItemModelShaper(modelManager);
		this.blockEntityRenderer = blockEntityWithoutLevelRenderer;
		this.itemColors = itemColors;
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
		ItemDisplayContext itemDisplayContext,
		boolean bl,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j,
		BakedModel bakedModel
	) {
		if (!itemStack.isEmpty()) {
			this.renderSimpleItemModel(itemStack, itemDisplayContext, bl, poseStack, multiBufferSource, i, j, bakedModel, shouldRenderItemFlat(itemDisplayContext));
		}
	}

	public void renderBundleItem(
		ItemStack itemStack,
		ItemDisplayContext itemDisplayContext,
		boolean bl,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j,
		BakedModel bakedModel,
		@Nullable Level level,
		@Nullable LivingEntity livingEntity,
		int k
	) {
		if (itemStack.getItem() instanceof BundleItem bundleItem) {
			if (BundleItem.hasSelectedItem(itemStack)) {
				boolean bl2 = shouldRenderItemFlat(itemDisplayContext);
				BakedModel bakedModel2 = this.resolveModelOverride(this.itemModelShaper.getItemModel(bundleItem.openBackModel()), itemStack, level, livingEntity, k);
				this.renderItemModelRaw(itemStack, itemDisplayContext, bl, poseStack, multiBufferSource, i, j, bakedModel2, bl2, -1.5F);
				ItemStack itemStack2 = BundleItem.getSelectedItemStack(itemStack);
				BakedModel bakedModel3 = this.getModel(itemStack2, level, livingEntity, k);
				this.renderSimpleItemModel(itemStack2, itemDisplayContext, bl, poseStack, multiBufferSource, i, j, bakedModel3, bl2);
				BakedModel bakedModel4 = this.resolveModelOverride(this.itemModelShaper.getItemModel(bundleItem.openFrontModel()), itemStack, level, livingEntity, k);
				this.renderItemModelRaw(itemStack, itemDisplayContext, bl, poseStack, multiBufferSource, i, j, bakedModel4, bl2, 0.5F);
			} else {
				this.render(itemStack, itemDisplayContext, bl, poseStack, multiBufferSource, i, j, bakedModel);
			}
		}
	}

	private void renderSimpleItemModel(
		ItemStack itemStack,
		ItemDisplayContext itemDisplayContext,
		boolean bl,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j,
		BakedModel bakedModel,
		boolean bl2
	) {
		if (bl2) {
			if (itemStack.is(Items.TRIDENT)) {
				bakedModel = this.modelManager.getModel(TRIDENT_MODEL);
			} else if (itemStack.is(Items.SPYGLASS)) {
				bakedModel = this.modelManager.getModel(SPYGLASS_MODEL);
			}
		}

		this.renderItemModelRaw(itemStack, itemDisplayContext, bl, poseStack, multiBufferSource, i, j, bakedModel, bl2, -0.5F);
	}

	private void renderItemModelRaw(
		ItemStack itemStack,
		ItemDisplayContext itemDisplayContext,
		boolean bl,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j,
		BakedModel bakedModel,
		boolean bl2,
		float f
	) {
		poseStack.pushPose();
		bakedModel.getTransforms().getTransform(itemDisplayContext).apply(bl, poseStack);
		poseStack.translate(-0.5F, -0.5F, f);
		this.renderItem(itemStack, itemDisplayContext, poseStack, multiBufferSource, i, j, bakedModel, bl2);
		poseStack.popPose();
	}

	private void renderItem(
		ItemStack itemStack,
		ItemDisplayContext itemDisplayContext,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j,
		BakedModel bakedModel,
		boolean bl
	) {
		if (!bakedModel.isCustomRenderer() && (!itemStack.is(Items.TRIDENT) || bl)) {
			RenderType renderType = ItemBlockRenderTypes.getRenderType(itemStack);
			VertexConsumer vertexConsumer;
			if (hasAnimatedTexture(itemStack) && itemStack.hasFoil()) {
				PoseStack.Pose pose = poseStack.last().copy();
				if (itemDisplayContext == ItemDisplayContext.GUI) {
					MatrixUtil.mulComponentWise(pose.pose(), 0.5F);
				} else if (itemDisplayContext.firstPerson()) {
					MatrixUtil.mulComponentWise(pose.pose(), 0.75F);
				}

				vertexConsumer = getCompassFoilBuffer(multiBufferSource, renderType, pose);
			} else {
				vertexConsumer = getFoilBuffer(multiBufferSource, renderType, true, itemStack.hasFoil());
			}

			this.renderModelLists(bakedModel, itemStack, i, j, poseStack, vertexConsumer);
		} else {
			this.blockEntityRenderer.renderByItem(itemStack, itemDisplayContext, poseStack, multiBufferSource, i, j);
		}
	}

	private static boolean shouldRenderItemFlat(ItemDisplayContext itemDisplayContext) {
		return itemDisplayContext == ItemDisplayContext.GUI || itemDisplayContext == ItemDisplayContext.GROUND || itemDisplayContext == ItemDisplayContext.FIXED;
	}

	private static boolean hasAnimatedTexture(ItemStack itemStack) {
		return itemStack.is(ItemTags.COMPASSES) || itemStack.is(Items.CLOCK);
	}

	public static VertexConsumer getArmorFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl) {
		return bl
			? VertexMultiConsumer.create(multiBufferSource.getBuffer(RenderType.armorEntityGlint()), multiBufferSource.getBuffer(renderType))
			: multiBufferSource.getBuffer(renderType);
	}

	public static VertexConsumer getCompassFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, PoseStack.Pose pose) {
		return VertexMultiConsumer.create(
			new SheetedDecalTextureGenerator(multiBufferSource.getBuffer(RenderType.glint()), pose, 0.0078125F), multiBufferSource.getBuffer(renderType)
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

	private void renderQuadList(PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, ItemStack itemStack, int i, int j) {
		boolean bl = !itemStack.isEmpty();
		PoseStack.Pose pose = poseStack.last();

		for (BakedQuad bakedQuad : list) {
			int k = -1;
			if (bl && bakedQuad.isTinted()) {
				k = this.itemColors.getColor(itemStack, bakedQuad.getTintIndex());
			}

			float f = (float)ARGB.alpha(k) / 255.0F;
			float g = (float)ARGB.red(k) / 255.0F;
			float h = (float)ARGB.green(k) / 255.0F;
			float l = (float)ARGB.blue(k) / 255.0F;
			vertexConsumer.putBulkData(pose, bakedQuad, g, h, l, f, i, j);
		}
	}

	public BakedModel getModel(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity, int i) {
		BakedModel bakedModel = this.itemModelShaper.getItemModel(itemStack);
		return this.resolveModelOverride(bakedModel, itemStack, level, livingEntity, i);
	}

	public void renderStatic(
		ItemStack itemStack,
		ItemDisplayContext itemDisplayContext,
		int i,
		int j,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		@Nullable Level level,
		int k
	) {
		this.renderStatic(null, itemStack, itemDisplayContext, false, poseStack, multiBufferSource, level, i, j, k);
	}

	public void renderStatic(
		@Nullable LivingEntity livingEntity,
		ItemStack itemStack,
		ItemDisplayContext itemDisplayContext,
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
			this.render(itemStack, itemDisplayContext, bl, poseStack, multiBufferSource, i, j, bakedModel);
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.itemModelShaper.invalidateCache();
	}

	@Nullable
	public BakedModel resolveItemModel(ItemStack itemStack, LivingEntity livingEntity, ItemDisplayContext itemDisplayContext) {
		return itemStack.isEmpty() ? null : this.getModel(itemStack, livingEntity.level(), livingEntity, livingEntity.getId() + itemDisplayContext.ordinal());
	}

	private BakedModel resolveModelOverride(BakedModel bakedModel, ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity, int i) {
		ClientLevel clientLevel = level instanceof ClientLevel ? (ClientLevel)level : null;
		BakedModel bakedModel2 = bakedModel.overrides().findOverride(itemStack, clientLevel, livingEntity, i);
		return bakedModel2 == null ? bakedModel : bakedModel2;
	}
}
