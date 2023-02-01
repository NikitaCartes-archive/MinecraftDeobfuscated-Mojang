/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.annotations.VisibleForTesting;
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
import com.mojang.logging.LogUtils;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.ScreenNarrationCollector;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2ic;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public abstract class Screen
extends AbstractContainerEventHandler
implements Renderable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> ALLOWED_PROTOCOLS = Sets.newHashSet("http", "https");
    private static final int EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE = 2;
    private static final Component USAGE_NARRATION = Component.translatable("narrator.screen.usage");
    protected final Component title;
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final List<NarratableEntry> narratables = Lists.newArrayList();
    @Nullable
    protected Minecraft minecraft;
    private boolean initialized;
    protected ItemRenderer itemRenderer;
    public int width;
    public int height;
    private final List<Renderable> renderables = Lists.newArrayList();
    public boolean passEvents;
    protected Font font;
    @Nullable
    private URI clickedLink;
    private static final long NARRATE_SUPPRESS_AFTER_INIT_TIME;
    private static final long NARRATE_DELAY_NARRATOR_ENABLED;
    private static final long NARRATE_DELAY_MOUSE_MOVE = 750L;
    private static final long NARRATE_DELAY_MOUSE_ACTION = 200L;
    private static final long NARRATE_DELAY_KEYBOARD_ACTION = 200L;
    private final ScreenNarrationCollector narrationState = new ScreenNarrationCollector();
    private long narrationSuppressTime = Long.MIN_VALUE;
    private long nextNarrationTime = Long.MAX_VALUE;
    @Nullable
    private NarratableEntry lastNarratable;
    @Nullable
    private DeferredTooltipRendering deferredTooltipRendering;

    protected Screen(Component component) {
        this.title = component;
    }

    public Component getTitle() {
        return this.title;
    }

    public Component getNarrationMessage() {
        return this.getTitle();
    }

    public final void renderWithTooltip(PoseStack poseStack, int i, int j, float f) {
        this.render(poseStack, i, j, f);
        if (this.deferredTooltipRendering != null) {
            this.renderTooltip(poseStack, this.deferredTooltipRendering, i, j);
            this.deferredTooltipRendering = null;
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        for (Renderable renderable : this.renderables) {
            renderable.render(poseStack, i, j, f);
        }
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        FocusNavigationEvent.TabNavigation focusNavigationEvent;
        if (i == 256 && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        }
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        switch (i) {
            case 263: {
                Record record = this.createArrowEvent(ScreenDirection.LEFT);
                break;
            }
            case 262: {
                Record record = this.createArrowEvent(ScreenDirection.RIGHT);
                break;
            }
            case 265: {
                Record record = this.createArrowEvent(ScreenDirection.UP);
                break;
            }
            case 264: {
                Record record = this.createArrowEvent(ScreenDirection.DOWN);
                break;
            }
            case 258: {
                Record record = this.createTabEvent();
                break;
            }
            default: {
                Record record = focusNavigationEvent = null;
            }
        }
        if (focusNavigationEvent != null) {
            ComponentPath componentPath = super.nextFocusPath(focusNavigationEvent);
            if (componentPath == null && focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation) {
                this.clearFocus();
                componentPath = super.nextFocusPath(focusNavigationEvent);
            }
            if (componentPath != null) {
                this.changeFocus(componentPath);
            }
        }
        return false;
    }

    private FocusNavigationEvent.TabNavigation createTabEvent() {
        boolean bl = !Screen.hasShiftDown();
        return new FocusNavigationEvent.TabNavigation(bl);
    }

    private FocusNavigationEvent.ArrowNavigation createArrowEvent(ScreenDirection screenDirection) {
        return new FocusNavigationEvent.ArrowNavigation(screenDirection);
    }

    protected void setInitialFocus(GuiEventListener guiEventListener) {
        ComponentPath componentPath = ComponentPath.path(this, guiEventListener.nextFocusPath(new FocusNavigationEvent.InitialFocus()));
        if (componentPath != null) {
            this.changeFocus(componentPath);
        }
    }

    private void clearFocus() {
        ComponentPath componentPath = this.getCurrentFocusPath();
        if (componentPath != null) {
            componentPath.applyFocus(false);
        }
    }

    @VisibleForTesting
    protected void changeFocus(ComponentPath componentPath) {
        this.clearFocus();
        componentPath.applyFocus(true);
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    public void onClose() {
        this.minecraft.setScreen(null);
    }

    protected <T extends GuiEventListener & Renderable> T addRenderableWidget(T guiEventListener) {
        this.renderables.add(guiEventListener);
        return this.addWidget(guiEventListener);
    }

    protected <T extends Renderable> T addRenderableOnly(T renderable) {
        this.renderables.add(renderable);
        return renderable;
    }

    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T guiEventListener) {
        this.children.add(guiEventListener);
        this.narratables.add(guiEventListener);
        return guiEventListener;
    }

    protected void removeWidget(GuiEventListener guiEventListener) {
        if (guiEventListener instanceof Renderable) {
            this.renderables.remove((Renderable)((Object)guiEventListener));
        }
        if (guiEventListener instanceof NarratableEntry) {
            this.narratables.remove((NarratableEntry)((Object)guiEventListener));
        }
        this.children.remove(guiEventListener);
    }

    protected void clearWidgets() {
        this.renderables.clear();
        this.children.clear();
        this.narratables.clear();
    }

    protected void renderTooltip(PoseStack poseStack, ItemStack itemStack, int i, int j) {
        this.renderTooltip(poseStack, this.getTooltipFromItem(itemStack), itemStack.getTooltipImage(), i, j);
    }

    public void renderTooltip(PoseStack poseStack, List<Component> list, Optional<TooltipComponent> optional, int i, int j) {
        List<ClientTooltipComponent> list2 = list.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        optional.ifPresent(tooltipComponent -> list2.add(1, ClientTooltipComponent.create(tooltipComponent)));
        this.renderTooltipInternal(poseStack, list2, i, j, DefaultTooltipPositioner.INSTANCE);
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

    public void renderTooltip(PoseStack poseStack, List<? extends FormattedCharSequence> list, int i, int j) {
        this.renderTooltipInternal(poseStack, list.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), i, j, DefaultTooltipPositioner.INSTANCE);
    }

    private void renderTooltip(PoseStack poseStack, DeferredTooltipRendering deferredTooltipRendering, int i, int j) {
        this.renderTooltipInternal(poseStack, deferredTooltipRendering.tooltip().stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), i, j, deferredTooltipRendering.positioner());
    }

    private void renderTooltipInternal(PoseStack poseStack, List<ClientTooltipComponent> list, int i2, int j2, ClientTooltipPositioner clientTooltipPositioner) {
        ClientTooltipComponent clientTooltipComponent2;
        int t;
        if (list.isEmpty()) {
            return;
        }
        int k2 = 0;
        int l2 = list.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent clientTooltipComponent : list) {
            int m2 = clientTooltipComponent.getWidth(this.font);
            if (m2 > k2) {
                k2 = m2;
            }
            l2 += clientTooltipComponent.getHeight();
        }
        int n2 = k2;
        int o2 = l2;
        Vector2ic vector2ic = clientTooltipPositioner.positionTooltip(this, i2, j2, n2, o2);
        int p = vector2ic.x();
        int q = vector2ic.y();
        poseStack.pushPose();
        int r = 400;
        float f = this.itemRenderer.blitOffset;
        this.itemRenderer.blitOffset = 400.0f;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder2 = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder2.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix4f2 = poseStack.last().pose();
        TooltipRenderUtil.renderTooltipBackground((matrix4f, bufferBuilder, i, j, k, l, m, n, o) -> GuiComponent.fillGradient(matrix4f, bufferBuilder, i, j, k, l, m, n, o), matrix4f2, bufferBuilder2, p, q, n2, o2, 400);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferUploader.drawWithShader(bufferBuilder2.end());
        RenderSystem.disableBlend();
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        poseStack.translate(0.0f, 0.0f, 400.0f);
        int s = q;
        for (t = 0; t < list.size(); ++t) {
            clientTooltipComponent2 = list.get(t);
            clientTooltipComponent2.renderText(this.font, p, s, matrix4f2, bufferSource);
            s += clientTooltipComponent2.getHeight() + (t == 0 ? 2 : 0);
        }
        bufferSource.endBatch();
        poseStack.popPose();
        s = q;
        for (t = 0; t < list.size(); ++t) {
            clientTooltipComponent2 = list.get(t);
            clientTooltipComponent2.renderImage(this.font, p, s, poseStack, this.itemRenderer, 400);
            s += clientTooltipComponent2.getHeight() + (t == 0 ? 2 : 0);
        }
        this.itemRenderer.blitOffset = f;
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
            block24: {
                if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
                    if (!this.minecraft.options.chatLinks().get().booleanValue()) {
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
                        if (this.minecraft.options.chatLinksPrompt().get().booleanValue()) {
                            this.clickedLink = uRI;
                            this.minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, clickEvent.getValue(), false));
                            break block24;
                        }
                        this.openLink(uRI);
                    } catch (URISyntaxException uRISyntaxException) {
                        LOGGER.error("Can't open url for {}", (Object)clickEvent, (Object)uRISyntaxException);
                    }
                } else if (clickEvent.getAction() == ClickEvent.Action.OPEN_FILE) {
                    URI uRI = new File(clickEvent.getValue()).toURI();
                    this.openLink(uRI);
                } else if (clickEvent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
                    this.insertText(SharedConstants.filterText(clickEvent.getValue()), true);
                } else if (clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    String string2 = SharedConstants.filterText(clickEvent.getValue());
                    if (string2.startsWith("/")) {
                        if (!this.minecraft.player.connection.sendUnsignedCommand(string2.substring(1))) {
                            LOGGER.error("Not allowed to run command with signed argument from click event: '{}'", (Object)string2);
                        }
                    } else {
                        LOGGER.error("Failed to run command without '/' prefix from click event: '{}'", (Object)string2);
                    }
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

    public final void init(Minecraft minecraft, int i, int j) {
        this.minecraft = minecraft;
        this.itemRenderer = minecraft.getItemRenderer();
        this.font = minecraft.font;
        this.width = i;
        this.height = j;
        if (!this.initialized) {
            this.init();
        } else {
            this.repositionElements();
        }
        this.initialized = true;
        this.triggerImmediateNarration(false);
        this.suppressNarration(NARRATE_SUPPRESS_AFTER_INIT_TIME);
    }

    protected void rebuildWidgets() {
        this.clearWidgets();
        this.clearFocus();
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

    public void renderBackground(PoseStack poseStack) {
        if (this.minecraft.level != null) {
            this.fillGradient(poseStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.renderDirtBackground(poseStack);
        }
    }

    public void renderDirtBackground(PoseStack poseStack) {
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(0.25f, 0.25f, 0.25f, 1.0f);
        int i = 32;
        Screen.blit(poseStack, 0, 0, 0, 0.0f, 0.0f, this.width, this.height, 32, 32);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
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

    protected void repositionElements() {
        this.rebuildWidgets();
    }

    public void resize(Minecraft minecraft, int i, int j) {
        this.width = i;
        this.height = j;
        this.repositionElements();
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

    private void scheduleNarration(long l, boolean bl) {
        this.nextNarrationTime = Util.getMillis() + l;
        if (bl) {
            this.narrationSuppressTime = Long.MIN_VALUE;
        }
    }

    private void suppressNarration(long l) {
        this.narrationSuppressTime = Util.getMillis() + l;
    }

    public void afterMouseMove() {
        this.scheduleNarration(750L, false);
    }

    public void afterMouseAction() {
        this.scheduleNarration(200L, true);
    }

    public void afterKeyboardAction() {
        this.scheduleNarration(200L, true);
    }

    private boolean shouldRunNarration() {
        return this.minecraft.getNarrator().isActive();
    }

    public void handleDelayedNarration() {
        long l;
        if (this.shouldRunNarration() && (l = Util.getMillis()) > this.nextNarrationTime && l > this.narrationSuppressTime) {
            this.runNarration(true);
            this.nextNarrationTime = Long.MAX_VALUE;
        }
    }

    public void triggerImmediateNarration(boolean bl) {
        if (this.shouldRunNarration()) {
            this.runNarration(bl);
        }
    }

    private void runNarration(boolean bl) {
        this.narrationState.update(this::updateNarrationState);
        String string = this.narrationState.collectNarrationText(!bl);
        if (!string.isEmpty()) {
            this.minecraft.getNarrator().sayNow(string);
        }
    }

    protected boolean shouldNarrateNavigation() {
        return true;
    }

    protected void updateNarrationState(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getNarrationMessage());
        if (this.shouldNarrateNavigation()) {
            narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }
        this.updateNarratedWidget(narrationElementOutput);
    }

    protected void updateNarratedWidget(NarrationElementOutput narrationElementOutput) {
        List list = this.narratables.stream().filter(NarratableEntry::isActive).collect(Collectors.toList());
        Collections.sort(list, Comparator.comparingInt(TabOrderedElement::getTabOrderGroup));
        NarratableSearchResult narratableSearchResult = Screen.findNarratableWidget(list, this.lastNarratable);
        if (narratableSearchResult != null) {
            if (narratableSearchResult.priority.isTerminal()) {
                this.lastNarratable = narratableSearchResult.entry;
            }
            if (list.size() > 1) {
                narrationElementOutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.screen", narratableSearchResult.index + 1, list.size()));
                if (narratableSearchResult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
                    narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.component_list.usage"));
                }
            }
            narratableSearchResult.entry.updateNarration(narrationElementOutput.nest());
        }
    }

    @Nullable
    public static NarratableSearchResult findNarratableWidget(List<? extends NarratableEntry> list, @Nullable NarratableEntry narratableEntry) {
        NarratableSearchResult narratableSearchResult = null;
        NarratableSearchResult narratableSearchResult2 = null;
        int j = list.size();
        for (int i = 0; i < j; ++i) {
            NarratableEntry narratableEntry2 = list.get(i);
            NarratableEntry.NarrationPriority narrationPriority = narratableEntry2.narrationPriority();
            if (narrationPriority.isTerminal()) {
                if (narratableEntry2 == narratableEntry) {
                    narratableSearchResult2 = new NarratableSearchResult(narratableEntry2, i, narrationPriority);
                    continue;
                }
                return new NarratableSearchResult(narratableEntry2, i, narrationPriority);
            }
            if (narrationPriority.compareTo(narratableSearchResult != null ? narratableSearchResult.priority : NarratableEntry.NarrationPriority.NONE) <= 0) continue;
            narratableSearchResult = new NarratableSearchResult(narratableEntry2, i, narrationPriority);
        }
        return narratableSearchResult != null ? narratableSearchResult : narratableSearchResult2;
    }

    public void narrationEnabled() {
        this.scheduleNarration(NARRATE_DELAY_NARRATOR_ENABLED, false);
    }

    public void setTooltipForNextRenderPass(List<FormattedCharSequence> list) {
        this.setTooltipForNextRenderPass(list, DefaultTooltipPositioner.INSTANCE, true);
    }

    public void setTooltipForNextRenderPass(List<FormattedCharSequence> list, ClientTooltipPositioner clientTooltipPositioner, boolean bl) {
        if (this.deferredTooltipRendering == null || bl) {
            this.deferredTooltipRendering = new DeferredTooltipRendering(list, clientTooltipPositioner);
        }
    }

    protected void setTooltipForNextRenderPass(Component component) {
        this.setTooltipForNextRenderPass(Tooltip.splitTooltip(this.minecraft, component));
    }

    public void setTooltipForNextRenderPass(Tooltip tooltip, ClientTooltipPositioner clientTooltipPositioner, boolean bl) {
        this.setTooltipForNextRenderPass(tooltip.toCharSequence(this.minecraft), clientTooltipPositioner, bl);
    }

    protected static void hideWidgets(AbstractWidget ... abstractWidgets) {
        for (AbstractWidget abstractWidget : abstractWidgets) {
            abstractWidget.visible = false;
        }
    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(0, 0, this.width, this.height);
    }

    static {
        NARRATE_DELAY_NARRATOR_ENABLED = NARRATE_SUPPRESS_AFTER_INIT_TIME = TimeUnit.SECONDS.toMillis(2L);
    }

    @Environment(value=EnvType.CLIENT)
    record DeferredTooltipRendering(List<FormattedCharSequence> tooltip, ClientTooltipPositioner positioner) {
    }

    @Environment(value=EnvType.CLIENT)
    public static class NarratableSearchResult {
        public final NarratableEntry entry;
        public final int index;
        public final NarratableEntry.NarrationPriority priority;

        public NarratableSearchResult(NarratableEntry narratableEntry, int i, NarratableEntry.NarrationPriority narrationPriority) {
            this.entry = narratableEntry;
            this.index = i;
            this.priority = narrationPriority;
        }
    }
}

