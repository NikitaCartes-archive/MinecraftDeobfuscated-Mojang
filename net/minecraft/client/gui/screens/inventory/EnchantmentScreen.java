/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.ArrayList;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

@Environment(value=EnvType.CLIENT)
public class EnchantmentScreen
extends AbstractContainerScreen<EnchantmentMenu> {
    private static final ResourceLocation ENCHANTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/enchanting_table.png");
    private static final ResourceLocation ENCHANTING_BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private static final BookModel BOOK_MODEL = new BookModel();
    private final Random random = new Random();
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
    protected void renderLabels(int i, int j) {
        this.font.draw(this.title.getColoredString(), 12.0f, 5.0f, 0x404040);
        this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0f, this.imageHeight - 96 + 2, 0x404040);
    }

    @Override
    public void tick() {
        super.tick();
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
    protected void renderBg(float f, int i, int j) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(ENCHANTING_TABLE_LOCATION);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
        RenderSystem.matrixMode(5889);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        int m = (int)this.minecraft.getWindow().getGuiScale();
        RenderSystem.viewport((this.width - 320) / 2 * m, (this.height - 240) / 2 * m, 320 * m, 240 * m);
        RenderSystem.translatef(-0.34f, 0.23f, 0.0f);
        RenderSystem.multMatrix(Matrix4f.perspective(90.0, 1.3333334f, 9.0f, 80.0f));
        RenderSystem.matrixMode(5888);
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.getPose().setIdentity();
        poseStack.translate(0.0, 3.3f, 1984.0);
        float g = 5.0f;
        poseStack.scale(5.0f, 5.0f, 5.0f);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(20.0f));
        float h = Mth.lerp(f, this.oOpen, this.open);
        poseStack.translate((1.0f - h) * 0.2f, (1.0f - h) * 0.1f, (1.0f - h) * 0.25f);
        float n = -(1.0f - h) * 90.0f - 90.0f;
        poseStack.mulPose(Vector3f.YP.rotationDegrees(n));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0f));
        float o = Mth.lerp(f, this.oFlip, this.flip) + 0.25f;
        float p = Mth.lerp(f, this.oFlip, this.flip) + 0.75f;
        o = (o - (float)Mth.fastFloor(o)) * 1.6f - 0.3f;
        p = (p - (float)Mth.fastFloor(p)) * 1.6f - 0.3f;
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
        RenderSystem.enableRescaleNormal();
        BOOK_MODEL.setupAnim(0.0f, o, p, h);
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer vertexConsumer = bufferSource.getBuffer(BOOK_MODEL.renderType(ENCHANTING_BOOK_LOCATION));
        BOOK_MODEL.renderToBuffer(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f);
        bufferSource.endBatch();
        poseStack.popPose();
        RenderSystem.matrixMode(5889);
        RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        EnchantmentNames.getInstance().initSeed(((EnchantmentMenu)this.menu).getEnchantmentSeed());
        int q = ((EnchantmentMenu)this.menu).getGoldCount();
        for (int r = 0; r < 3; ++r) {
            int s = k + 60;
            int t = s + 20;
            this.setBlitOffset(0);
            this.minecraft.getTextureManager().bind(ENCHANTING_TABLE_LOCATION);
            int u = ((EnchantmentMenu)this.menu).costs[r];
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            if (u == 0) {
                this.blit(s, l + 14 + 19 * r, 0, 185, 108, 19);
                continue;
            }
            String string = "" + u;
            int v = 86 - this.font.width(string);
            String string2 = EnchantmentNames.getInstance().getRandomName(this.font, v);
            Font font = this.minecraft.getFontManager().get(Minecraft.ALT_FONT);
            int w = 6839882;
            if (!(q >= r + 1 && this.minecraft.player.experienceLevel >= u || this.minecraft.player.abilities.instabuild)) {
                this.blit(s, l + 14 + 19 * r, 0, 185, 108, 19);
                this.blit(s + 1, l + 15 + 19 * r, 16 * r, 239, 16, 16);
                font.drawWordWrap(string2, t, l + 16 + 19 * r, v, (w & 0xFEFEFE) >> 1);
                w = 4226832;
            } else {
                int x = i - (k + 60);
                int y = j - (l + 14 + 19 * r);
                if (x >= 0 && y >= 0 && x < 108 && y < 19) {
                    this.blit(s, l + 14 + 19 * r, 0, 204, 108, 19);
                    w = 0xFFFF80;
                } else {
                    this.blit(s, l + 14 + 19 * r, 0, 166, 108, 19);
                }
                this.blit(s + 1, l + 15 + 19 * r, 16 * r, 223, 16, 16);
                font.drawWordWrap(string2, t, l + 16 + 19 * r, v, w);
                w = 8453920;
            }
            font = this.minecraft.font;
            font.drawShadow(string, t + 86 - font.width(string), l + 16 + 19 * r + 7, w);
        }
    }

    @Override
    public void render(int i, int j, float f) {
        f = this.minecraft.getFrameTime();
        this.renderBackground();
        super.render(i, j, f);
        this.renderTooltip(i, j);
        boolean bl = this.minecraft.player.abilities.instabuild;
        int k = ((EnchantmentMenu)this.menu).getGoldCount();
        for (int l = 0; l < 3; ++l) {
            int m = ((EnchantmentMenu)this.menu).costs[l];
            Enchantment enchantment = Enchantment.byId(((EnchantmentMenu)this.menu).enchantClue[l]);
            int n = ((EnchantmentMenu)this.menu).levelClue[l];
            int o = l + 1;
            if (!this.isHovering(60, 14 + 19 * l, 108, 17, i, j) || m <= 0 || n < 0 || enchantment == null) continue;
            ArrayList<String> list = Lists.newArrayList();
            list.add("" + (Object)((Object)ChatFormatting.WHITE) + (Object)((Object)ChatFormatting.ITALIC) + I18n.get("container.enchant.clue", enchantment.getFullname(n).getColoredString()));
            if (!bl) {
                list.add("");
                if (this.minecraft.player.experienceLevel < m) {
                    list.add((Object)((Object)ChatFormatting.RED) + I18n.get("container.enchant.level.requirement", ((EnchantmentMenu)this.menu).costs[l]));
                } else {
                    String string = o == 1 ? I18n.get("container.enchant.lapis.one", new Object[0]) : I18n.get("container.enchant.lapis.many", o);
                    ChatFormatting chatFormatting = k >= o ? ChatFormatting.GRAY : ChatFormatting.RED;
                    list.add((Object)((Object)chatFormatting) + "" + string);
                    string = o == 1 ? I18n.get("container.enchant.level.one", new Object[0]) : I18n.get("container.enchant.level.many", o);
                    list.add((Object)((Object)ChatFormatting.GRAY) + "" + string);
                }
            }
            this.renderTooltip(list, i, j);
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

