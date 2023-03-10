/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WinScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
    private static final Component SECTION_HEADING = Component.literal("============").withStyle(ChatFormatting.WHITE);
    private static final String NAME_PREFIX = "           ";
    private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
    private static final float SPEEDUP_FACTOR = 5.0f;
    private static final float SPEEDUP_FACTOR_FAST = 15.0f;
    private final boolean poem;
    private final Runnable onFinished;
    private float scroll;
    private List<FormattedCharSequence> lines;
    private IntSet centeredLines;
    private int totalScrollLength;
    private boolean speedupActive;
    private final IntSet speedupModifiers = new IntOpenHashSet();
    private float scrollSpeed;
    private final float unmodifiedScrollSpeed;
    private final LogoRenderer logoRenderer = new LogoRenderer(false);

    public WinScreen(boolean bl, Runnable runnable) {
        super(GameNarrator.NO_TITLE);
        this.poem = bl;
        this.onFinished = runnable;
        this.unmodifiedScrollSpeed = !bl ? 0.75f : 0.5f;
        this.scrollSpeed = this.unmodifiedScrollSpeed;
    }

    private float calculateScrollSpeed() {
        if (this.speedupActive) {
            return this.unmodifiedScrollSpeed * (5.0f + (float)this.speedupModifiers.size() * 15.0f);
        }
        return this.unmodifiedScrollSpeed;
    }

    @Override
    public void tick() {
        this.minecraft.getMusicManager().tick();
        this.minecraft.getSoundManager().tick(false);
        float f = this.totalScrollLength + this.height + this.height + 24;
        if (this.scroll > f) {
            this.respawn();
        }
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 341 || i == 345) {
            this.speedupModifiers.add(i);
        } else if (i == 32) {
            this.speedupActive = true;
        }
        this.scrollSpeed = this.calculateScrollSpeed();
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        if (i == 32) {
            this.speedupActive = false;
        } else if (i == 341 || i == 345) {
            this.speedupModifiers.remove(i);
        }
        this.scrollSpeed = this.calculateScrollSpeed();
        return super.keyReleased(i, j, k);
    }

    @Override
    public void onClose() {
        this.respawn();
    }

    private void respawn() {
        this.onFinished.run();
    }

    @Override
    protected void init() {
        if (this.lines != null) {
            return;
        }
        this.lines = Lists.newArrayList();
        this.centeredLines = new IntOpenHashSet();
        if (this.poem) {
            this.wrapCreditsIO("texts/end.txt", this::addPoemFile);
        }
        this.wrapCreditsIO("texts/credits.json", this::addCreditsFile);
        if (this.poem) {
            this.wrapCreditsIO("texts/postcredits.txt", this::addPoemFile);
        }
        this.totalScrollLength = this.lines.size() * 12;
    }

    private void wrapCreditsIO(String string, CreditsReader creditsReader) {
        try (BufferedReader reader = this.minecraft.getResourceManager().openAsReader(new ResourceLocation(string));){
            creditsReader.read(reader);
        } catch (Exception exception) {
            LOGGER.error("Couldn't load credits", exception);
        }
    }

    private void addPoemFile(Reader reader) throws IOException {
        int i;
        Object string;
        BufferedReader bufferedReader = new BufferedReader(reader);
        RandomSource randomSource = RandomSource.create(8124371L);
        while ((string = bufferedReader.readLine()) != null) {
            string = ((String)string).replaceAll("PLAYERNAME", this.minecraft.getUser().getName());
            while ((i = ((String)string).indexOf(OBFUSCATE_TOKEN)) != -1) {
                String string2 = ((String)string).substring(0, i);
                String string3 = ((String)string).substring(i + OBFUSCATE_TOKEN.length());
                string = string2 + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, randomSource.nextInt(4) + 3) + string3;
            }
            this.addPoemLines((String)string);
            this.addEmptyLine();
        }
        for (i = 0; i < 8; ++i) {
            this.addEmptyLine();
        }
    }

    private void addCreditsFile(Reader reader) {
        JsonArray jsonArray = GsonHelper.parseArray(reader);
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String string = jsonObject.get("section").getAsString();
            this.addCreditsLine(SECTION_HEADING, true);
            this.addCreditsLine(Component.literal(string).withStyle(ChatFormatting.YELLOW), true);
            this.addCreditsLine(SECTION_HEADING, true);
            this.addEmptyLine();
            this.addEmptyLine();
            JsonArray jsonArray2 = jsonObject.getAsJsonArray("titles");
            for (JsonElement jsonElement2 : jsonArray2) {
                JsonObject jsonObject2 = jsonElement2.getAsJsonObject();
                String string2 = jsonObject2.get("title").getAsString();
                JsonArray jsonArray3 = jsonObject2.getAsJsonArray("names");
                this.addCreditsLine(Component.literal(string2).withStyle(ChatFormatting.GRAY), false);
                for (JsonElement jsonElement3 : jsonArray3) {
                    String string3 = jsonElement3.getAsString();
                    this.addCreditsLine(Component.literal(NAME_PREFIX).append(string3).withStyle(ChatFormatting.WHITE), false);
                }
                this.addEmptyLine();
                this.addEmptyLine();
            }
        }
    }

    private void addEmptyLine() {
        this.lines.add(FormattedCharSequence.EMPTY);
    }

    private void addPoemLines(String string) {
        this.lines.addAll(this.minecraft.font.split(Component.literal(string), 274));
    }

    private void addCreditsLine(Component component, boolean bl) {
        if (bl) {
            this.centeredLines.add(this.lines.size());
        }
        this.lines.add(component.getVisualOrderText());
    }

    private void renderBg(PoseStack poseStack) {
        RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
        int i = this.width;
        float f = this.scroll * 0.5f;
        int j = 64;
        float g = this.scroll / this.unmodifiedScrollSpeed;
        float h = g * 0.02f;
        float k = (float)(this.totalScrollLength + this.height + this.height + 24) / this.unmodifiedScrollSpeed;
        float l = (k - 20.0f - g) * 0.005f;
        if (l < h) {
            h = l;
        }
        if (h > 1.0f) {
            h = 1.0f;
        }
        h *= h;
        h = h * 96.0f / 255.0f;
        RenderSystem.setShaderColor(h, h, h, 1.0f);
        WinScreen.blit(poseStack, 0, 0, 0, 0.0f, f, i, this.height, 64, 64);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.scroll += f * this.scrollSpeed;
        this.renderBg(poseStack);
        int k = this.width / 2 - 137;
        int l = this.height + 50;
        float g = -this.scroll;
        poseStack.pushPose();
        poseStack.translate(0.0f, g, 0.0f);
        this.logoRenderer.renderLogo(poseStack, this.width, 1.0f, l);
        int m = l + 100;
        for (int n = 0; n < this.lines.size(); ++n) {
            float h;
            if (n == this.lines.size() - 1 && (h = (float)m + g - (float)(this.height / 2 - 6)) < 0.0f) {
                poseStack.translate(0.0f, -h, 0.0f);
            }
            if ((float)m + g + 12.0f + 8.0f > 0.0f && (float)m + g < (float)this.height) {
                FormattedCharSequence formattedCharSequence = this.lines.get(n);
                if (this.centeredLines.contains(n)) {
                    this.font.drawShadow(poseStack, formattedCharSequence, (float)(k + (274 - this.font.width(formattedCharSequence)) / 2), (float)m, 0xFFFFFF);
                } else {
                    this.font.drawShadow(poseStack, formattedCharSequence, (float)k, (float)m, 0xFFFFFF);
                }
            }
            m += 12;
        }
        poseStack.popPose();
        RenderSystem.setShaderTexture(0, VIGNETTE_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        WinScreen.blit(poseStack, 0, 0, 0, 0.0f, 0.0f, this.width, this.height, this.width, this.height);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        super.render(poseStack, i, j, f);
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    static interface CreditsReader {
        public void read(Reader var1) throws IOException;
    }
}

