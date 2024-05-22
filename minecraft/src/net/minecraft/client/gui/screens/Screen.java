package net.minecraft.client.gui.screens;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public abstract class Screen extends AbstractContainerEventHandler implements Renderable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Set<String> ALLOWED_PROTOCOLS = Sets.<String>newHashSet("http", "https");
	private static final Component USAGE_NARRATION = Component.translatable("narrator.screen.usage");
	protected static final CubeMap CUBE_MAP = new CubeMap(ResourceLocation.withDefaultNamespace("textures/gui/title/background/panorama"));
	protected static final PanoramaRenderer PANORAMA = new PanoramaRenderer(CUBE_MAP);
	public static final ResourceLocation MENU_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/menu_background.png");
	public static final ResourceLocation HEADER_SEPARATOR = ResourceLocation.withDefaultNamespace("textures/gui/header_separator.png");
	public static final ResourceLocation FOOTER_SEPARATOR = ResourceLocation.withDefaultNamespace("textures/gui/footer_separator.png");
	private static final ResourceLocation INWORLD_MENU_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_background.png");
	public static final ResourceLocation INWORLD_HEADER_SEPARATOR = ResourceLocation.withDefaultNamespace("textures/gui/inworld_header_separator.png");
	public static final ResourceLocation INWORLD_FOOTER_SEPARATOR = ResourceLocation.withDefaultNamespace("textures/gui/inworld_footer_separator.png");
	protected final Component title;
	private final List<GuiEventListener> children = Lists.<GuiEventListener>newArrayList();
	private final List<NarratableEntry> narratables = Lists.<NarratableEntry>newArrayList();
	@Nullable
	protected Minecraft minecraft;
	private boolean initialized;
	public int width;
	public int height;
	private final List<Renderable> renderables = Lists.<Renderable>newArrayList();
	protected Font font;
	@Nullable
	private URI clickedLink;
	private static final long NARRATE_SUPPRESS_AFTER_INIT_TIME = TimeUnit.SECONDS.toMillis(2L);
	private static final long NARRATE_DELAY_NARRATOR_ENABLED = NARRATE_SUPPRESS_AFTER_INIT_TIME;
	private static final long NARRATE_DELAY_MOUSE_MOVE = 750L;
	private static final long NARRATE_DELAY_MOUSE_ACTION = 200L;
	private static final long NARRATE_DELAY_KEYBOARD_ACTION = 200L;
	private final ScreenNarrationCollector narrationState = new ScreenNarrationCollector();
	private long narrationSuppressTime = Long.MIN_VALUE;
	private long nextNarrationTime = Long.MAX_VALUE;
	@Nullable
	private NarratableEntry lastNarratable;
	@Nullable
	private Screen.DeferredTooltipRendering deferredTooltipRendering;
	protected final Executor screenExecutor = runnable -> this.minecraft.execute(() -> {
			if (this.minecraft.screen == this) {
				runnable.run();
			}
		});

	protected Screen(Component component) {
		this.title = component;
	}

	public Component getTitle() {
		return this.title;
	}

	public Component getNarrationMessage() {
		return this.getTitle();
	}

	public final void renderWithTooltip(GuiGraphics guiGraphics, int i, int j, float f) {
		this.render(guiGraphics, i, j, f);
		if (this.deferredTooltipRendering != null) {
			guiGraphics.renderTooltip(this.font, this.deferredTooltipRendering.tooltip(), this.deferredTooltipRendering.positioner(), i, j);
			this.deferredTooltipRendering = null;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderBackground(guiGraphics, i, j, f);

		for (Renderable renderable : this.renderables) {
			renderable.render(guiGraphics, i, j, f);
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256 && this.shouldCloseOnEsc()) {
			this.onClose();
			return true;
		} else if (super.keyPressed(i, j, k)) {
			return true;
		} else {
			FocusNavigationEvent focusNavigationEvent = (FocusNavigationEvent)(switch (i) {
				case 258 -> this.createTabEvent();
				default -> null;
				case 262 -> this.createArrowEvent(ScreenDirection.RIGHT);
				case 263 -> this.createArrowEvent(ScreenDirection.LEFT);
				case 264 -> this.createArrowEvent(ScreenDirection.DOWN);
				case 265 -> this.createArrowEvent(ScreenDirection.UP);
			});
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
	}

	private FocusNavigationEvent.TabNavigation createTabEvent() {
		boolean bl = !hasShiftDown();
		return new FocusNavigationEvent.TabNavigation(bl);
	}

	private FocusNavigationEvent.ArrowNavigation createArrowEvent(ScreenDirection screenDirection) {
		return new FocusNavigationEvent.ArrowNavigation(screenDirection);
	}

	protected void setInitialFocus() {
		if (this.minecraft.getLastInputType().isKeyboard()) {
			FocusNavigationEvent.TabNavigation tabNavigation = new FocusNavigationEvent.TabNavigation(true);
			ComponentPath componentPath = super.nextFocusPath(tabNavigation);
			if (componentPath != null) {
				this.changeFocus(componentPath);
			}
		}
	}

	protected void setInitialFocus(GuiEventListener guiEventListener) {
		ComponentPath componentPath = ComponentPath.path(this, guiEventListener.nextFocusPath(new FocusNavigationEvent.InitialFocus()));
		if (componentPath != null) {
			this.changeFocus(componentPath);
		}
	}

	public void clearFocus() {
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

	protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T guiEventListener) {
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
			this.renderables.remove((Renderable)guiEventListener);
		}

		if (guiEventListener instanceof NarratableEntry) {
			this.narratables.remove((NarratableEntry)guiEventListener);
		}

		this.children.remove(guiEventListener);
	}

	protected void clearWidgets() {
		this.renderables.clear();
		this.children.clear();
		this.narratables.clear();
	}

	public static List<Component> getTooltipFromItem(Minecraft minecraft, ItemStack itemStack) {
		return itemStack.getTooltipLines(
			Item.TooltipContext.of(minecraft.level),
			minecraft.player,
			minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL
		);
	}

	protected void insertText(String string, boolean bl) {
	}

	public boolean handleComponentClicked(@Nullable Style style) {
		if (style == null) {
			return false;
		} else {
			ClickEvent clickEvent = style.getClickEvent();
			if (hasShiftDown()) {
				if (style.getInsertion() != null) {
					this.insertText(style.getInsertion(), false);
				}
			} else if (clickEvent != null) {
				if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
					if (!this.minecraft.options.chatLinks().get()) {
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

						if (this.minecraft.options.chatLinksPrompt().get()) {
							this.clickedLink = uRI;
							this.minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, clickEvent.getValue(), false));
						} else {
							this.openLink(uRI);
						}
					} catch (URISyntaxException var5) {
						LOGGER.error("Can't open url for {}", clickEvent, var5);
					}
				} else if (clickEvent.getAction() == ClickEvent.Action.OPEN_FILE) {
					URI uRIx = new File(clickEvent.getValue()).toURI();
					this.openLink(uRIx);
				} else if (clickEvent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
					this.insertText(StringUtil.filterText(clickEvent.getValue()), true);
				} else if (clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
					String string2 = StringUtil.filterText(clickEvent.getValue());
					if (string2.startsWith("/")) {
						if (!this.minecraft.player.connection.sendUnsignedCommand(string2.substring(1))) {
							LOGGER.error("Not allowed to run command with signed argument from click event: '{}'", string2);
						}
					} else {
						LOGGER.error("Failed to run command without '/' prefix from click event: '{}'", string2);
					}
				} else if (clickEvent.getAction() == ClickEvent.Action.COPY_TO_CLIPBOARD) {
					this.minecraft.keyboardHandler.setClipboard(clickEvent.getValue());
				} else {
					LOGGER.error("Don't know how to handle {}", clickEvent);
				}

				return true;
			}

			return false;
		}
	}

	public final void init(Minecraft minecraft, int i, int j) {
		this.minecraft = minecraft;
		this.font = minecraft.font;
		this.width = i;
		this.height = j;
		if (!this.initialized) {
			this.init();
			this.setInitialFocus();
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
		this.setInitialFocus();
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

	public void added() {
	}

	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.minecraft.level == null) {
			this.renderPanorama(guiGraphics, f);
		}

		this.renderBlurredBackground(f);
		this.renderMenuBackground(guiGraphics);
	}

	protected void renderBlurredBackground(float f) {
		this.minecraft.gameRenderer.processBlurEffect(f);
		this.minecraft.getMainRenderTarget().bindWrite(false);
	}

	protected void renderPanorama(GuiGraphics guiGraphics, float f) {
		PANORAMA.render(guiGraphics, this.width, this.height, 1.0F, f);
	}

	protected void renderMenuBackground(GuiGraphics guiGraphics) {
		this.renderMenuBackground(guiGraphics, 0, 0, this.width, this.height);
	}

	protected void renderMenuBackground(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		renderMenuBackgroundTexture(guiGraphics, this.minecraft.level == null ? MENU_BACKGROUND : INWORLD_MENU_BACKGROUND, i, j, 0.0F, 0.0F, k, l);
	}

	public static void renderMenuBackgroundTexture(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int i, int j, float f, float g, int k, int l) {
		int m = 32;
		RenderSystem.enableBlend();
		guiGraphics.blit(resourceLocation, i, j, 0, f, g, k, l, 32, 32);
		RenderSystem.disableBlend();
	}

	public void renderTransparentBackground(GuiGraphics guiGraphics) {
		guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
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
		return Minecraft.ON_OSX
			? InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 343)
				|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 347)
			: InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 341)
				|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 345);
	}

	public static boolean hasShiftDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340)
			|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
	}

	public static boolean hasAltDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 342)
			|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 346);
	}

	public static boolean isCut(int i) {
		return i == 88 && hasControlDown() && !hasShiftDown() && !hasAltDown();
	}

	public static boolean isPaste(int i) {
		return i == 86 && hasControlDown() && !hasShiftDown() && !hasAltDown();
	}

	public static boolean isCopy(int i) {
		return i == 67 && hasControlDown() && !hasShiftDown() && !hasAltDown();
	}

	public static boolean isSelectAll(int i) {
		return i == 65 && hasControlDown() && !hasShiftDown() && !hasAltDown();
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
		} catch (Throwable var6) {
			CrashReport crashReport = CrashReport.forThrowable(var6, string);
			CrashReportCategory crashReportCategory = crashReport.addCategory("Affected screen");
			crashReportCategory.setDetail("Screen name", (CrashReportDetail<String>)(() -> string2));
			throw new ReportedException(crashReport);
		}
	}

	protected boolean isValidCharacterForName(String string, char c, int i) {
		int j = string.indexOf(58);
		int k = string.indexOf(47);
		if (c == ':') {
			return (k == -1 || i <= k) && j == -1;
		} else {
			return c == '/' ? i > j : c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.';
		}
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
		if (this.shouldRunNarration()) {
			long l = Util.getMillis();
			if (l > this.nextNarrationTime && l > this.narrationSuppressTime) {
				this.runNarration(true);
				this.nextNarrationTime = Long.MAX_VALUE;
			}
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
		List<NarratableEntry> list = this.narratables
			.stream()
			.filter(NarratableEntry::isActive)
			.sorted(Comparator.comparingInt(TabOrderedElement::getTabOrderGroup))
			.toList();
		Screen.NarratableSearchResult narratableSearchResult = findNarratableWidget(list, this.lastNarratable);
		if (narratableSearchResult != null) {
			if (narratableSearchResult.priority.isTerminal()) {
				this.lastNarratable = narratableSearchResult.entry;
			}

			if (list.size() > 1) {
				narrationElementOutput.add(NarratedElementType.POSITION, Component.translatable("narrator.position.screen", narratableSearchResult.index + 1, list.size()));
				if (narratableSearchResult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
					narrationElementOutput.add(NarratedElementType.USAGE, this.getUsageNarration());
				}
			}

			narratableSearchResult.entry.updateNarration(narrationElementOutput.nest());
		}
	}

	protected Component getUsageNarration() {
		return Component.translatable("narration.component_list.usage");
	}

	@Nullable
	public static Screen.NarratableSearchResult findNarratableWidget(List<? extends NarratableEntry> list, @Nullable NarratableEntry narratableEntry) {
		Screen.NarratableSearchResult narratableSearchResult = null;
		Screen.NarratableSearchResult narratableSearchResult2 = null;
		int i = 0;

		for (int j = list.size(); i < j; i++) {
			NarratableEntry narratableEntry2 = (NarratableEntry)list.get(i);
			NarratableEntry.NarrationPriority narrationPriority = narratableEntry2.narrationPriority();
			if (narrationPriority.isTerminal()) {
				if (narratableEntry2 != narratableEntry) {
					return new Screen.NarratableSearchResult(narratableEntry2, i, narrationPriority);
				}

				narratableSearchResult2 = new Screen.NarratableSearchResult(narratableEntry2, i, narrationPriority);
			} else if (narrationPriority.compareTo(narratableSearchResult != null ? narratableSearchResult.priority : NarratableEntry.NarrationPriority.NONE) > 0) {
				narratableSearchResult = new Screen.NarratableSearchResult(narratableEntry2, i, narrationPriority);
			}
		}

		return narratableSearchResult != null ? narratableSearchResult : narratableSearchResult2;
	}

	public void narrationEnabled() {
		this.scheduleNarration(NARRATE_DELAY_NARRATOR_ENABLED, false);
	}

	protected void clearTooltipForNextRenderPass() {
		this.deferredTooltipRendering = null;
	}

	public void setTooltipForNextRenderPass(List<FormattedCharSequence> list) {
		this.setTooltipForNextRenderPass(list, DefaultTooltipPositioner.INSTANCE, true);
	}

	public void setTooltipForNextRenderPass(List<FormattedCharSequence> list, ClientTooltipPositioner clientTooltipPositioner, boolean bl) {
		if (this.deferredTooltipRendering == null || bl) {
			this.deferredTooltipRendering = new Screen.DeferredTooltipRendering(list, clientTooltipPositioner);
		}
	}

	public void setTooltipForNextRenderPass(Component component) {
		this.setTooltipForNextRenderPass(Tooltip.splitTooltip(this.minecraft, component));
	}

	public void setTooltipForNextRenderPass(Tooltip tooltip, ClientTooltipPositioner clientTooltipPositioner, boolean bl) {
		this.setTooltipForNextRenderPass(tooltip.toCharSequence(this.minecraft), clientTooltipPositioner, bl);
	}

	@Override
	public ScreenRectangle getRectangle() {
		return new ScreenRectangle(0, 0, this.width, this.height);
	}

	@Nullable
	public Music getBackgroundMusic() {
		return null;
	}

	@Environment(EnvType.CLIENT)
	static record DeferredTooltipRendering(List<FormattedCharSequence> tooltip, ClientTooltipPositioner positioner) {
	}

	@Environment(EnvType.CLIENT)
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
