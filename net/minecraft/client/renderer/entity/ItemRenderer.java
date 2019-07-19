/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
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
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ItemRenderer
implements ResourceManagerReloadListener {
    public static final ResourceLocation ENCHANT_GLINT_LOCATION = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private static final Set<Item> IGNORED = Sets.newHashSet(Items.AIR);
    public float blitOffset;
    private final ItemModelShaper itemModelShaper;
    private final TextureManager textureManager;
    private final ItemColors itemColors;

    public ItemRenderer(TextureManager textureManager, ModelManager modelManager, ItemColors itemColors) {
        this.textureManager = textureManager;
        this.itemModelShaper = new ItemModelShaper(modelManager);
        for (Item item : Registry.ITEM) {
            if (IGNORED.contains(item)) continue;
            this.itemModelShaper.register(item, new ModelResourceLocation(Registry.ITEM.getKey(item), "inventory"));
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
        bufferBuilder.begin(7, DefaultVertexFormat.BLOCK_NORMALS);
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
        if (itemStack.isEmpty()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translatef(-0.5f, -0.5f, -0.5f);
        if (bakedModel.isCustomRenderer()) {
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableRescaleNormal();
            EntityBlockRenderer.instance.renderByItem(itemStack);
        } else {
            this.renderModelLists(bakedModel, itemStack);
            if (itemStack.hasFoil()) {
                ItemRenderer.renderFoilLayer(this.textureManager, () -> this.renderModelLists(bakedModel, -8372020), 8);
            }
        }
        GlStateManager.popMatrix();
    }

    public static void renderFoilLayer(TextureManager textureManager, Runnable runnable, int i) {
        GlStateManager.depthMask(false);
        GlStateManager.depthFunc(514);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
        textureManager.bind(ENCHANT_GLINT_LOCATION);
        GlStateManager.matrixMode(5890);
        GlStateManager.pushMatrix();
        GlStateManager.scalef(i, i, i);
        float f = (float)(Util.getMillis() % 3000L) / 3000.0f / (float)i;
        GlStateManager.translatef(f, 0.0f, 0.0f);
        GlStateManager.rotatef(-50.0f, 0.0f, 0.0f, 1.0f);
        runnable.run();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.scalef(i, i, i);
        float g = (float)(Util.getMillis() % 4873L) / 4873.0f / (float)i;
        GlStateManager.translatef(-g, 0.0f, 0.0f);
        GlStateManager.rotatef(10.0f, 0.0f, 0.0f, 1.0f);
        runnable.run();
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableLighting();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
    }

    private void applyNormal(BufferBuilder bufferBuilder, BakedQuad bakedQuad) {
        Vec3i vec3i = bakedQuad.getDirection().getNormal();
        bufferBuilder.postNormal(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    private void putQuadData(BufferBuilder bufferBuilder, BakedQuad bakedQuad, int i) {
        bufferBuilder.putBulkData(bakedQuad.getVertices());
        bufferBuilder.fixupQuadColor(i);
        this.applyNormal(bufferBuilder, bakedQuad);
    }

    private void renderQuadList(BufferBuilder bufferBuilder, List<BakedQuad> list, int i, ItemStack itemStack) {
        boolean bl = i == -1 && !itemStack.isEmpty();
        int k = list.size();
        for (int j = 0; j < k; ++j) {
            BakedQuad bakedQuad = list.get(j);
            int l = i;
            if (bl && bakedQuad.isTinted()) {
                l = this.itemColors.getColor(itemStack, bakedQuad.getTintIndex());
                l |= 0xFF000000;
            }
            this.putQuadData(bufferBuilder, bakedQuad, l);
        }
    }

    public boolean isGui3d(ItemStack itemStack) {
        BakedModel bakedModel = this.itemModelShaper.getItemModel(itemStack);
        if (bakedModel == null) {
            return false;
        }
        return bakedModel.isGui3d();
    }

    public void renderStatic(ItemStack itemStack, ItemTransforms.TransformType transformType) {
        if (itemStack.isEmpty()) {
            return;
        }
        BakedModel bakedModel = this.getModel(itemStack);
        this.renderStatic(itemStack, bakedModel, transformType, false);
    }

    public BakedModel getModel(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity) {
        BakedModel bakedModel = this.itemModelShaper.getItemModel(itemStack);
        Item item = itemStack.getItem();
        if (!item.hasProperties()) {
            return bakedModel;
        }
        return this.resolveOverrides(bakedModel, itemStack, level, livingEntity);
    }

    public BakedModel getInHandModel(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        Item item = itemStack.getItem();
        BakedModel bakedModel = item == Items.TRIDENT ? this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident_in_hand#inventory")) : this.itemModelShaper.getItemModel(itemStack);
        if (!item.hasProperties()) {
            return bakedModel;
        }
        return this.resolveOverrides(bakedModel, itemStack, level, livingEntity);
    }

    public BakedModel getModel(ItemStack itemStack) {
        return this.getModel(itemStack, null, null);
    }

    private BakedModel resolveOverrides(BakedModel bakedModel, ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity) {
        BakedModel bakedModel2 = bakedModel.getOverrides().resolve(bakedModel, itemStack, level, livingEntity);
        return bakedModel2 == null ? this.itemModelShaper.getModelManager().getMissingModel() : bakedModel2;
    }

    public void renderWithMobState(ItemStack itemStack, LivingEntity livingEntity, ItemTransforms.TransformType transformType, boolean bl) {
        if (itemStack.isEmpty() || livingEntity == null) {
            return;
        }
        BakedModel bakedModel = this.getInHandModel(itemStack, livingEntity.level, livingEntity);
        this.renderStatic(itemStack, bakedModel, transformType, bl);
    }

    protected void renderStatic(ItemStack itemStack, BakedModel bakedModel, ItemTransforms.TransformType transformType, boolean bl) {
        if (itemStack.isEmpty()) {
            return;
        }
        this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
        this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).pushFilter(false, false);
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        ItemTransforms itemTransforms = bakedModel.getTransforms();
        ItemTransforms.apply(itemTransforms.getTransform(transformType), bl);
        if (this.needsFlip(itemTransforms.getTransform(transformType))) {
            GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
        }
        this.render(itemStack, bakedModel);
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
        this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).popFilter();
    }

    private boolean needsFlip(ItemTransform itemTransform) {
        return itemTransform.scale.x() < 0.0f ^ itemTransform.scale.y() < 0.0f ^ itemTransform.scale.z() < 0.0f;
    }

    public void renderGuiItem(ItemStack itemStack, int i, int j) {
        this.renderGuiItem(itemStack, i, j, this.getModel(itemStack));
    }

    protected void renderGuiItem(ItemStack itemStack, int i, int j, BakedModel bakedModel) {
        GlStateManager.pushMatrix();
        this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
        this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).pushFilter(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.setupGuiItem(i, j, bakedModel.isGui3d());
        bakedModel.getTransforms().apply(ItemTransforms.TransformType.GUI);
        this.render(itemStack, bakedModel);
        GlStateManager.disableAlphaTest();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
        this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
        this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).popFilter();
    }

    private void setupGuiItem(int i, int j, boolean bl) {
        GlStateManager.translatef(i, j, 100.0f + this.blitOffset);
        GlStateManager.translatef(8.0f, 8.0f, 0.0f);
        GlStateManager.scalef(1.0f, -1.0f, 1.0f);
        GlStateManager.scalef(16.0f, 16.0f, 16.0f);
        if (bl) {
            GlStateManager.enableLighting();
        } else {
            GlStateManager.disableLighting();
        }
    }

    public void renderAndDecorateItem(ItemStack itemStack, int i, int j) {
        this.renderAndDecorateItem(Minecraft.getInstance().player, itemStack, i, j);
    }

    public void renderAndDecorateItem(@Nullable LivingEntity livingEntity, ItemStack itemStack, int i, int j) {
        if (itemStack.isEmpty()) {
            return;
        }
        this.blitOffset += 50.0f;
        try {
            this.renderGuiItem(itemStack, i, j, this.getModel(itemStack, null, livingEntity));
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering item");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
            crashReportCategory.setDetail("Item Type", () -> String.valueOf(itemStack.getItem()));
            crashReportCategory.setDetail("Item Damage", () -> String.valueOf(itemStack.getDamageValue()));
            crashReportCategory.setDetail("Item NBT", () -> String.valueOf(itemStack.getTag()));
            crashReportCategory.setDetail("Item Foil", () -> String.valueOf(itemStack.hasFoil()));
            throw new ReportedException(crashReport);
        }
        this.blitOffset -= 50.0f;
    }

    public void renderGuiItemDecorations(Font font, ItemStack itemStack, int i, int j) {
        this.renderGuiItemDecorations(font, itemStack, i, j, null);
    }

    public void renderGuiItemDecorations(Font font, ItemStack itemStack, int i, int j, @Nullable String string) {
        LocalPlayer localPlayer;
        float m;
        if (itemStack.isEmpty()) {
            return;
        }
        if (itemStack.getCount() != 1 || string != null) {
            String string2 = string == null ? String.valueOf(itemStack.getCount()) : string;
            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            GlStateManager.disableBlend();
            font.drawShadow(string2, i + 19 - 2 - font.width(string2), j + 6 + 3, 0xFFFFFF);
            GlStateManager.enableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableDepthTest();
        }
        if (itemStack.isDamaged()) {
            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            GlStateManager.disableTexture();
            GlStateManager.disableAlphaTest();
            GlStateManager.disableBlend();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();
            float f = itemStack.getDamageValue();
            float g = itemStack.getMaxDamage();
            float h = Math.max(0.0f, (g - f) / g);
            int k = Math.round(13.0f - f * 13.0f / g);
            int l = Mth.hsvToRgb(h / 3.0f, 1.0f, 1.0f);
            this.fillRect(bufferBuilder, i + 2, j + 13, 13, 2, 0, 0, 0, 255);
            this.fillRect(bufferBuilder, i + 2, j + 13, k, 1, l >> 16 & 0xFF, l >> 8 & 0xFF, l & 0xFF, 255);
            GlStateManager.enableBlend();
            GlStateManager.enableAlphaTest();
            GlStateManager.enableTexture();
            GlStateManager.enableLighting();
            GlStateManager.enableDepthTest();
        }
        float f = m = (localPlayer = Minecraft.getInstance().player) == null ? 0.0f : localPlayer.getCooldowns().getCooldownPercent(itemStack.getItem(), Minecraft.getInstance().getFrameTime());
        if (m > 0.0f) {
            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            GlStateManager.disableTexture();
            Tesselator tesselator2 = Tesselator.getInstance();
            BufferBuilder bufferBuilder2 = tesselator2.getBuilder();
            this.fillRect(bufferBuilder2, i, j + Mth.floor(16.0f * (1.0f - m)), 16, Mth.ceil(16.0f * m), 255, 255, 255, 127);
            GlStateManager.enableTexture();
            GlStateManager.enableLighting();
            GlStateManager.enableDepthTest();
        }
    }

    private void fillRect(BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, int o, int p) {
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(i + 0, j + 0, 0.0).color(m, n, o, p).endVertex();
        bufferBuilder.vertex(i + 0, j + l, 0.0).color(m, n, o, p).endVertex();
        bufferBuilder.vertex(i + k, j + l, 0.0).color(m, n, o, p).endVertex();
        bufferBuilder.vertex(i + k, j + 0, 0.0).color(m, n, o, p).endVertex();
        Tesselator.getInstance().end();
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.itemModelShaper.rebuildCache();
    }
}

