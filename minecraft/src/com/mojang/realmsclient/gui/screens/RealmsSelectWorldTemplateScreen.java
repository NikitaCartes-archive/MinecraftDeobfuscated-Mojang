package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.TextRenderingUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
	static final Logger LOGGER = LogUtils.getLogger();
	static final ResourceLocation SLOT_FRAME_SPRITE = new ResourceLocation("widget/slot_frame");
	private static final Component SELECT_BUTTON_NAME = Component.translatable("mco.template.button.select");
	private static final Component TRAILER_BUTTON_NAME = Component.translatable("mco.template.button.trailer");
	private static final Component PUBLISHER_BUTTON_NAME = Component.translatable("mco.template.button.publisher");
	private static final int BUTTON_WIDTH = 100;
	private static final int BUTTON_SPACING = 10;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	final Consumer<WorldTemplate> callback;
	RealmsSelectWorldTemplateScreen.WorldTemplateList worldTemplateList;
	private final RealmsServer.WorldType worldType;
	private Button selectButton;
	private Button trailerButton;
	private Button publisherButton;
	@Nullable
	WorldTemplate selectedTemplate = null;
	@Nullable
	String currentLink;
	@Nullable
	private Component[] warning;
	@Nullable
	List<TextRenderingUtils.Line> noTemplatesMessage;

	public RealmsSelectWorldTemplateScreen(Component component, Consumer<WorldTemplate> consumer, RealmsServer.WorldType worldType) {
		this(component, consumer, worldType, null);
	}

	public RealmsSelectWorldTemplateScreen(
		Component component, Consumer<WorldTemplate> consumer, RealmsServer.WorldType worldType, @Nullable WorldTemplatePaginatedList worldTemplatePaginatedList
	) {
		super(component);
		this.callback = consumer;
		this.worldType = worldType;
		if (worldTemplatePaginatedList == null) {
			this.worldTemplateList = new RealmsSelectWorldTemplateScreen.WorldTemplateList(this);
			this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
		} else {
			this.worldTemplateList = new RealmsSelectWorldTemplateScreen.WorldTemplateList(this, Lists.newArrayList(worldTemplatePaginatedList.templates));
			this.fetchTemplatesAsync(worldTemplatePaginatedList);
		}
	}

	public void setWarning(Component... components) {
		this.warning = components;
	}

	@Override
	public void init() {
		this.layout.addTitleHeader(this.title, this.font);
		this.worldTemplateList = this.layout.addToContents(new RealmsSelectWorldTemplateScreen.WorldTemplateList(this, this.worldTemplateList.getTemplates()));
		LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(10));
		linearLayout.defaultCellSetting().alignHorizontallyCenter();
		this.trailerButton = linearLayout.addChild(Button.builder(TRAILER_BUTTON_NAME, button -> this.onTrailer()).width(100).build());
		this.selectButton = linearLayout.addChild(Button.builder(SELECT_BUTTON_NAME, button -> this.selectTemplate()).width(100).build());
		linearLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).width(100).build());
		this.publisherButton = linearLayout.addChild(Button.builder(PUBLISHER_BUTTON_NAME, button -> this.onPublish()).width(100).build());
		this.updateButtonStates();
		this.layout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.worldTemplateList.setSize(this.width, this.height - this.layout.getFooterHeight() - this.getHeaderHeight());
		this.layout.arrangeElements();
	}

	@Override
	public Component getNarrationMessage() {
		List<Component> list = Lists.<Component>newArrayListWithCapacity(2);
		list.add(this.title);
		if (this.warning != null) {
			list.addAll(Arrays.asList(this.warning));
		}

		return CommonComponents.joinLines(list);
	}

	void updateButtonStates() {
		this.publisherButton.visible = this.selectedTemplate != null && !this.selectedTemplate.link.isEmpty();
		this.trailerButton.visible = this.selectedTemplate != null && !this.selectedTemplate.trailer.isEmpty();
		this.selectButton.active = this.selectedTemplate != null;
	}

	@Override
	public void onClose() {
		this.callback.accept(null);
	}

	private void selectTemplate() {
		if (this.selectedTemplate != null) {
			this.callback.accept(this.selectedTemplate);
		}
	}

	private void onTrailer() {
		if (this.selectedTemplate != null && !this.selectedTemplate.trailer.isBlank()) {
			ConfirmLinkScreen.confirmLinkNow(this, this.selectedTemplate.trailer);
		}
	}

	private void onPublish() {
		if (this.selectedTemplate != null && !this.selectedTemplate.link.isBlank()) {
			ConfirmLinkScreen.confirmLinkNow(this, this.selectedTemplate.link);
		}
	}

	private void fetchTemplatesAsync(WorldTemplatePaginatedList worldTemplatePaginatedList) {
		(new Thread("realms-template-fetcher") {
				public void run() {
					WorldTemplatePaginatedList worldTemplatePaginatedList = worldTemplatePaginatedList;
					RealmsClient realmsClient = RealmsClient.create();

					while (worldTemplatePaginatedList != null) {
						Either<WorldTemplatePaginatedList, Exception> either = RealmsSelectWorldTemplateScreen.this.fetchTemplates(worldTemplatePaginatedList, realmsClient);
						worldTemplatePaginatedList = (WorldTemplatePaginatedList)RealmsSelectWorldTemplateScreen.this.minecraft
							.submit(
								() -> {
									if (either.right().isPresent()) {
										RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates", (Throwable)either.right().get());
										if (RealmsSelectWorldTemplateScreen.this.worldTemplateList.isEmpty()) {
											RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(I18n.get("mco.template.select.failure"));
										}

										return null;
									} else {
										WorldTemplatePaginatedList worldTemplatePaginatedListxxx = (WorldTemplatePaginatedList)either.left().get();

										for (WorldTemplate worldTemplate : worldTemplatePaginatedListxxx.templates) {
											RealmsSelectWorldTemplateScreen.this.worldTemplateList.addEntry(worldTemplate);
										}

										if (worldTemplatePaginatedListxxx.templates.isEmpty()) {
											if (RealmsSelectWorldTemplateScreen.this.worldTemplateList.isEmpty()) {
												String string = I18n.get("mco.template.select.none", "%link");
												TextRenderingUtils.LineSegment lineSegment = TextRenderingUtils.LineSegment.link(
													I18n.get("mco.template.select.none.linkTitle"), "https://aka.ms/MinecraftRealmsContentCreator"
												);
												RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(string, lineSegment);
											}

											return null;
										} else {
											return worldTemplatePaginatedListxxx;
										}
									}
								}
							)
							.join();
					}
				}
			})
			.start();
	}

	Either<WorldTemplatePaginatedList, Exception> fetchTemplates(WorldTemplatePaginatedList worldTemplatePaginatedList, RealmsClient realmsClient) {
		try {
			return Either.left(realmsClient.fetchWorldTemplates(worldTemplatePaginatedList.page + 1, worldTemplatePaginatedList.size, this.worldType));
		} catch (RealmsServiceException var4) {
			return Either.right(var4);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.currentLink = null;
		if (this.noTemplatesMessage != null) {
			this.renderMultilineMessage(guiGraphics, i, j, this.noTemplatesMessage);
		}

		if (this.warning != null) {
			for (int k = 0; k < this.warning.length; k++) {
				Component component = this.warning[k];
				guiGraphics.drawCenteredString(this.font, component, this.width / 2, row(-1 + k), -6250336);
			}
		}
	}

	private void renderMultilineMessage(GuiGraphics guiGraphics, int i, int j, List<TextRenderingUtils.Line> list) {
		for (int k = 0; k < list.size(); k++) {
			TextRenderingUtils.Line line = (TextRenderingUtils.Line)list.get(k);
			int l = row(4 + k);
			int m = line.segments.stream().mapToInt(lineSegmentx -> this.font.width(lineSegmentx.renderedText())).sum();
			int n = this.width / 2 - m / 2;

			for (TextRenderingUtils.LineSegment lineSegment : line.segments) {
				int o = lineSegment.isLink() ? 3368635 : -1;
				int p = guiGraphics.drawString(this.font, lineSegment.renderedText(), n, l, o);
				if (lineSegment.isLink() && i > n && i < p && j > l - 3 && j < l + 8) {
					this.setTooltipForNextRenderPass(Component.literal(lineSegment.getLinkUrl()));
					this.currentLink = lineSegment.getLinkUrl();
				}

				n = p;
			}
		}
	}

	int getHeaderHeight() {
		return this.warning != null ? row(1) : 33;
	}

	@Environment(EnvType.CLIENT)
	class Entry extends ObjectSelectionList.Entry<RealmsSelectWorldTemplateScreen.Entry> {
		private static final WidgetSprites WEBSITE_LINK_SPRITES = new WidgetSprites(new ResourceLocation("icon/link"), new ResourceLocation("icon/link_highlighted"));
		private static final WidgetSprites TRAILER_LINK_SPRITES = new WidgetSprites(
			new ResourceLocation("icon/video_link"), new ResourceLocation("icon/video_link_highlighted")
		);
		private static final Component PUBLISHER_LINK_TOOLTIP = Component.translatable("mco.template.info.tooltip");
		private static final Component TRAILER_LINK_TOOLTIP = Component.translatable("mco.template.trailer.tooltip");
		public final WorldTemplate template;
		private long lastClickTime;
		@Nullable
		private ImageButton websiteButton;
		@Nullable
		private ImageButton trailerButton;

		public Entry(final WorldTemplate worldTemplate) {
			this.template = worldTemplate;
			if (!worldTemplate.link.isBlank()) {
				this.websiteButton = new ImageButton(
					15, 15, WEBSITE_LINK_SPRITES, ConfirmLinkScreen.confirmLink(RealmsSelectWorldTemplateScreen.this, worldTemplate.link), PUBLISHER_LINK_TOOLTIP
				);
				this.websiteButton.setTooltip(Tooltip.create(PUBLISHER_LINK_TOOLTIP));
			}

			if (!worldTemplate.trailer.isBlank()) {
				this.trailerButton = new ImageButton(
					15, 15, TRAILER_LINK_SPRITES, ConfirmLinkScreen.confirmLink(RealmsSelectWorldTemplateScreen.this, worldTemplate.trailer), TRAILER_LINK_TOOLTIP
				);
				this.trailerButton.setTooltip(Tooltip.create(TRAILER_LINK_TOOLTIP));
			}
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.template;
			RealmsSelectWorldTemplateScreen.this.updateButtonStates();
			if (Util.getMillis() - this.lastClickTime < 250L && this.isFocused()) {
				RealmsSelectWorldTemplateScreen.this.callback.accept(this.template);
			}

			this.lastClickTime = Util.getMillis();
			if (this.websiteButton != null) {
				this.websiteButton.mouseClicked(d, e, i);
			}

			if (this.trailerButton != null) {
				this.trailerButton.mouseClicked(d, e, i);
			}

			return super.mouseClicked(d, e, i);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			guiGraphics.blit(RealmsTextureManager.worldTemplate(this.template.id, this.template.image), k + 1, j + 1 + 1, 0.0F, 0.0F, 38, 38, 38, 38);
			guiGraphics.blitSprite(RealmsSelectWorldTemplateScreen.SLOT_FRAME_SPRITE, k, j + 1, 40, 40);
			int p = 5;
			int q = RealmsSelectWorldTemplateScreen.this.font.width(this.template.version);
			if (this.websiteButton != null) {
				this.websiteButton.setPosition(k + l - q - this.websiteButton.getWidth() - 10, j);
				this.websiteButton.render(guiGraphics, n, o, f);
			}

			if (this.trailerButton != null) {
				this.trailerButton.setPosition(k + l - q - this.trailerButton.getWidth() * 2 - 15, j);
				this.trailerButton.render(guiGraphics, n, o, f);
			}

			int r = k + 45 + 20;
			int s = j + 5;
			guiGraphics.drawString(RealmsSelectWorldTemplateScreen.this.font, this.template.name, r, s, -1, false);
			guiGraphics.drawString(RealmsSelectWorldTemplateScreen.this.font, this.template.version, k + l - q - 5, s, 7105644, false);
			guiGraphics.drawString(RealmsSelectWorldTemplateScreen.this.font, this.template.author, r, s + 9 + 5, -6250336, false);
			if (!this.template.recommendedPlayers.isBlank()) {
				guiGraphics.drawString(RealmsSelectWorldTemplateScreen.this.font, this.template.recommendedPlayers, r, j + m - 9 / 2 - 5, 5000268, false);
			}
		}

		@Override
		public Component getNarration() {
			Component component = CommonComponents.joinLines(
				Component.literal(this.template.name),
				Component.translatable("mco.template.select.narrate.authors", this.template.author),
				Component.literal(this.template.recommendedPlayers),
				Component.translatable("mco.template.select.narrate.version", this.template.version)
			);
			return Component.translatable("narrator.select", component);
		}
	}

	@Environment(EnvType.CLIENT)
	class WorldTemplateList extends RealmsObjectSelectionList<RealmsSelectWorldTemplateScreen.Entry> {
		public WorldTemplateList(final RealmsSelectWorldTemplateScreen realmsSelectWorldTemplateScreen) {
			this(realmsSelectWorldTemplateScreen, Collections.emptyList());
		}

		public WorldTemplateList(final Iterable<WorldTemplate> realmsSelectWorldTemplateScreen, final Iterable iterable) {
			super(
				realmsSelectWorldTemplateScreen.width,
				realmsSelectWorldTemplateScreen.height - 33 - realmsSelectWorldTemplateScreen.getHeaderHeight(),
				realmsSelectWorldTemplateScreen.getHeaderHeight(),
				46
			);
			this.this$0 = realmsSelectWorldTemplateScreen;
			iterable.forEach(this::addEntry);
		}

		public void addEntry(WorldTemplate worldTemplate) {
			this.addEntry(this.this$0.new Entry(worldTemplate));
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (this.this$0.currentLink != null) {
				ConfirmLinkScreen.confirmLinkNow(this.this$0, this.this$0.currentLink);
				return true;
			} else {
				return super.mouseClicked(d, e, i);
			}
		}

		public void setSelected(@Nullable RealmsSelectWorldTemplateScreen.Entry entry) {
			super.setSelected(entry);
			this.this$0.selectedTemplate = entry == null ? null : entry.template;
			this.this$0.updateButtonStates();
		}

		@Override
		public int getMaxPosition() {
			return this.getItemCount() * 46;
		}

		@Override
		public int getRowWidth() {
			return 300;
		}

		public boolean isEmpty() {
			return this.getItemCount() == 0;
		}

		public List<WorldTemplate> getTemplates() {
			return (List<WorldTemplate>)this.children().stream().map(entry -> entry.template).collect(Collectors.toList());
		}
	}
}
