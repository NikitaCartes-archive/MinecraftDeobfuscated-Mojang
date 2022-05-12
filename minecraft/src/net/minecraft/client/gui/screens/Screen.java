package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
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
import com.mojang.math.Matrix4f;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.ScreenNarrationCollector;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
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
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public abstract class Screen extends AbstractContainerEventHandler implements Widget {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Set<String> ALLOWED_PROTOCOLS = Sets.<String>newHashSet("http", "https");
	private static final int EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE = 2;
	private static final Component USAGE_NARRATION = Component.translatable("narrator.screen.usage");
	protected final Component title;
	private final List<GuiEventListener> children = Lists.<GuiEventListener>newArrayList();
	private final List<NarratableEntry> narratables = Lists.<NarratableEntry>newArrayList();
	@Nullable
	protected Minecraft minecraft;
	protected ItemRenderer itemRenderer;
	public int width;
	public int height;
	private final List<Widget> renderables = Lists.<Widget>newArrayList();
	public boolean passEvents;
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

	protected Screen(Component component) {
		this.title = component;
	}

	public Component getTitle() {
		return this.title;
	}

	public Component getNarrationMessage() {
		return this.getTitle();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		for (Widget widget : this.renderables) {
			widget.render(poseStack, i, j, f);
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256 && this.shouldCloseOnEsc()) {
			this.onClose();
			return true;
		} else if (i == 258) {
			boolean bl = !hasShiftDown();
			if (!this.changeFocus(bl)) {
				this.changeFocus(bl);
			}

			return false;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	public boolean shouldCloseOnEsc() {
		return true;
	}

	public void onClose() {
		this.minecraft.setScreen(null);
	}

	protected <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T guiEventListener) {
		this.renderables.add(guiEventListener);
		return this.addWidget(guiEventListener);
	}

	protected <T extends Widget> T addRenderableOnly(T widget) {
		this.renderables.add(widget);
		return widget;
	}

	protected <T extends GuiEventListener & NarratableEntry> T addWidget(T guiEventListener) {
		this.children.add(guiEventListener);
		this.narratables.add(guiEventListener);
		return guiEventListener;
	}

	protected void removeWidget(GuiEventListener guiEventListener) {
		if (guiEventListener instanceof Widget) {
			this.renderables.remove((Widget)guiEventListener);
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

	protected void renderTooltip(PoseStack poseStack, ItemStack itemStack, int i, int j) {
		this.renderTooltip(poseStack, this.getTooltipFromItem(itemStack), itemStack.getTooltipImage(), i, j);
	}

	public void renderTooltip(PoseStack poseStack, List<Component> list, Optional<TooltipComponent> optional, int i, int j) {
		List<ClientTooltipComponent> list2 = (List<ClientTooltipComponent>)list.stream()
			.map(Component::getVisualOrderText)
			.map(ClientTooltipComponent::create)
			.collect(Collectors.toList());
		optional.ifPresent(tooltipComponent -> list2.add(1, ClientTooltipComponent.create(tooltipComponent)));
		this.renderTooltipInternal(poseStack, list2, i, j);
	}

	public List<Component> getTooltipFromItem(ItemStack itemStack) {
		return itemStack.getTooltipLines(
			this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL
		);
	}

	public void renderTooltip(PoseStack poseStack, Component component, int i, int j) {
		this.renderTooltip(poseStack, Arrays.asList(component.getVisualOrderText()), i, j);
	}

	public void renderComponentTooltip(PoseStack poseStack, List<Component> list, int i, int j) {
		this.renderTooltip(poseStack, Lists.transform(list, Component::getVisualOrderText), i, j);
	}

	public void renderTooltip(PoseStack poseStack, List<? extends FormattedCharSequence> list, int i, int j) {
		this.renderTooltipInternal(poseStack, (List<ClientTooltipComponent>)list.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), i, j);
	}

	private void renderTooltipInternal(PoseStack poseStack, List<ClientTooltipComponent> list, int i, int j) {
		if (!list.isEmpty()) {
			int k = 0;
			int l = list.size() == 1 ? -2 : 0;

			for (ClientTooltipComponent clientTooltipComponent : list) {
				int m = clientTooltipComponent.getWidth(this.font);
				if (m > k) {
					k = m;
				}

				l += clientTooltipComponent.getHeight();
			}

			int n = i + 12;
			int o = j - 12;
			if (n + k > this.width) {
				n -= 28 + k;
			}

			if (o + l + 6 > this.height) {
				o = this.height - l - 6;
			}

			if (j - l - 8 < 0) {
				o = j + 8;
			}

			poseStack.pushPose();
			int q = -267386864;
			int r = 1347420415;
			int s = 1344798847;
			int t = 400;
			float f = this.itemRenderer.blitOffset;
			this.itemRenderer.blitOffset = 400.0F;
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			Matrix4f matrix4f = poseStack.last().pose();
			fillGradient(matrix4f, bufferBuilder, n - 3, o - 4, n + k + 3, o - 3, 400, -267386864, -267386864);
			fillGradient(matrix4f, bufferBuilder, n - 3, o + l + 3, n + k + 3, o + l + 4, 400, -267386864, -267386864);
			fillGradient(matrix4f, bufferBuilder, n - 3, o - 3, n + k + 3, o + l + 3, 400, -267386864, -267386864);
			fillGradient(matrix4f, bufferBuilder, n - 4, o - 3, n - 3, o + l + 3, 400, -267386864, -267386864);
			fillGradient(matrix4f, bufferBuilder, n + k + 3, o - 3, n + k + 4, o + l + 3, 400, -267386864, -267386864);
			fillGradient(matrix4f, bufferBuilder, n - 3, o - 3 + 1, n - 3 + 1, o + l + 3 - 1, 400, 1347420415, 1344798847);
			fillGradient(matrix4f, bufferBuilder, n + k + 2, o - 3 + 1, n + k + 3, o + l + 3 - 1, 400, 1347420415, 1344798847);
			fillGradient(matrix4f, bufferBuilder, n - 3, o - 3, n + k + 3, o - 3 + 1, 400, 1347420415, 1347420415);
			fillGradient(matrix4f, bufferBuilder, n - 3, o + l + 2, n + k + 3, o + l + 3, 400, 1344798847, 1344798847);
			RenderSystem.enableDepthTest();
			RenderSystem.disableTexture();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			BufferUploader.drawWithShader(bufferBuilder.end());
			RenderSystem.disableBlend();
			RenderSystem.enableTexture();
			MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			poseStack.translate(0.0, 0.0, 400.0);
			int u = o;

			for (int v = 0; v < list.size(); v++) {
				ClientTooltipComponent clientTooltipComponent2 = (ClientTooltipComponent)list.get(v);
				clientTooltipComponent2.renderText(this.font, n, u, matrix4f, bufferSource);
				u += clientTooltipComponent2.getHeight() + (v == 0 ? 2 : 0);
			}

			bufferSource.endBatch();
			poseStack.popPose();
			u = o;

			for (int v = 0; v < list.size(); v++) {
				ClientTooltipComponent clientTooltipComponent2 = (ClientTooltipComponent)list.get(v);
				clientTooltipComponent2.renderImage(this.font, n, u, poseStack, this.itemRenderer, 400);
				u += clientTooltipComponent2.getHeight() + (v == 0 ? 2 : 0);
			}

			this.itemRenderer.blitOffset = f;
		}
	}

	protected void renderComponentHoverEffect(PoseStack poseStack, @Nullable Style style, int i, int j) {
		if (style != null && style.getHoverEvent() != null) {
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
					this.insertText(clickEvent.getValue(), true);
				} else if (clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
					this.minecraft.player.command(clickEvent.getValue());
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
		this.itemRenderer = minecraft.getItemRenderer();
		this.font = minecraft.font;
		this.width = i;
		this.height = j;
		this.rebuildWidgets();
		this.triggerImmediateNarration(false);
		this.suppressNarration(NARRATE_SUPPRESS_AFTER_INIT_TIME);
	}

	protected void rebuildWidgets() {
		this.clearWidgets();
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
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(0.0, (double)this.height, 0.0).uv(0.0F, (float)this.height / 32.0F + (float)i).color(64, 64, 64, 255).endVertex();
		bufferBuilder.vertex((double)this.width, (double)this.height, 0.0)
			.uv((float)this.width / 32.0F, (float)this.height / 32.0F + (float)i)
			.color(64, 64, 64, 255)
			.endVertex();
		bufferBuilder.vertex((double)this.width, 0.0, 0.0).uv((float)this.width / 32.0F, (float)i).color(64, 64, 64, 255).endVertex();
		bufferBuilder.vertex(0.0, 0.0, 0.0).uv(0.0F, (float)i).color(64, 64, 64, 255).endVertex();
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

	public void resize(Minecraft minecraft, int i, int j) {
		this.init(minecraft, i, j);
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
		return NarratorChatListener.INSTANCE.isActive();
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
			NarratorChatListener.INSTANCE.sayNow(string);
		}
	}

	protected void updateNarrationState(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.getNarrationMessage());
		narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
		this.updateNarratedWidget(narrationElementOutput);
	}

	protected void updateNarratedWidget(NarrationElementOutput narrationElementOutput) {
		ImmutableList<NarratableEntry> immutableList = (ImmutableList<NarratableEntry>)this.narratables
			.stream()
			.filter(NarratableEntry::isActive)
			.collect(ImmutableList.toImmutableList());
		Screen.NarratableSearchResult narratableSearchResult = findNarratableWidget(immutableList, this.lastNarratable);
		if (narratableSearchResult != null) {
			if (narratableSearchResult.priority.isTerminal()) {
				this.lastNarratable = narratableSearchResult.entry;
			}

			if (immutableList.size() > 1) {
				narrationElementOutput.add(
					NarratedElementType.POSITION, Component.translatable("narrator.position.screen", narratableSearchResult.index + 1, immutableList.size())
				);
				if (narratableSearchResult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
					narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.component_list.usage"));
				}
			}

			narratableSearchResult.entry.updateNarration(narrationElementOutput.nest());
		}
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

	protected static void hideWidgets(AbstractWidget... abstractWidgets) {
		for (AbstractWidget abstractWidget : abstractWidgets) {
			abstractWidget.visible = false;
		}
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
