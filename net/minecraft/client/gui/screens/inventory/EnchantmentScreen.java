/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class EnchantmentScreen
extends AbstractContainerScreen<EnchantmentMenu> {
    private static final ResourceLocation ENCHANTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/enchanting_table.png");
    private static final ResourceLocation ENCHANTING_BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private final RandomSource random = RandomSource.create();
    private BookModel bookModel;
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    private ItemStack last = ItemStack.EMPTY;

    public EnchantmentScreen(EnchantmentMenu enchantmentMenu, Inventory inventory, Component component) {
        super(enchantmentMenu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.tickBook();
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        int j = (this.width - this.imageWidth) / 2;
        int k = (this.height - this.imageHeight) / 2;
        for (int l = 0; l < 3; ++l) {
            double f = d - (double)(j + 60);
            double g = e - (double)(k + 14 + 19 * l);
            if (!(f >= 0.0) || !(g >= 0.0) || !(f < 108.0) || !(g < 19.0) || !((EnchantmentMenu)this.menu).clickMenuButton(this.minecraft.player, l)) continue;
            this.minecraft.gameMode.handleInventoryButtonClick(((EnchantmentMenu)this.menu).containerId, l);
            return true;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        Lighting.setupForFlatItems();
        RenderSystem.setShaderTexture(0, ENCHANTING_TABLE_LOCATION);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        EnchantmentScreen.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
        int m = (int)this.minecraft.getWindow().getGuiScale();
        RenderSystem.viewport((this.width - 320) / 2 * m, (this.height - 240) / 2 * m, 320 * m, 240 * m);
        Matrix4f matrix4f = new Matrix4f().translation(-0.34f, 0.23f, 0.0f).perspective(1.5707964f, 1.3333334f, 9.0f, 80.0f);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(matrix4f);
        poseStack.pushPose();
        poseStack.setIdentity();
        poseStack.translate(0.0f, 3.3f, 1984.0f);
        float g = 5.0f;
        poseStack.scale(5.0f, 5.0f, 5.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(20.0f));
        float h = Mth.lerp(f, this.oOpen, this.open);
        poseStack.translate((1.0f - h) * 0.2f, (1.0f - h) * 0.1f, (1.0f - h) * 0.25f);
        float n = -(1.0f - h) * 90.0f - 90.0f;
        poseStack.mulPose(Axis.YP.rotationDegrees(n));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        float o = Mth.lerp(f, this.oFlip, this.flip) + 0.25f;
        float p = Mth.lerp(f, this.oFlip, this.flip) + 0.75f;
        o = (o - (float)Mth.floor(o)) * 1.6f - 0.3f;
        p = (p - (float)Mth.floor(p)) * 1.6f - 0.3f;
        if (o < 0.0f) {
            o = 0.0f;
        }
        if (p < 0.0f) {
            p = 0.0f;
        }
        if (o > 1.0f) {
            o = 1.0f;
        }
        if (p > 1.0f) {
            p = 1.0f;
        }
        this.bookModel.setupAnim(0.0f, o, p, h);
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.bookModel.renderType(ENCHANTING_BOOK_LOCATION));
        this.bookModel.renderToBuffer(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        bufferSource.endBatch();
        poseStack.popPose();
        RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
        RenderSystem.restoreProjectionMatrix();
        Lighting.setupFor3DItems();
        EnchantmentNames.getInstance().initSeed(((EnchantmentMenu)this.menu).getEnchantmentSeed());
        int q = ((EnchantmentMenu)this.menu).getGoldCount();
        for (int r = 0; r < 3; ++r) {
            int s = k + 60;
            int t = s + 20;
            RenderSystem.setShaderTexture(0, ENCHANTING_TABLE_LOCATION);
            int u = ((EnchantmentMenu)this.menu).costs[r];
            if (u == 0) {
                EnchantmentScreen.blit(poseStack, s, l + 14 + 19 * r, 0, 185, 108, 19);
                continue;
            }
            String string = "" + u;
            int v = 86 - this.font.width(string);
            FormattedText formattedText = EnchantmentNames.getInstance().getRandomName(this.font, v);
            int w = 6839882;
            if (!(q >= r + 1 && this.minecraft.player.experienceLevel >= u || this.minecraft.player.getAbilities().instabuild)) {
                EnchantmentScreen.blit(poseStack, s, l + 14 + 19 * r, 0, 185, 108, 19);
                EnchantmentScreen.blit(poseStack, s + 1, l + 15 + 19 * r, 16 * r, 239, 16, 16);
                this.font.drawWordWrap(poseStack, formattedText, t, l + 16 + 19 * r, v, (w & 0xFEFEFE) >> 1);
                w = 4226832;
            } else {
                int x = i - (k + 60);
                int y = j - (l + 14 + 19 * r);
                if (x >= 0 && y >= 0 && x < 108 && y < 19) {
                    EnchantmentScreen.blit(poseStack, s, l + 14 + 19 * r, 0, 204, 108, 19);
                    w = 0xFFFF80;
                } else {
                    EnchantmentScreen.blit(poseStack, s, l + 14 + 19 * r, 0, 166, 108, 19);
                }
                EnchantmentScreen.blit(poseStack, s + 1, l + 15 + 19 * r, 16 * r, 223, 16, 16);
                this.font.drawWordWrap(poseStack, formattedText, t, l + 16 + 19 * r, v, w);
                w = 8453920;
            }
            this.font.drawShadow(poseStack, string, (float)(t + 86 - this.font.width(string)), (float)(l + 16 + 19 * r + 7), w);
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        f = this.minecraft.getFrameTime();
        this.renderBackground(poseStack);
        super.render(poseStack, i, j, f);
        this.renderTooltip(poseStack, i, j);
        boolean bl = this.minecraft.player.getAbilities().instabuild;
        int k = ((EnchantmentMenu)this.menu).getGoldCount();
        for (int l = 0; l < 3; ++l) {
            int m = ((EnchantmentMenu)this.menu).costs[l];
            Enchantment enchantment = Enchantment.byId(((EnchantmentMenu)this.menu).enchantClue[l]);
            int n = ((EnchantmentMenu)this.menu).levelClue[l];
            int o = l + 1;
            if (!this.isHovering(60, 14 + 19 * l, 108, 17, i, j) || m <= 0 || n < 0 || enchantment == null) continue;
            ArrayList<Component> list = Lists.newArrayList();
            list.add(Component.translatable("container.enchant.clue", enchantment.getFullname(n)).withStyle(ChatFormatting.WHITE));
            if (!bl) {
                list.add(CommonComponents.EMPTY);
                if (this.minecraft.player.experienceLevel < m) {
                    list.add(Component.translatable("container.enchant.level.requirement", ((EnchantmentMenu)this.menu).costs[l]).withStyle(ChatFormatting.RED));
                } else {
                    MutableComponent mutableComponent = o == 1 ? Component.translatable("container.enchant.lapis.one") : Component.translatable("container.enchant.lapis.many", o);
                    list.add(mutableComponent.withStyle(k >= o ? ChatFormatting.GRAY : ChatFormatting.RED));
                    MutableComponent mutableComponent2 = o == 1 ? Component.translatable("container.enchant.level.one") : Component.translatable("container.enchant.level.many", o);
                    list.add(mutableComponent2.withStyle(ChatFormatting.GRAY));
                }
            }
            this.renderComponentTooltip(poseStack, list, i, j);
            break;
        }
    }

    public void tickBook() {
        ItemStack itemStack = ((EnchantmentMenu)this.menu).getSlot(0).getItem();
        if (!ItemStack.matches(itemStack, this.last)) {
            this.last = itemStack;
            do {
                this.flipT += (float)(this.random.nextInt(4) - this.random.nextInt(4));
            } while (this.flip <= this.flipT + 1.0f && this.flip >= this.flipT - 1.0f);
        }
        ++this.time;
        this.oFlip = this.flip;
        this.oOpen = this.open;
        boolean bl = false;
        for (int i = 0; i < 3; ++i) {
            if (((EnchantmentMenu)this.menu).costs[i] == 0) continue;
            bl = true;
        }
        this.open = bl ? (this.open += 0.2f) : (this.open -= 0.2f);
        this.open = Mth.clamp(this.open, 0.0f, 1.0f);
        float f = (this.flipT - this.flip) * 0.4f;
        float g = 0.2f;
        f = Mth.clamp(f, -0.2f, 0.2f);
        this.flipA += (f - this.flipA) * 0.9f;
        this.flip += this.flipA;
    }
}

