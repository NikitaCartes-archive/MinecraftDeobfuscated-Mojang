/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LoomScreen
extends AbstractContainerScreen<LoomMenu> {
    private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/loom.png");
    private static final int TOTAL_PATTERN_ROWS = (BannerPattern.COUNT - BannerPattern.PATTERN_ITEM_COUNT - 1 + 4 - 1) / 4;
    private ModelPart flag;
    @Nullable
    private List<Pair<BannerPattern, DyeColor>> resultBannerPatterns;
    private ItemStack bannerStack = ItemStack.EMPTY;
    private ItemStack dyeStack = ItemStack.EMPTY;
    private ItemStack patternStack = ItemStack.EMPTY;
    private boolean displayPatterns;
    private boolean displaySpecialPattern;
    private boolean hasMaxPatterns;
    private float scrollOffs;
    private boolean scrolling;
    private int startIndex = 1;

    public LoomScreen(LoomMenu loomMenu, Inventory inventory, Component component) {
        super(loomMenu, inventory, component);
        loomMenu.registerUpdateListener(this::containerChanged);
        this.titleLabelY -= 2;
    }

    @Override
    protected void init() {
        super.init();
        this.flag = this.minecraft.getEntityModels().bakeLayer(ModelLayers.BANNER).getChild("flag");
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        super.render(poseStack, i, j, f);
        this.renderTooltip(poseStack, i, j);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        this.renderBackground(poseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BG_LOCATION);
        int k = this.leftPos;
        int l = this.topPos;
        this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
        Slot slot = ((LoomMenu)this.menu).getBannerSlot();
        Slot slot2 = ((LoomMenu)this.menu).getDyeSlot();
        Slot slot3 = ((LoomMenu)this.menu).getPatternSlot();
        Slot slot4 = ((LoomMenu)this.menu).getResultSlot();
        if (!slot.hasItem()) {
            this.blit(poseStack, k + slot.x, l + slot.y, this.imageWidth, 0, 16, 16);
        }
        if (!slot2.hasItem()) {
            this.blit(poseStack, k + slot2.x, l + slot2.y, this.imageWidth + 16, 0, 16, 16);
        }
        if (!slot3.hasItem()) {
            this.blit(poseStack, k + slot3.x, l + slot3.y, this.imageWidth + 32, 0, 16, 16);
        }
        int m = (int)(41.0f * this.scrollOffs);
        this.blit(poseStack, k + 119, l + 13 + m, 232 + (this.displayPatterns ? 0 : 12), 0, 12, 15);
        Lighting.setupForFlatItems();
        if (this.resultBannerPatterns != null && !this.hasMaxPatterns) {
            MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
            poseStack.pushPose();
            poseStack.translate(k + 139, l + 52, 0.0);
            poseStack.scale(24.0f, -24.0f, 1.0f);
            poseStack.translate(0.5, 0.5, 0.5);
            float g = 0.6666667f;
            poseStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
            this.flag.xRot = 0.0f;
            this.flag.y = -32.0f;
            BannerRenderer.renderPatterns(poseStack, bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, this.resultBannerPatterns);
            poseStack.popPose();
            bufferSource.endBatch();
        } else if (this.hasMaxPatterns) {
            this.blit(poseStack, k + slot4.x - 2, l + slot4.y - 2, this.imageWidth, 17, 17, 16);
        }
        if (this.displayPatterns) {
            int n = k + 60;
            int o = l + 13;
            int p = this.startIndex + 16;
            for (int q = this.startIndex; q < p && q < BannerPattern.COUNT - BannerPattern.PATTERN_ITEM_COUNT; ++q) {
                int r = q - this.startIndex;
                int s = n + r % 4 * 14;
                int t = o + r / 4 * 14;
                RenderSystem.setShaderTexture(0, BG_LOCATION);
                int u = this.imageHeight;
                if (q == ((LoomMenu)this.menu).getSelectedBannerPatternIndex()) {
                    u += 14;
                } else if (i >= s && j >= t && i < s + 14 && j < t + 14) {
                    u += 28;
                }
                this.blit(poseStack, s, t, 0, u, 14, 14);
                this.renderPattern(q, s, t);
            }
        } else if (this.displaySpecialPattern) {
            int n = k + 60;
            int o = l + 13;
            RenderSystem.setShaderTexture(0, BG_LOCATION);
            this.blit(poseStack, n, o, 0, this.imageHeight, 14, 14);
            int p = ((LoomMenu)this.menu).getSelectedBannerPatternIndex();
            this.renderPattern(p, n, o);
        }
        Lighting.setupFor3DItems();
    }

    private void renderPattern(int i, int j, int k) {
        ItemStack itemStack = new ItemStack(Items.GRAY_BANNER);
        CompoundTag compoundTag = itemStack.getOrCreateTagElement("BlockEntityTag");
        ListTag listTag = new BannerPattern.Builder().addPattern(BannerPattern.BASE, DyeColor.GRAY).addPattern(BannerPattern.values()[i], DyeColor.WHITE).toListTag();
        compoundTag.put("Patterns", listTag);
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate((float)j + 0.5f, k + 16, 0.0);
        poseStack.scale(6.0f, -6.0f, 1.0f);
        poseStack.translate(0.5, 0.5, 0.0);
        poseStack.translate(0.5, 0.5, 0.5);
        float f = 0.6666667f;
        poseStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        this.flag.xRot = 0.0f;
        this.flag.y = -32.0f;
        List<Pair<BannerPattern, DyeColor>> list = BannerBlockEntity.createPatterns(DyeColor.GRAY, BannerBlockEntity.getItemPatterns(itemStack));
        BannerRenderer.renderPatterns(poseStack, bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, list);
        poseStack.popPose();
        bufferSource.endBatch();
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        this.scrolling = false;
        if (this.displayPatterns) {
            int j = this.leftPos + 60;
            int k = this.topPos + 13;
            int l = this.startIndex + 16;
            for (int m = this.startIndex; m < l; ++m) {
                int n = m - this.startIndex;
                double f = d - (double)(j + n % 4 * 14);
                double g = e - (double)(k + n / 4 * 14);
                if (!(f >= 0.0) || !(g >= 0.0) || !(f < 14.0) || !(g < 14.0) || !((LoomMenu)this.menu).clickMenuButton(this.minecraft.player, m)) continue;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0f));
                this.minecraft.gameMode.handleInventoryButtonClick(((LoomMenu)this.menu).containerId, m);
                return true;
            }
            j = this.leftPos + 119;
            k = this.topPos + 9;
            if (d >= (double)j && d < (double)(j + 12) && e >= (double)k && e < (double)(k + 56)) {
                this.scrolling = true;
            }
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (this.scrolling && this.displayPatterns) {
            int j = this.topPos + 13;
            int k = j + 56;
            this.scrollOffs = ((float)e - (float)j - 7.5f) / ((float)(k - j) - 15.0f);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0f, 1.0f);
            int l = TOTAL_PATTERN_ROWS - 4;
            int m = (int)((double)(this.scrollOffs * (float)l) + 0.5);
            if (m < 0) {
                m = 0;
            }
            this.startIndex = 1 + m * 4;
            return true;
        }
        return super.mouseDragged(d, e, i, f, g);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (this.displayPatterns) {
            int i = TOTAL_PATTERN_ROWS - 4;
            this.scrollOffs = (float)((double)this.scrollOffs - f / (double)i);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0f, 1.0f);
            this.startIndex = 1 + (int)((double)(this.scrollOffs * (float)i) + 0.5) * 4;
        }
        return true;
    }

    @Override
    protected boolean hasClickedOutside(double d, double e, int i, int j, int k) {
        return d < (double)i || e < (double)j || d >= (double)(i + this.imageWidth) || e >= (double)(j + this.imageHeight);
    }

    private void containerChanged() {
        ItemStack itemStack = ((LoomMenu)this.menu).getResultSlot().getItem();
        this.resultBannerPatterns = itemStack.isEmpty() ? null : BannerBlockEntity.createPatterns(((BannerItem)itemStack.getItem()).getColor(), BannerBlockEntity.getItemPatterns(itemStack));
        ItemStack itemStack2 = ((LoomMenu)this.menu).getBannerSlot().getItem();
        ItemStack itemStack3 = ((LoomMenu)this.menu).getDyeSlot().getItem();
        ItemStack itemStack4 = ((LoomMenu)this.menu).getPatternSlot().getItem();
        CompoundTag compoundTag = itemStack2.getOrCreateTagElement("BlockEntityTag");
        boolean bl = this.hasMaxPatterns = compoundTag.contains("Patterns", 9) && !itemStack2.isEmpty() && compoundTag.getList("Patterns", 10).size() >= 6;
        if (this.hasMaxPatterns) {
            this.resultBannerPatterns = null;
        }
        if (!(ItemStack.matches(itemStack2, this.bannerStack) && ItemStack.matches(itemStack3, this.dyeStack) && ItemStack.matches(itemStack4, this.patternStack))) {
            this.displayPatterns = !itemStack2.isEmpty() && !itemStack3.isEmpty() && itemStack4.isEmpty() && !this.hasMaxPatterns;
            this.displaySpecialPattern = !this.hasMaxPatterns && !itemStack4.isEmpty() && !itemStack2.isEmpty() && !itemStack3.isEmpty();
        }
        this.bannerStack = itemStack2.copy();
        this.dyeStack = itemStack3.copy();
        this.patternStack = itemStack4.copy();
    }
}

