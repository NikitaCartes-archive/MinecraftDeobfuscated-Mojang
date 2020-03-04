/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Matrix4f;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class Screen
extends AbstractContainerEventHandler
implements Widget {
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
    public void render(int i, int j, float f) {
        for (int k = 0; k < this.buttons.size(); ++k) {
            this.buttons.get(k).render(i, j, f);
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
            return true;
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

    protected void renderTooltip(ItemStack itemStack, int i, int j) {
        this.renderTooltip(this.getTooltipFromItem(itemStack), i, j);
    }

    public List<String> getTooltipFromItem(ItemStack itemStack) {
        List<Component> list = itemStack.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
        ArrayList<String> list2 = Lists.newArrayList();
        for (Component component : list) {
            list2.add(component.getColoredString());
        }
        return list2;
    }

    public void renderTooltip(String string, int i, int j) {
        this.renderTooltip(Arrays.asList(string), i, j);
    }

    public void renderTooltip(List<String> list, int i, int j) {
        int l;
        if (list.isEmpty()) {
            return;
        }
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        int k = 0;
        for (String string : list) {
            l = this.font.width(string);
            if (l <= k) continue;
            k = l;
        }
        int m = i + 12;
        int n = j - 12;
        l = k;
        int o = 8;
        if (list.size() > 1) {
            o += 2 + (list.size() - 1) * 10;
        }
        if (m + k > this.width) {
            m -= 28 + k;
        }
        if (n + o + 6 > this.height) {
            n = this.height - o - 6;
        }
        this.setBlitOffset(300);
        this.itemRenderer.blitOffset = 300.0f;
        int p = -267386864;
        this.fillGradient(m - 3, n - 4, m + l + 3, n - 3, -267386864, -267386864);
        this.fillGradient(m - 3, n + o + 3, m + l + 3, n + o + 4, -267386864, -267386864);
        this.fillGradient(m - 3, n - 3, m + l + 3, n + o + 3, -267386864, -267386864);
        this.fillGradient(m - 4, n - 3, m - 3, n + o + 3, -267386864, -267386864);
        this.fillGradient(m + l + 3, n - 3, m + l + 4, n + o + 3, -267386864, -267386864);
        int q = 0x505000FF;
        int r = 1344798847;
        this.fillGradient(m - 3, n - 3 + 1, m - 3 + 1, n + o + 3 - 1, 0x505000FF, 1344798847);
        this.fillGradient(m + l + 2, n - 3 + 1, m + l + 3, n + o + 3 - 1, 0x505000FF, 1344798847);
        this.fillGradient(m - 3, n - 3, m + l + 3, n - 3 + 1, 0x505000FF, 0x505000FF);
        this.fillGradient(m - 3, n + o + 2, m + l + 3, n + o + 3, 1344798847, 1344798847);
        PoseStack poseStack = new PoseStack();
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        poseStack.translate(0.0, 0.0, this.itemRenderer.blitOffset);
        Matrix4f matrix4f = poseStack.last().pose();
        for (int s = 0; s < list.size(); ++s) {
            String string2 = list.get(s);
            if (string2 != null) {
                this.font.drawInBatch(string2, m, n, -1, true, matrix4f, bufferSource, false, 0, 0xF000F0);
            }
            if (s == 0) {
                n += 2;
            }
            n += 10;
        }
        bufferSource.endBatch();
        this.setBlitOffset(0);
        this.itemRenderer.blitOffset = 0.0f;
        RenderSystem.enableDepthTest();
        RenderSystem.enableRescaleNormal();
    }

    protected void renderComponentHoverEffect(Component component, int i, int j) {
        if (component == null || component.getStyle().getHoverEvent() == null) {
            return;
        }
        HoverEvent hoverEvent = component.getStyle().getHoverEvent();
        if (hoverEvent.getAction() == HoverEvent.Action.SHOW_ITEM) {
            ItemStack itemStack = ItemStack.EMPTY;
            try {
                CompoundTag tag = TagParser.parseTag(hoverEvent.getValue().getString());
                if (tag instanceof CompoundTag) {
                    itemStack = ItemStack.of(tag);
                }
            } catch (CommandSyntaxException tag) {
                // empty catch block
            }
            if (itemStack.isEmpty()) {
                this.renderTooltip((Object)((Object)ChatFormatting.RED) + "Invalid Item!", i, j);
            } else {
                this.renderTooltip(itemStack, i, j);
            }
        } else if (hoverEvent.getAction() == HoverEvent.Action.SHOW_ENTITY) {
            if (this.minecraft.options.advancedItemTooltips) {
                try {
                    CompoundTag compoundTag = TagParser.parseTag(hoverEvent.getValue().getString());
                    ArrayList<String> list = Lists.newArrayList();
                    Component component2 = Component.Serializer.fromJson(compoundTag.getString("name"));
                    if (component2 != null) {
                        list.add(component2.getColoredString());
                    }
                    if (compoundTag.contains("type", 8)) {
                        String string = compoundTag.getString("type");
                        list.add("Type: " + string);
                    }
                    list.add(compoundTag.getString("id"));
                    this.renderTooltip(list, i, j);
                } catch (JsonSyntaxException | CommandSyntaxException exception) {
                    this.renderTooltip((Object)((Object)ChatFormatting.RED) + "Invalid Entity!", i, j);
                }
            }
        } else if (hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT) {
            this.renderTooltip(this.minecraft.font.split(hoverEvent.getValue().getColoredString(), Math.max(this.width / 2, 200)), i, j);
        }
    }

    protected void insertText(String string, boolean bl) {
    }

    public boolean handleComponentClicked(Component component) {
        if (component == null) {
            return false;
        }
        ClickEvent clickEvent = component.getStyle().getClickEvent();
        if (Screen.hasShiftDown()) {
            if (component.getStyle().getInsertion() != null) {
                this.insertText(component.getStyle().getInsertion(), false);
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

    public void tick() {
    }

    public void removed() {
    }

    public void renderBackground() {
        this.renderBackground(0);
    }

    public void renderBackground(int i) {
        if (this.minecraft.level != null) {
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
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
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
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
}

