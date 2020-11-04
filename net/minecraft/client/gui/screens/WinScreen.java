/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WinScreen
extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation EDITION_LOCATION = new ResourceLocation("textures/gui/title/edition.png");
    private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
    private static final String OBFUSCATE_TOKEN = "" + (Object)((Object)ChatFormatting.WHITE) + (Object)((Object)ChatFormatting.OBFUSCATED) + (Object)((Object)ChatFormatting.GREEN) + (Object)((Object)ChatFormatting.AQUA);
    private final boolean poem;
    private final Runnable onFinished;
    private float time;
    private List<FormattedCharSequence> lines;
    private IntSet centeredLines;
    private int totalScrollLength;
    private float scrollSpeed = 0.5f;

    public WinScreen(boolean bl, Runnable runnable) {
        super(NarratorChatListener.NO_TITLE);
        this.poem = bl;
        this.onFinished = runnable;
        if (!bl) {
            this.scrollSpeed = 0.75f;
        }
    }

    @Override
    public void tick() {
        this.minecraft.getMusicManager().tick();
        this.minecraft.getSoundManager().tick(false);
        float f = (float)(this.totalScrollLength + this.height + this.height + 24) / this.scrollSpeed;
        if (this.time > f) {
            this.respawn();
        }
    }

    @Override
    public void onClose() {
        this.respawn();
    }

    private void respawn() {
        this.onFinished.run();
        this.minecraft.setScreen(null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void init() {
        if (this.lines != null) {
            return;
        }
        this.lines = Lists.newArrayList();
        this.centeredLines = new IntOpenHashSet();
        Resource resource = null;
        try {
            String string4;
            BufferedReader bufferedReader;
            InputStream inputStream;
            int i = 274;
            if (this.poem) {
                int j;
                String string;
                resource = this.minecraft.getResourceManager().getResource(new ResourceLocation("texts/end.txt"));
                inputStream = resource.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                Random random = new Random(8124371L);
                while ((string = bufferedReader.readLine()) != null) {
                    string = string.replaceAll("PLAYERNAME", this.minecraft.getUser().getName());
                    while ((j = string.indexOf(OBFUSCATE_TOKEN)) != -1) {
                        String string2 = string.substring(0, j);
                        String string3 = string.substring(j + OBFUSCATE_TOKEN.length());
                        string = string2 + (Object)((Object)ChatFormatting.WHITE) + (Object)((Object)ChatFormatting.OBFUSCATED) + "XXXXXXXX".substring(0, random.nextInt(4) + 3) + string3;
                    }
                    this.lines.addAll(this.minecraft.font.split(new TextComponent(string), 274));
                    this.lines.add(FormattedCharSequence.EMPTY);
                }
                inputStream.close();
                for (j = 0; j < 8; ++j) {
                    this.lines.add(FormattedCharSequence.EMPTY);
                }
            }
            inputStream = this.minecraft.getResourceManager().getResource(new ResourceLocation("texts/credits.txt")).getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            while ((string4 = bufferedReader.readLine()) != null) {
                boolean bl;
                string4 = string4.replaceAll("PLAYERNAME", this.minecraft.getUser().getName());
                if ((string4 = string4.replaceAll("\t", "    ")).startsWith("[C]")) {
                    string4 = string4.substring(3);
                    bl = true;
                } else {
                    bl = false;
                }
                List<FormattedCharSequence> list = this.minecraft.font.split(new TextComponent(string4), 274);
                for (FormattedCharSequence formattedCharSequence : list) {
                    if (bl) {
                        this.centeredLines.add(this.lines.size());
                    }
                    this.lines.add(formattedCharSequence);
                }
                this.lines.add(FormattedCharSequence.EMPTY);
            }
            inputStream.close();
            this.totalScrollLength = this.lines.size() * 12;
            IOUtils.closeQuietly((Closeable)resource);
        } catch (Exception exception) {
            LOGGER.error("Couldn't load credits", (Throwable)exception);
        } finally {
            IOUtils.closeQuietly(resource);
        }
    }

    private void renderBg(int i, int j, float f) {
        this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
        int k = this.width;
        float g = -this.time * 0.5f * this.scrollSpeed;
        float h = (float)this.height - this.time * 0.5f * this.scrollSpeed;
        float l = 0.015625f;
        float m = this.time * 0.02f;
        float n = (float)(this.totalScrollLength + this.height + this.height + 24) / this.scrollSpeed;
        float o = (n - 20.0f - this.time) * 0.005f;
        if (o < m) {
            m = o;
        }
        if (m > 1.0f) {
            m = 1.0f;
        }
        m *= m;
        m = m * 96.0f / 255.0f;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(0.0, this.height, this.getBlitOffset()).uv(0.0f, g * 0.015625f).color(m, m, m, 1.0f).endVertex();
        bufferBuilder.vertex(k, this.height, this.getBlitOffset()).uv((float)k * 0.015625f, g * 0.015625f).color(m, m, m, 1.0f).endVertex();
        bufferBuilder.vertex(k, 0.0, this.getBlitOffset()).uv((float)k * 0.015625f, h * 0.015625f).color(m, m, m, 1.0f).endVertex();
        bufferBuilder.vertex(0.0, 0.0, this.getBlitOffset()).uv(0.0f, h * 0.015625f).color(m, m, m, 1.0f).endVertex();
        tesselator.end();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        int o;
        this.renderBg(i, j, f);
        int k = 274;
        int l = this.width / 2 - 137;
        int m = this.height + 50;
        this.time += f;
        float g = -this.time * this.scrollSpeed;
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0f, g, 0.0f);
        this.minecraft.getTextureManager().bind(LOGO_LOCATION);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        this.blitOutlineBlack(l, m, (integer, integer2) -> {
            this.blit(poseStack, integer + 0, (int)integer2, 0, 0, 155, 44);
            this.blit(poseStack, integer + 155, (int)integer2, 0, 45, 155, 44);
        });
        RenderSystem.disableBlend();
        this.minecraft.getTextureManager().bind(EDITION_LOCATION);
        WinScreen.blit(poseStack, l + 88, m + 37, 0.0f, 0.0f, 98, 14, 128, 16);
        RenderSystem.disableAlphaTest();
        int n = m + 100;
        for (o = 0; o < this.lines.size(); ++o) {
            float h;
            if (o == this.lines.size() - 1 && (h = (float)n + g - (float)(this.height / 2 - 6)) < 0.0f) {
                RenderSystem.translatef(0.0f, -h, 0.0f);
            }
            if ((float)n + g + 12.0f + 8.0f > 0.0f && (float)n + g < (float)this.height) {
                FormattedCharSequence formattedCharSequence = this.lines.get(o);
                if (this.centeredLines.contains(o)) {
                    this.font.drawShadow(poseStack, formattedCharSequence, (float)(l + (274 - this.font.width(formattedCharSequence)) / 2), (float)n, 0xFFFFFF);
                } else {
                    this.font.random.setSeed((long)((float)((long)o * 4238972211L) + this.time / 4.0f));
                    this.font.drawShadow(poseStack, formattedCharSequence, (float)l, (float)n, 0xFFFFFF);
                }
            }
            n += 12;
        }
        RenderSystem.popMatrix();
        this.minecraft.getTextureManager().bind(VIGNETTE_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        o = this.width;
        int p = this.height;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(0.0, p, this.getBlitOffset()).uv(0.0f, 1.0f).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(o, p, this.getBlitOffset()).uv(1.0f, 1.0f).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(o, 0.0, this.getBlitOffset()).uv(1.0f, 0.0f).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(0.0, 0.0, this.getBlitOffset()).uv(0.0f, 0.0f).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
        super.render(poseStack, i, j, f);
    }
}

