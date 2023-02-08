/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.MatrixUtil;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ItemRenderer
implements ResourceManagerReloadListener {
    public static final ResourceLocation ENCHANT_GLINT_LOCATION = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private static final Set<Item> IGNORED = Sets.newHashSet(Items.AIR);
    private static final int GUI_SLOT_CENTER_X = 8;
    private static final int GUI_SLOT_CENTER_Y = 8;
    public static final int ITEM_COUNT_BLIT_OFFSET = 200;
    public static final float COMPASS_FOIL_UI_SCALE = 0.5f;
    public static final float COMPASS_FOIL_FIRST_PERSON_SCALE = 0.75f;
    public static final float COMPASS_FOIL_TEXTURE_SCALE = 0.0078125f;
    private static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.vanilla("trident", "inventory");
    public static final ModelResourceLocation TRIDENT_IN_HAND_MODEL = ModelResourceLocation.vanilla("trident_in_hand", "inventory");
    private static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.vanilla("spyglass", "inventory");
    public static final ModelResourceLocation SPYGLASS_IN_HAND_MODEL = ModelResourceLocation.vanilla("spyglass_in_hand", "inventory");
    public float blitOffset;
    private final Minecraft minecraft;
    private final ItemModelShaper itemModelShaper;
    private final TextureManager textureManager;
    private final ItemColors itemColors;
    private final BlockEntityWithoutLevelRenderer blockEntityRenderer;

    public ItemRenderer(Minecraft minecraft, TextureManager textureManager, ModelManager modelManager, ItemColors itemColors, BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer) {
        this.minecraft = minecraft;
        this.textureManager = textureManager;
        this.itemModelShaper = new ItemModelShaper(modelManager);
        this.blockEntityRenderer = blockEntityWithoutLevelRenderer;
        for (Item item : BuiltInRegistries.ITEM) {
            if (IGNORED.contains(item)) continue;
            this.itemModelShaper.register(item, new ModelResourceLocation(BuiltInRegistries.ITEM.getKey(item), "inventory"));
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

    public void render(ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel) {
        boolean bl2;
        if (itemStack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        boolean bl3 = bl2 = itemDisplayContext == ItemDisplayContext.GUI || itemDisplayContext == ItemDisplayContext.GROUND || itemDisplayContext == ItemDisplayContext.FIXED;
        if (bl2) {
            if (itemStack.is(Items.TRIDENT)) {
                bakedModel = this.itemModelShaper.getModelManager().getModel(TRIDENT_MODEL);
            } else if (itemStack.is(Items.SPYGLASS)) {
                bakedModel = this.itemModelShaper.getModelManager().getModel(SPYGLASS_MODEL);
            }
        }
        bakedModel.getTransforms().getTransform(itemDisplayContext).apply(bl, poseStack);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        if (bakedModel.isCustomRenderer() || itemStack.is(Items.TRIDENT) && !bl2) {
            this.blockEntityRenderer.renderByItem(itemStack, itemDisplayContext, poseStack, multiBufferSource, i, j);
        } else {
            VertexConsumer vertexConsumer;
            Block block;
            boolean bl32 = itemDisplayContext != ItemDisplayContext.GUI && !itemDisplayContext.firstPerson() && itemStack.getItem() instanceof BlockItem ? !((block = ((BlockItem)itemStack.getItem()).getBlock()) instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock) : true;
            RenderType renderType = ItemBlockRenderTypes.getRenderType(itemStack, bl32);
            if (itemStack.is(ItemTags.COMPASSES) && itemStack.hasFoil()) {
                poseStack.pushPose();
                PoseStack.Pose pose = poseStack.last();
                if (itemDisplayContext == ItemDisplayContext.GUI) {
                    MatrixUtil.mulComponentWise(pose.pose(), 0.5f);
                } else if (itemDisplayContext.firstPerson()) {
                    MatrixUtil.mulComponentWise(pose.pose(), 0.75f);
                }
                vertexConsumer = bl32 ? ItemRenderer.getCompassFoilBufferDirect(multiBufferSource, renderType, pose) : ItemRenderer.getCompassFoilBuffer(multiBufferSource, renderType, pose);
                poseStack.popPose();
            } else {
                vertexConsumer = bl32 ? ItemRenderer.getFoilBufferDirect(multiBufferSource, renderType, true, itemStack.hasFoil()) : ItemRenderer.getFoilBuffer(multiBufferSource, renderType, true, itemStack.hasFoil());
            }
            this.renderModelLists(bakedModel, itemStack, i, j, poseStack, vertexConsumer);
        }
        poseStack.popPose();
    }

    public static VertexConsumer getArmorFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl, boolean bl2) {
        if (bl2) {
            return VertexMultiConsumer.create(multiBufferSource.getBuffer(bl ? RenderType.armorGlint() : RenderType.armorEntityGlint()), multiBufferSource.getBuffer(renderType));
        }
        return multiBufferSource.getBuffer(renderType);
    }

    public static VertexConsumer getCompassFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, PoseStack.Pose pose) {
        return VertexMultiConsumer.create((VertexConsumer)new SheetedDecalTextureGenerator(multiBufferSource.getBuffer(RenderType.glint()), pose.pose(), pose.normal(), 0.0078125f), multiBufferSource.getBuffer(renderType));
    }

    public static VertexConsumer getCompassFoilBufferDirect(MultiBufferSource multiBufferSource, RenderType renderType, PoseStack.Pose pose) {
        return VertexMultiConsumer.create((VertexConsumer)new SheetedDecalTextureGenerator(multiBufferSource.getBuffer(RenderType.glintDirect()), pose.pose(), pose.normal(), 0.0078125f), multiBufferSource.getBuffer(renderType));
    }

    public static VertexConsumer getFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl, boolean bl2) {
        if (bl2) {
            if (Minecraft.useShaderTransparency() && renderType == Sheets.translucentItemSheet()) {
                return VertexMultiConsumer.create(multiBufferSource.getBuffer(RenderType.glintTranslucent()), multiBufferSource.getBuffer(renderType));
            }
            return VertexMultiConsumer.create(multiBufferSource.getBuffer(bl ? RenderType.glint() : RenderType.entityGlint()), multiBufferSource.getBuffer(renderType));
        }
        return multiBufferSource.getBuffer(renderType);
    }

    public static VertexConsumer getFoilBufferDirect(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl, boolean bl2) {
        if (bl2) {
            return VertexMultiConsumer.create(multiBufferSource.getBuffer(bl ? RenderType.glintDirect() : RenderType.entityGlintDirect()), multiBufferSource.getBuffer(renderType));
        }
        return multiBufferSource.getBuffer(renderType);
    }

    private void renderQuadList(PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, ItemStack itemStack, int i, int j) {
        boolean bl = !itemStack.isEmpty();
        PoseStack.Pose pose = poseStack.last();
        for (BakedQuad bakedQuad : list) {
            int k = -1;
            if (bl && bakedQuad.isTinted()) {
                k = this.itemColors.getColor(itemStack, bakedQuad.getTintIndex());
            }
            float f = (float)(k >> 16 & 0xFF) / 255.0f;
            float g = (float)(k >> 8 & 0xFF) / 255.0f;
            float h = (float)(k & 0xFF) / 255.0f;
            vertexConsumer.putBulkData(pose, bakedQuad, f, g, h, i, j);
        }
    }

    public BakedModel getModel(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity, int i) {
        BakedModel bakedModel = itemStack.is(Items.TRIDENT) ? this.itemModelShaper.getModelManager().getModel(TRIDENT_IN_HAND_MODEL) : (itemStack.is(Items.SPYGLASS) ? this.itemModelShaper.getModelManager().getModel(SPYGLASS_IN_HAND_MODEL) : this.itemModelShaper.getItemModel(itemStack));
        ClientLevel clientLevel = level instanceof ClientLevel ? (ClientLevel)level : null;
        BakedModel bakedModel2 = bakedModel.getOverrides().resolve(bakedModel, itemStack, clientLevel, livingEntity, i);
        return bakedModel2 == null ? this.itemModelShaper.getModelManager().getMissingModel() : bakedModel2;
    }

    public void renderStatic(ItemStack itemStack, ItemDisplayContext itemDisplayContext, int i, int j, PoseStack poseStack, MultiBufferSource multiBufferSource, @Nullable Level level, int k) {
        this.renderStatic(null, itemStack, itemDisplayContext, false, poseStack, multiBufferSource, level, i, j, k);
    }

    public void renderStatic(@Nullable LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, @Nullable Level level, int i, int j, int k) {
        if (itemStack.isEmpty()) {
            return;
        }
        BakedModel bakedModel = this.getModel(itemStack, level, livingEntity, k);
        this.render(itemStack, itemDisplayContext, bl, poseStack, multiBufferSource, i, j, bakedModel);
    }

    public void renderGuiItem(ItemStack itemStack, int i, int j) {
        this.renderGuiItem(itemStack, i, j, this.getModel(itemStack, null, null, 0));
    }

    protected void renderGuiItem(ItemStack itemStack, int i, int j, BakedModel bakedModel) {
        boolean bl;
        this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(i, j, 100.0f + this.blitOffset);
        poseStack.translate(8.0f, 8.0f, 0.0f);
        poseStack.scale(1.0f, -1.0f, 1.0f);
        poseStack.scale(16.0f, 16.0f, 16.0f);
        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack2 = new PoseStack();
        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        boolean bl2 = bl = !bakedModel.usesBlockLight();
        if (bl) {
            Lighting.setupForFlatItems();
        }
        this.render(itemStack, ItemDisplayContext.GUI, false, poseStack2, bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, bakedModel);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if (bl) {
            Lighting.setupFor3DItems();
        }
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public void renderAndDecorateItem(ItemStack itemStack, int i, int j) {
        this.tryRenderGuiItem(this.minecraft.player, this.minecraft.level, itemStack, i, j, 0);
    }

    public void renderAndDecorateItem(ItemStack itemStack, int i, int j, int k) {
        this.tryRenderGuiItem(this.minecraft.player, this.minecraft.level, itemStack, i, j, k);
    }

    public void renderAndDecorateItem(ItemStack itemStack, int i, int j, int k, int l) {
        this.tryRenderGuiItem(this.minecraft.player, this.minecraft.level, itemStack, i, j, k, l);
    }

    public void renderAndDecorateFakeItem(ItemStack itemStack, int i, int j) {
        this.tryRenderGuiItem(null, this.minecraft.level, itemStack, i, j, 0);
    }

    public void renderAndDecorateItem(LivingEntity livingEntity, ItemStack itemStack, int i, int j, int k) {
        this.tryRenderGuiItem(livingEntity, livingEntity.level, itemStack, i, j, k);
    }

    private void tryRenderGuiItem(@Nullable LivingEntity livingEntity, @Nullable Level level, ItemStack itemStack, int i, int j, int k) {
        this.tryRenderGuiItem(livingEntity, level, itemStack, i, j, k, 0);
    }

    private void tryRenderGuiItem(@Nullable LivingEntity livingEntity, @Nullable Level level, ItemStack itemStack, int i, int j, int k, int l) {
        if (itemStack.isEmpty()) {
            return;
        }
        BakedModel bakedModel = this.getModel(itemStack, level, livingEntity, k);
        this.blitOffset = bakedModel.isGui3d() ? this.blitOffset + 50.0f + (float)l : this.blitOffset + 50.0f;
        try {
            this.renderGuiItem(itemStack, i, j, bakedModel);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering item");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
            crashReportCategory.setDetail("Item Type", () -> String.valueOf(itemStack.getItem()));
            crashReportCategory.setDetail("Item Damage", () -> String.valueOf(itemStack.getDamageValue()));
            crashReportCategory.setDetail("Item NBT", () -> String.valueOf(itemStack.getTag()));
            crashReportCategory.setDetail("Item Foil", () -> String.valueOf(itemStack.hasFoil()));
            throw new ReportedException(crashReport);
        }
        this.blitOffset = bakedModel.isGui3d() ? this.blitOffset - 50.0f - (float)l : this.blitOffset - 50.0f;
    }

    public void renderGuiItemDecorations(Font font, ItemStack itemStack, int i, int j) {
        this.renderGuiItemDecorations(font, itemStack, i, j, null);
    }

    public void renderGuiItemDecorations(Font font, ItemStack itemStack, int i, int j, @Nullable String string) {
        LocalPlayer localPlayer;
        float f;
        int n;
        int m;
        if (itemStack.isEmpty()) {
            return;
        }
        PoseStack poseStack = new PoseStack();
        if (itemStack.getCount() != 1 || string != null) {
            String string2 = string == null ? String.valueOf(itemStack.getCount()) : string;
            poseStack.translate(0.0f, 0.0f, this.blitOffset + 200.0f);
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            font.drawInBatch(string2, (float)(i + 19 - 2 - font.width(string2)), (float)(j + 6 + 3), 0xFFFFFF, true, poseStack.last().pose(), (MultiBufferSource)bufferSource, false, 0, 0xF000F0);
            bufferSource.endBatch();
        }
        if (itemStack.isBarVisible()) {
            RenderSystem.disableDepthTest();
            int k = itemStack.getBarWidth();
            int l = itemStack.getBarColor();
            m = i + 2;
            n = j + 13;
            GuiComponent.fill(poseStack, m, n, m + 13, n + 2, -16777216);
            GuiComponent.fill(poseStack, m, n, m + k, n + 1, l | 0xFF000000);
            RenderSystem.enableDepthTest();
        }
        float f2 = f = (localPlayer = this.minecraft.player) == null ? 0.0f : localPlayer.getCooldowns().getCooldownPercent(itemStack.getItem(), this.minecraft.getFrameTime());
        if (f > 0.0f) {
            RenderSystem.disableDepthTest();
            m = j + Mth.floor(16.0f * (1.0f - f));
            n = m + Mth.ceil(16.0f * f);
            GuiComponent.fill(poseStack, i, m, i + 16, n, Integer.MAX_VALUE);
            RenderSystem.enableDepthTest();
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.itemModelShaper.rebuildCache();
    }
}

