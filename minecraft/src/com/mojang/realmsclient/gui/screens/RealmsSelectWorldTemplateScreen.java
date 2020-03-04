package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation LINK_ICON = new ResourceLocation("realms", "textures/gui/realms/link_icons.png");
	private static final ResourceLocation TRAILER_ICON = new ResourceLocation("realms", "textures/gui/realms/trailer_icons.png");
	private static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
	private final RealmsScreenWithCallback lastScreen;
	private RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList worldTemplateObjectSelectionList;
	private int selectedTemplate = -1;
	private String title;
	private Button selectButton;
	private Button trailerButton;
	private Button publisherButton;
	private String toolTip;
	private String currentLink;
	private final RealmsServer.WorldType worldType;
	private int clicks;
	private String warning;
	private String warningURL;
	private boolean displayWarning;
	private boolean hoverWarning;
	private List<TextRenderingUtils.Line> noTemplatesMessage;

	public RealmsSelectWorldTemplateScreen(RealmsScreenWithCallback realmsScreenWithCallback, RealmsServer.WorldType worldType) {
		this(realmsScreenWithCallback, worldType, null);
	}

	public RealmsSelectWorldTemplateScreen(
		RealmsScreenWithCallback realmsScreenWithCallback, RealmsServer.WorldType worldType, @Nullable WorldTemplatePaginatedList worldTemplatePaginatedList
	) {
		this.lastScreen = realmsScreenWithCallback;
		this.worldType = worldType;
		if (worldTemplatePaginatedList == null) {
			this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList();
			this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
		} else {
			this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(
				Lists.<WorldTemplate>newArrayList(worldTemplatePaginatedList.templates)
			);
			this.fetchTemplatesAsync(worldTemplatePaginatedList);
		}

		this.title = I18n.get("mco.template.title");
	}

	public void setTitle(String string) {
		this.title = string;
	}

	public void setWarning(String string) {
		this.warning = string;
		this.displayWarning = true;
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.hoverWarning && this.warningURL != null) {
			Util.getPlatform().openUri("https://beta.minecraft.net/realms/adventure-maps-in-1-9");
			return true;
		} else {
			return super.mouseClicked(d, e, i);
		}
	}

	@Override
	public void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(
			this.worldTemplateObjectSelectionList.getTemplates()
		);
		this.trailerButton = this.addButton(
			new Button(this.width / 2 - 206, this.height - 32, 100, 20, I18n.get("mco.template.button.trailer"), buttonx -> this.onTrailer())
		);
		this.selectButton = this.addButton(
			new Button(this.width / 2 - 100, this.height - 32, 100, 20, I18n.get("mco.template.button.select"), buttonx -> this.selectTemplate())
		);
		String string = this.worldType == RealmsServer.WorldType.MINIGAME ? "gui.cancel" : "gui.back";
		Button button = new Button(this.width / 2 + 6, this.height - 32, 100, 20, I18n.get(string), buttonx -> this.backButtonClicked());
		this.addButton(button);
		this.publisherButton = this.addButton(
			new Button(this.width / 2 + 112, this.height - 32, 100, 20, I18n.get("mco.template.button.publisher"), buttonx -> this.onPublish())
		);
		this.selectButton.active = false;
		this.trailerButton.visible = false;
		this.publisherButton.visible = false;
		this.addWidget(this.worldTemplateObjectSelectionList);
		this.magicalSpecialHackyFocus(this.worldTemplateObjectSelectionList);
		NarrationHelper.now((Iterable<String>)Stream.of(this.title, this.warning).filter(Objects::nonNull).collect(Collectors.toList()));
	}

	private void updateButtonStates() {
		this.publisherButton.visible = this.shouldPublisherBeVisible();
		this.trailerButton.visible = this.shouldTrailerBeVisible();
		this.selectButton.active = this.shouldSelectButtonBeActive();
	}

	private boolean shouldSelectButtonBeActive() {
		return this.selectedTemplate != -1;
	}

	private boolean shouldPublisherBeVisible() {
		return this.selectedTemplate != -1 && !this.getSelectedTemplate().link.isEmpty();
	}

	private WorldTemplate getSelectedTemplate() {
		return this.worldTemplateObjectSelectionList.get(this.selectedTemplate);
	}

	private boolean shouldTrailerBeVisible() {
		return this.selectedTemplate != -1 && !this.getSelectedTemplate().trailer.isEmpty();
	}

	@Override
	public void tick() {
		super.tick();
		this.clicks--;
		if (this.clicks < 0) {
			this.clicks = 0;
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.backButtonClicked();
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	private void backButtonClicked() {
		this.lastScreen.callback(null);
		this.minecraft.setScreen(this.lastScreen);
	}

	private void selectTemplate() {
		if (this.hasValidTemplate()) {
			this.lastScreen.callback(this.getSelectedTemplate());
		}
	}

	private boolean hasValidTemplate() {
		return this.selectedTemplate >= 0 && this.selectedTemplate < this.worldTemplateObjectSelectionList.getItemCount();
	}

	private void onTrailer() {
		if (this.hasValidTemplate()) {
			WorldTemplate worldTemplate = this.getSelectedTemplate();
			if (!"".equals(worldTemplate.trailer)) {
				Util.getPlatform().openUri(worldTemplate.trailer);
			}
		}
	}

	private void onPublish() {
		if (this.hasValidTemplate()) {
			WorldTemplate worldTemplate = this.getSelectedTemplate();
			if (!"".equals(worldTemplate.link)) {
				Util.getPlatform().openUri(worldTemplate.link);
			}
		}
	}

	private void fetchTemplatesAsync(WorldTemplatePaginatedList worldTemplatePaginatedList) {
		(new Thread("realms-template-fetcher") {
				public void run() {
					WorldTemplatePaginatedList worldTemplatePaginatedList = worldTemplatePaginatedList;
					RealmsClient realmsClient = RealmsClient.create();

					while (worldTemplatePaginatedList != null) {
						Either<WorldTemplatePaginatedList, String> either = RealmsSelectWorldTemplateScreen.this.fetchTemplates(worldTemplatePaginatedList, realmsClient);
						worldTemplatePaginatedList = (WorldTemplatePaginatedList)RealmsSelectWorldTemplateScreen.this.minecraft
							.submit(
								() -> {
									if (either.right().isPresent()) {
										RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates: {}", either.right().get());
										if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
											RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(I18n.get("mco.template.select.failure"));
										}

										return null;
									} else {
										WorldTemplatePaginatedList worldTemplatePaginatedListxxx = (WorldTemplatePaginatedList)either.left().get();

										for (WorldTemplate worldTemplate : worldTemplatePaginatedListxxx.templates) {
											RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.addEntry(worldTemplate);
										}

										if (worldTemplatePaginatedListxxx.templates.isEmpty()) {
											if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
												String string = I18n.get("mco.template.select.none", "%link");
												TextRenderingUtils.LineSegment lineSegment = TextRenderingUtils.LineSegment.link(
													I18n.get("mco.template.select.none.linkTitle"), "https://minecraft.net/realms/content-creator/"
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

	private Either<WorldTemplatePaginatedList, String> fetchTemplates(WorldTemplatePaginatedList worldTemplatePaginatedList, RealmsClient realmsClient) {
		try {
			return Either.left(realmsClient.fetchWorldTemplates(worldTemplatePaginatedList.page + 1, worldTemplatePaginatedList.size, this.worldType));
		} catch (RealmsServiceException var4) {
			return Either.right(var4.getMessage());
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.toolTip = null;
		this.currentLink = null;
		this.hoverWarning = false;
		this.renderBackground();
		this.worldTemplateObjectSelectionList.render(i, j, f);
		if (this.noTemplatesMessage != null) {
			this.renderMultilineMessage(i, j, this.noTemplatesMessage);
		}

		this.drawCenteredString(this.font, this.title, this.width / 2, 13, 16777215);
		if (this.displayWarning) {
			String[] strings = this.warning.split("\\\\n");

			for (int k = 0; k < strings.length; k++) {
				int l = this.font.width(strings[k]);
				int m = this.width / 2 - l / 2;
				int n = row(-1 + k);
				if (i >= m && i <= m + l && j >= n && j <= n + 9) {
					this.hoverWarning = true;
				}
			}

			for (int kx = 0; kx < strings.length; kx++) {
				String string = strings[kx];
				int m = 10526880;
				if (this.warningURL != null) {
					if (this.hoverWarning) {
						m = 7107012;
						string = ChatFormatting.STRIKETHROUGH + string;
					} else {
						m = 3368635;
					}
				}

				this.drawCenteredString(this.font, string, this.width / 2, row(-1 + kx), m);
			}
		}

		super.render(i, j, f);
		if (this.toolTip != null) {
			this.renderMousehoverTooltip(this.toolTip, i, j);
		}
	}

	private void renderMultilineMessage(int i, int j, List<TextRenderingUtils.Line> list) {
		for (int k = 0; k < list.size(); k++) {
			TextRenderingUtils.Line line = (TextRenderingUtils.Line)list.get(k);
			int l = row(4 + k);
			int m = line.segments.stream().mapToInt(lineSegmentx -> this.font.width(lineSegmentx.renderedText())).sum();
			int n = this.width / 2 - m / 2;

			for (TextRenderingUtils.LineSegment lineSegment : line.segments) {
				int o = lineSegment.isLink() ? 3368635 : 16777215;
				int p = this.font.drawShadow(lineSegment.renderedText(), (float)n, (float)l, o);
				if (lineSegment.isLink() && i > n && i < p && j > l - 3 && j < l + 8) {
					this.toolTip = lineSegment.getLinkUrl();
					this.currentLink = lineSegment.getLinkUrl();
				}

				n = p;
			}
		}
	}

	protected void renderMousehoverTooltip(String string, int i, int j) {
		if (string != null) {
			int k = i + 12;
			int l = j - 12;
			int m = this.font.width(string);
			this.fillGradient(k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
			this.font.drawShadow(string, (float)k, (float)l, 16777215);
		}
	}

	@Environment(EnvType.CLIENT)
	class Entry extends ObjectSelectionList.Entry<RealmsSelectWorldTemplateScreen.Entry> {
		private final WorldTemplate template;

		public Entry(WorldTemplate worldTemplate) {
			this.template = worldTemplate;
		}

		@Override
		public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderWorldTemplateItem(this.template, k, j, n, o);
		}

		private void renderWorldTemplateItem(WorldTemplate worldTemplate, int i, int j, int k, int l) {
			int m = i + 45 + 20;
			RealmsSelectWorldTemplateScreen.this.font.draw(worldTemplate.name, (float)m, (float)(j + 2), 16777215);
			RealmsSelectWorldTemplateScreen.this.font.draw(worldTemplate.author, (float)m, (float)(j + 15), 7105644);
			RealmsSelectWorldTemplateScreen.this.font
				.draw(worldTemplate.version, (float)(m + 227 - RealmsSelectWorldTemplateScreen.this.font.width(worldTemplate.version)), (float)(j + 1), 7105644);
			if (!"".equals(worldTemplate.link) || !"".equals(worldTemplate.trailer) || !"".equals(worldTemplate.recommendedPlayers)) {
				this.drawIcons(m - 1, j + 25, k, l, worldTemplate.link, worldTemplate.trailer, worldTemplate.recommendedPlayers);
			}

			this.drawImage(i, j + 1, k, l, worldTemplate);
		}

		private void drawImage(int i, int j, int k, int l, WorldTemplate worldTemplate) {
			RealmsTextureManager.bindWorldTemplate(worldTemplate.id, worldTemplate.image);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GuiComponent.blit(i + 1, j + 1, 0.0F, 0.0F, 38, 38, 38, 38);
			RealmsSelectWorldTemplateScreen.this.minecraft.getTextureManager().bind(RealmsSelectWorldTemplateScreen.SLOT_FRAME_LOCATION);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GuiComponent.blit(i, j, 0.0F, 0.0F, 40, 40, 40, 40);
		}

		private void drawIcons(int i, int j, int k, int l, String string, String string2, String string3) {
			if (!"".equals(string3)) {
				RealmsSelectWorldTemplateScreen.this.font.draw(string3, (float)i, (float)(j + 4), 5000268);
			}

			int m = "".equals(string3) ? 0 : RealmsSelectWorldTemplateScreen.this.font.width(string3) + 2;
			boolean bl = false;
			boolean bl2 = false;
			if (k >= i + m && k <= i + m + 32 && l >= j && l <= j + 15 && l < RealmsSelectWorldTemplateScreen.this.height - 15 && l > 32) {
				if (k <= i + 15 + m && k > m) {
					if ("".equals(string)) {
						bl2 = true;
					} else {
						bl = true;
					}
				} else if (!"".equals(string)) {
					bl2 = true;
				}
			}

			if (!"".equals(string)) {
				RealmsSelectWorldTemplateScreen.this.minecraft.getTextureManager().bind(RealmsSelectWorldTemplateScreen.LINK_ICON);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.pushMatrix();
				RenderSystem.scalef(1.0F, 1.0F, 1.0F);
				float f = bl ? 15.0F : 0.0F;
				GuiComponent.blit(i + m, j, f, 0.0F, 15, 15, 30, 15);
				RenderSystem.popMatrix();
			}

			if (!"".equals(string2)) {
				RealmsSelectWorldTemplateScreen.this.minecraft.getTextureManager().bind(RealmsSelectWorldTemplateScreen.TRAILER_ICON);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.pushMatrix();
				RenderSystem.scalef(1.0F, 1.0F, 1.0F);
				int n = i + m + ("".equals(string) ? 0 : 17);
				float g = bl2 ? 15.0F : 0.0F;
				GuiComponent.blit(n, j, g, 0.0F, 15, 15, 30, 15);
				RenderSystem.popMatrix();
			}

			if (bl && !"".equals(string)) {
				RealmsSelectWorldTemplateScreen.this.toolTip = I18n.get("mco.template.info.tooltip");
				RealmsSelectWorldTemplateScreen.this.currentLink = string;
			} else if (bl2 && !"".equals(string2)) {
				RealmsSelectWorldTemplateScreen.this.toolTip = I18n.get("mco.template.trailer.tooltip");
				RealmsSelectWorldTemplateScreen.this.currentLink = string2;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class WorldTemplateObjectSelectionList extends RealmsObjectSelectionList<RealmsSelectWorldTemplateScreen.Entry> {
		public WorldTemplateObjectSelectionList() {
			this(Collections.emptyList());
		}

		public WorldTemplateObjectSelectionList(Iterable<WorldTemplate> iterable) {
			super(
				RealmsSelectWorldTemplateScreen.this.width,
				RealmsSelectWorldTemplateScreen.this.height,
				RealmsSelectWorldTemplateScreen.this.displayWarning ? RealmsSelectWorldTemplateScreen.row(1) : 32,
				RealmsSelectWorldTemplateScreen.this.height - 40,
				46
			);
			iterable.forEach(this::addEntry);
		}

		public void addEntry(WorldTemplate worldTemplate) {
			this.addEntry(RealmsSelectWorldTemplateScreen.this.new Entry(worldTemplate));
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (i == 0 && e >= (double)this.y0 && e <= (double)this.y1) {
				int j = this.width / 2 - 150;
				if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
					Util.getPlatform().openUri(RealmsSelectWorldTemplateScreen.this.currentLink);
				}

				int k = (int)Math.floor(e - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
				int l = k / this.itemHeight;
				if (d >= (double)j && d < (double)this.getScrollbarPosition() && l >= 0 && k >= 0 && l < this.getItemCount()) {
					this.selectItem(l);
					this.itemClicked(k, l, d, e, this.width);
					if (l >= RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount()) {
						return super.mouseClicked(d, e, i);
					}

					RealmsSelectWorldTemplateScreen.this.clicks = RealmsSelectWorldTemplateScreen.this.clicks + 7;
					if (RealmsSelectWorldTemplateScreen.this.clicks >= 10) {
						RealmsSelectWorldTemplateScreen.this.selectTemplate();
					}

					return true;
				}
			}

			return super.mouseClicked(d, e, i);
		}

		@Override
		public void selectItem(int i) {
			this.setSelectedItem(i);
			if (i != -1) {
				WorldTemplate worldTemplate = RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.get(i);
				String string = I18n.get("narrator.select.list.position", i + 1, RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount());
				String string2 = I18n.get("mco.template.select.narrate.version", worldTemplate.version);
				String string3 = I18n.get("mco.template.select.narrate.authors", worldTemplate.author);
				String string4 = NarrationHelper.join(Arrays.asList(worldTemplate.name, string3, worldTemplate.recommendedPlayers, string2, string));
				NarrationHelper.now(I18n.get("narrator.select", string4));
			}
		}

		public void setSelected(@Nullable RealmsSelectWorldTemplateScreen.Entry entry) {
			super.setSelected(entry);
			RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.children().indexOf(entry);
			RealmsSelectWorldTemplateScreen.this.updateButtonStates();
		}

		@Override
		public int getMaxPosition() {
			return this.getItemCount() * 46;
		}

		@Override
		public int getRowWidth() {
			return 300;
		}

		@Override
		public void renderBackground() {
			RealmsSelectWorldTemplateScreen.this.renderBackground();
		}

		@Override
		public boolean isFocused() {
			return RealmsSelectWorldTemplateScreen.this.getFocused() == this;
		}

		public boolean isEmpty() {
			return this.getItemCount() == 0;
		}

		public WorldTemplate get(int i) {
			return ((RealmsSelectWorldTemplateScreen.Entry)this.children().get(i)).template;
		}

		public List<WorldTemplate> getTemplates() {
			return (List<WorldTemplate>)this.children().stream().map(entry -> entry.template).collect(Collectors.toList());
		}
	}
}
