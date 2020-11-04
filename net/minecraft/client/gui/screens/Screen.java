/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class Screen
extends AbstractContainerEventHandler
implements TickableWidget,
Widget {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<String> ALLOWED_PROTOCOLS = Sets.newHashSet("http", "https");
    protected final Component title;
    protected final List<GuiEventListener> children = Lists.newArrayList();
    @Nullable
    protected Minecraft minecraft;
    protected ItemRenderer itemRenderer;
    public int width;
    public int height;
    protected final List<AbstractWidget> buttons = Lists.newArrayList();
    public boolean passEvents;
    protected Font font;
    private URI clickedLink;

    protected Screen(Component component) {
        this.title = component;
    }

    public Component getTitle() {
        return this.title;
    }

    public String getNarrationMessage() {
        return this.getTitle().getString();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        for (int k = 0; k < this.buttons.size(); ++k) {
            this.buttons.get(k).render(poseStack, i, j, f);
        }
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256 && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        }
        if (i == 258) {
            boolean bl;
            boolean bl2 = bl = !Screen.hasShiftDown();
            if (!this.changeFocus(bl)) {
                this.changeFocus(bl);
            }
            return false;
        }
        return super.keyPressed(i, j, k);
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    public void onClose() {
        this.minecraft.setScreen(null);
    }

    protected <T extends AbstractWidget> T addButton(T abstractWidget) {
        this.buttons.add(abstractWidget);
        return this.addWidget(abstractWidget);
    }

    protected <T extends GuiEventListener> T addWidget(T guiEventListener) {
        this.children.add(guiEventListener);
        return guiEventListener;
    }

    protected void renderTooltip(PoseStack poseStack, ItemStack itemStack, int i, int j) {
        this.renderComponentTooltip(poseStack, this.getTooltipFromItem(itemStack), i, j);
    }

    public List<Component> getTooltipFromItem(ItemStack itemStack) {
        return itemStack.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    public void renderTooltip(PoseStack poseStack, Component component, int i, int j) {
        this.renderTooltip(poseStack, Arrays.asList(component.getVisualOrderText()), i, j);
    }

    public void renderComponentTooltip(PoseStack poseStack, List<Component> list, int i, int j) {
        this.renderTooltip(poseStack, Lists.transform(list, Component::getVisualOrderText), i, j);
    }

    /*
     * WARNING - void declaration
     */
    public void renderTooltip(PoseStack poseStack, List<? extends FormattedCharSequence> list, int i, int j) {
        int n;
        int l;
        if (list.isEmpty()) {
            return;
        }
        int k = 0;
        for (FormattedCharSequence formattedCharSequence : list) {
            l = this.font.width(formattedCharSequence);
            if (l <= k) continue;
            k = l;
        }
        int m = i + 12;
        int n2 = j - 12;
        l = k;
        int o = 8;
        if (list.size() > 1) {
            o += 2 + (list.size() - 1) * 10;
        }
        if (m + k > this.width) {
            m -= 28 + k;
        }
        if (n2 + o + 6 > this.height) {
            n = this.height - o - 6;
        }
        poseStack.pushPose();
        int p = -267386864;
        int q = 0x505000FF;
        int r = 1344798847;
        int s = 400;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix4f = poseStack.last().pose();
        Screen.fillGradient(matrix4f, bufferBuilder, m - 3, n - 4, m + l + 3, n - 3, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, m - 3, n + o + 3, m + l + 3, n + o + 4, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, m - 3, n - 3, m + l + 3, n + o + 3, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, m - 4, n - 3, m - 3, n + o + 3, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, m + l + 3, n - 3, m + l + 4, n + o + 3, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, m - 3, n - 3 + 1, m - 3 + 1, n + o + 3 - 1, 400, 0x505000FF, 1344798847);
        Screen.fillGradient(matrix4f, bufferBuilder, m + l + 2, n - 3 + 1, m + l + 3, n + o + 3 - 1, 400, 0x505000FF, 1344798847);
        Screen.fillGradient(matrix4f, bufferBuilder, m - 3, n - 3, m + l + 3, n - 3 + 1, 400, 0x505000FF, 0x505000FF);
        Screen.fillGradient(matrix4f, bufferBuilder, m - 3, n + o + 2, m + l + 3, n + o + 3, 400, 1344798847, 1344798847);
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        poseStack.translate(0.0, 0.0, 400.0);
        for (int t = 0; t < list.size(); ++t) {
            FormattedCharSequence formattedCharSequence2 = list.get(t);
            if (formattedCharSequence2 != null) {
                void var7_11;
                this.font.drawInBatch(formattedCharSequence2, (float)m, (float)var7_11, -1, true, matrix4f, (MultiBufferSource)bufferSource, false, 0, 0xF000F0);
            }
            if (t == 0) {
                var7_11 += 2;
            }
            var7_11 += 10;
        }
        bufferSource.endBatch();
        poseStack.popPose();
    }

    protected void renderComponentHoverEffect(PoseStack poseStack, @Nullable Style style, int i, int j) {
        if (style == null || style.getHoverEvent() == null) {
            return;
        }
        HoverEvent hoverEvent = style.getHoverEvent();
        HoverEvent.ItemStackInfo itemStackInfo = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
        if (itemStackInfo != null) {
            this.renderTooltip(poseStack, itemStackInfo.getItemStack(), i, j);
        } else {
            HoverEvent.EntityTooltipInfo entityTooltipInfo = hoverEvent.getValue(HoverEvent.Action.SHOW_ENTITY);
            if (entityTooltipInfo != null) {
                if (this.minecraft.options.advancedItemTooltips) {
                    this.renderComponentTooltip(poseStack, entityTooltipInfo.getTooltipLines(), i, j);
                }
            } else {
                Component component = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
                if (component != null) {
                    this.renderTooltip(poseStack, this.minecraft.font.split(component, Math.max(this.width / 2, 200)), i, j);
                }
            }
        }
    }

    protected void insertText(String string, boolean bl) {
    }

    public boolean handleComponentClicked(@Nullable Style style) {
        if (style == null) {
            return false;
        }
        ClickEvent clickEvent = style.getClickEvent();
        if (Screen.hasShiftDown()) {
            if (style.getInsertion() != null) {
                this.insertText(style.getInsertion(), false);
            }
        } else if (clickEvent != null) {
            block21: {
                if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
                    if (!this.minecraft.options.chatLinks) {
                        return false;
                    }
                    try {
                        URI uRI = new URI(clickEvent.getValue());
                        String string = uRI.getScheme();
                        if (string == null) {
                            throw new URISyntaxException(clickEvent.getValue(), "Missing protocol");
                        }
                        if (!ALLOWED_PROTOCOLS.contains(string.toLowerCase(Locale.ROOT))) {
                            throw new URISyntaxException(clickEvent.getValue(), "Unsupported protocol: " + string.toLowerCase(Locale.ROOT));
                        }
                        if (this.minecraft.options.chatLinksPrompt) {
                            this.clickedLink = uRI;
                            this.minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, clickEvent.getValue(), false));
                            break block21;
                        }
                        this.openLink(uRI);
                    } catch (URISyntaxException uRISyntaxException) {
                        LOGGER.error("Can't open url for {}", (Object)clickEvent, (Object)uRISyntaxException);
                    }
                } else if (clickEvent.getAction() == ClickEvent.Action.OPEN_FILE) {
                    URI uRI = new File(clickEvent.getValue()).toURI();
                    this.openLink(uRI);
                } else if (clickEvent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
                    this.insertText(clickEvent.getValue(), true);
                } else if (clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    this.sendMessage(clickEvent.getValue(), false);
                } else if (clickEvent.getAction() == ClickEvent.Action.COPY_TO_CLIPBOARD) {
                    this.minecraft.keyboardHandler.setClipboard(clickEvent.getValue());
                } else {
                    LOGGER.error("Don't know how to handle {}", (Object)clickEvent);
                }
            }
            return true;
        }
        return false;
    }

    public void sendMessage(String string) {
        this.sendMessage(string, true);
    }

    public void sendMessage(String string, boolean bl) {
        if (bl) {
            this.minecraft.gui.getChat().addRecentChat(string);
        }
        this.minecraft.player.chat(string);
    }

    public void init(Minecraft minecraft, int i, int j) {
        this.minecraft = minecraft;
        this.itemRenderer = minecraft.getItemRenderer();
        this.font = minecraft.font;
        this.width = i;
        this.height = j;
        this.buttons.clear();
        this.children.clear();
        this.setFocused(null);
        this.init();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    protected void init() {
    }

    @Override
    public void tick() {
    }

    public void removed() {
    }

    public void renderBackground(PoseStack poseStack) {
        this.renderBackground(poseStack, 0);
    }

    public void renderBackground(PoseStack poseStack, int i) {
        if (this.minecraft.level != null) {
            this.fillGradient(poseStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.renderDirtBackground(i);
        }
    }

    public void renderDirtBackground(int i) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        this.minecraft.getTextureManager().bind(BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        float f = 32.0f;
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(0.0, this.height, 0.0).uv(0.0f, (float)this.height / 32.0f + (float)i).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.width, this.height, 0.0).uv((float)this.width / 32.0f, (float)this.height / 32.0f + (float)i).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.width, 0.0, 0.0).uv((float)this.width / 32.0f, i).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(0.0, 0.0, 0.0).uv(0.0f, i).color(64, 64, 64, 255).endVertex();
        tesselator.end();
    }

    public boolean isPauseScreen() {
        return true;
    }

    private void confirmLink(boolean bl) {
        if (bl) {
            this.openLink(this.clickedLink);
        }
        this.clickedLink = null;
        this.minecraft.setScreen(this);
    }

    private void openLink(URI uRI) {
        Util.getPlatform().openUri(uRI);
    }

    public static boolean hasControlDown() {
        if (Minecraft.ON_OSX) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 343) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 347);
        }
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 341) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 345);
    }

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
    }

    public static boolean hasAltDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 342) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 346);
    }

    public static boolean isCut(int i) {
        return i == 88 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isPaste(int i) {
        return i == 86 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isCopy(int i) {
        return i == 67 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isSelectAll(int i) {
        return i == 65 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public void resize(Minecraft minecraft, int i, int j) {
        this.init(minecraft, i, j);
    }

    public static void wrapScreenError(Runnable runnable, String string, String string2) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, string);
            CrashReportCategory crashReportCategory = crashReport.addCategory("Affected screen");
            crashReportCategory.setDetail("Screen name", () -> string2);
            throw new ReportedException(crashReport);
        }
    }

    protected boolean isValidCharacterForName(String string, char c, int i) {
        int j = string.indexOf(58);
        int k = string.indexOf(47);
        if (c == ':') {
            return (k == -1 || i <= k) && j == -1;
        }
        if (c == '/') {
            return i > j;
        }
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.';
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return true;
    }

    public void onFilesDrop(List<Path> list) {
    }
}

