package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
	static final Logger LOGGER = LogManager.getLogger();
	static final ResourceLocation LINK_ICON = new ResourceLocation("realms", "textures/gui/realms/link_icons.png");
	static final ResourceLocation TRAILER_ICON = new ResourceLocation("realms", "textures/gui/realms/trailer_icons.png");
	static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
	static final Component PUBLISHER_LINK_TOOLTIP = new TranslatableComponent("mco.template.info.tooltip");
	static final Component TRAILER_LINK_TOOLTIP = new TranslatableComponent("mco.template.trailer.tooltip");
	private final Consumer<WorldTemplate> callback;
	RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList worldTemplateObjectSelectionList;
	int selectedTemplate = -1;
	private Button selectButton;
	private Button trailerButton;
	private Button publisherButton;
	@Nullable
	Component toolTip;
	String currentLink;
	private final RealmsServer.WorldType worldType;
	int clicks;
	@Nullable
	private Component[] warning;
	private String warningURL;
	boolean displayWarning;
	private boolean hoverWarning;
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
			this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList();
			this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
		} else {
			this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(
				Lists.<WorldTemplate>newArrayList(worldTemplatePaginatedList.templates)
			);
			this.fetchTemplatesAsync(worldTemplatePaginatedList);
		}
	}

	public void setWarning(Component... components) {
		this.warning = components;
		this.displayWarning = true;
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.hoverWarning && this.warningURL != null) {
			Util.getPlatform().openUri("https://www.minecraft.net/realms/adventure-maps-in-1-9");
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
		this.trailerButton = this.addRenderableWidget(
			new Button(this.width / 2 - 206, this.height - 32, 100, 20, new TranslatableComponent("mco.template.button.trailer"), buttonx -> this.onTrailer())
		);
		this.selectButton = this.addRenderableWidget(
			new Button(this.width / 2 - 100, this.height - 32, 100, 20, new TranslatableComponent("mco.template.button.select"), buttonx -> this.selectTemplate())
		);
		Component component = this.worldType == RealmsServer.WorldType.MINIGAME ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_BACK;
		Button button = new Button(this.width / 2 + 6, this.height - 32, 100, 20, component, buttonx -> this.onClose());
		this.addRenderableWidget(button);
		this.publisherButton = this.addRenderableWidget(
			new Button(this.width / 2 + 112, this.height - 32, 100, 20, new TranslatableComponent("mco.template.button.publisher"), buttonx -> this.onPublish())
		);
		this.selectButton.active = false;
		this.trailerButton.visible = false;
		this.publisherButton.visible = false;
		this.addWidget(this.worldTemplateObjectSelectionList);
		this.magicalSpecialHackyFocus(this.worldTemplateObjectSelectionList);
	}

	@Override
	public Component getNarrationMessage() {
		List<Component> list = Lists.<Component>newArrayListWithCapacity(2);
		if (this.title != null) {
			list.add(this.title);
		}

		if (this.warning != null) {
			list.addAll(Arrays.asList(this.warning));
		}

		return CommonComponents.joinLines(list);
	}

	void updateButtonStates() {
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
	public void onClose() {
		this.callback.accept(null);
	}

	void selectTemplate() {
		if (this.hasValidTemplate()) {
			this.callback.accept(this.getSelectedTemplate());
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

	Either<WorldTemplatePaginatedList, String> fetchTemplates(WorldTemplatePaginatedList worldTemplatePaginatedList, RealmsClient realmsClient) {
		try {
			return Either.left(realmsClient.fetchWorldTemplates(worldTemplatePaginatedList.page + 1, worldTemplatePaginatedList.size, this.worldType));
		} catch (RealmsServiceException var4) {
			return Either.right(var4.getMessage());
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.toolTip = null;
		this.currentLink = null;
		this.hoverWarning = false;
		this.renderBackground(poseStack);
		this.worldTemplateObjectSelectionList.render(poseStack, i, j, f);
		if (this.noTemplatesMessage != null) {
			this.renderMultilineMessage(poseStack, i, j, this.noTemplatesMessage);
		}

		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 13, 16777215);
		if (this.displayWarning) {
			Component[] components = this.warning;

			for (int k = 0; k < components.length; k++) {
				int l = this.font.width(components[k]);
				int m = this.width / 2 - l / 2;
				int n = row(-1 + k);
				if (i >= m && i <= m + l && j >= n && j <= n + 9) {
					this.hoverWarning = true;
				}
			}

			for (int kx = 0; kx < components.length; kx++) {
				Component component = components[kx];
				int m = 10526880;
				if (this.warningURL != null) {
					if (this.hoverWarning) {
						m = 7107012;
						component = component.copy().withStyle(ChatFormatting.STRIKETHROUGH);
					} else {
						m = 3368635;
					}
				}

				drawCenteredString(poseStack, this.font, component, this.width / 2, row(-1 + kx), m);
			}
		}

		super.render(poseStack, i, j, f);
		this.renderMousehoverTooltip(poseStack, this.toolTip, i, j);
	}

	private void renderMultilineMessage(PoseStack poseStack, int i, int j, List<TextRenderingUtils.Line> list) {
		for (int k = 0; k < list.size(); k++) {
			TextRenderingUtils.Line line = (TextRenderingUtils.Line)list.get(k);
			int l = row(4 + k);
			int m = line.segments.stream().mapToInt(lineSegmentx -> this.font.width(lineSegmentx.renderedText())).sum();
			int n = this.width / 2 - m / 2;

			for (TextRenderingUtils.LineSegment lineSegment : line.segments) {
				int o = lineSegment.isLink() ? 3368635 : 16777215;
				int p = this.font.drawShadow(poseStack, lineSegment.renderedText(), (float)n, (float)l, o);
				if (lineSegment.isLink() && i > n && i < p && j > l - 3 && j < l + 8) {
					this.toolTip = new TextComponent(lineSegment.getLinkUrl());
					this.currentLink = lineSegment.getLinkUrl();
				}

				n = p;
			}
		}
	}

	protected void renderMousehoverTooltip(PoseStack poseStack, @Nullable Component component, int i, int j) {
		if (component != null) {
			int k = i + 12;
			int l = j - 12;
			int m = this.font.width(component);
			this.fillGradient(poseStack, k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
			this.font.drawShadow(poseStack, component, (float)k, (float)l, 16777215);
		}
	}

	@Environment(EnvType.CLIENT)
	class Entry extends ObjectSelectionList.Entry<RealmsSelectWorldTemplateScreen.Entry> {
		final WorldTemplate template;

		public Entry(WorldTemplate worldTemplate) {
			this.template = worldTemplate;
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.renderWorldTemplateItem(poseStack, this.template, k, j, n, o);
		}

		private void renderWorldTemplateItem(PoseStack poseStack, WorldTemplate worldTemplate, int i, int j, int k, int l) {
			int m = i + 45 + 20;
			RealmsSelectWorldTemplateScreen.this.font.draw(poseStack, worldTemplate.name, (float)m, (float)(j + 2), 16777215);
			RealmsSelectWorldTemplateScreen.this.font.draw(poseStack, worldTemplate.author, (float)m, (float)(j + 15), 7105644);
			RealmsSelectWorldTemplateScreen.this.font
				.draw(poseStack, worldTemplate.version, (float)(m + 227 - RealmsSelectWorldTemplateScreen.this.font.width(worldTemplate.version)), (float)(j + 1), 7105644);
			if (!"".equals(worldTemplate.link) || !"".equals(worldTemplate.trailer) || !"".equals(worldTemplate.recommendedPlayers)) {
				this.drawIcons(poseStack, m - 1, j + 25, k, l, worldTemplate.link, worldTemplate.trailer, worldTemplate.recommendedPlayers);
			}

			this.drawImage(poseStack, i, j + 1, k, l, worldTemplate);
		}

		private void drawImage(PoseStack poseStack, int i, int j, int k, int l, WorldTemplate worldTemplate) {
			RealmsTextureManager.bindWorldTemplate(worldTemplate.id, worldTemplate.image);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			GuiComponent.blit(poseStack, i + 1, j + 1, 0.0F, 0.0F, 38, 38, 38, 38);
			RenderSystem.setShaderTexture(0, RealmsSelectWorldTemplateScreen.SLOT_FRAME_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			GuiComponent.blit(poseStack, i, j, 0.0F, 0.0F, 40, 40, 40, 40);
		}

		private void drawIcons(PoseStack poseStack, int i, int j, int k, int l, String string, String string2, String string3) {
			if (!"".equals(string3)) {
				RealmsSelectWorldTemplateScreen.this.font.draw(poseStack, string3, (float)i, (float)(j + 4), 5000268);
			}

			int m = "".equals(string3) ? 0 : RealmsSelectWorldTemplateScreen.this.font.width(string3) + 2;
			boolean bl = false;
			boolean bl2 = false;
			boolean bl3 = "".equals(string);
			if (k >= i + m && k <= i + m + 32 && l >= j && l <= j + 15 && l < RealmsSelectWorldTemplateScreen.this.height - 15 && l > 32) {
				if (k <= i + 15 + m && k > m) {
					if (bl3) {
						bl2 = true;
					} else {
						bl = true;
					}
				} else if (!bl3) {
					bl2 = true;
				}
			}

			if (!bl3) {
				RenderSystem.setShaderTexture(0, RealmsSelectWorldTemplateScreen.LINK_ICON);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				float f = bl ? 15.0F : 0.0F;
				GuiComponent.blit(poseStack, i + m, j, f, 0.0F, 15, 15, 30, 15);
			}

			if (!"".equals(string2)) {
				RenderSystem.setShaderTexture(0, RealmsSelectWorldTemplateScreen.TRAILER_ICON);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				int n = i + m + (bl3 ? 0 : 17);
				float g = bl2 ? 15.0F : 0.0F;
				GuiComponent.blit(poseStack, n, j, g, 0.0F, 15, 15, 30, 15);
			}

			if (bl) {
				RealmsSelectWorldTemplateScreen.this.toolTip = RealmsSelectWorldTemplateScreen.PUBLISHER_LINK_TOOLTIP;
				RealmsSelectWorldTemplateScreen.this.currentLink = string;
			} else if (bl2 && !"".equals(string2)) {
				RealmsSelectWorldTemplateScreen.this.toolTip = RealmsSelectWorldTemplateScreen.TRAILER_LINK_TOOLTIP;
				RealmsSelectWorldTemplateScreen.this.currentLink = string2;
			}
		}

		@Override
		public Component getNarration() {
			Component component = CommonComponents.joinLines(
				new TextComponent(this.template.name),
				new TranslatableComponent("mco.template.select.narrate.authors", this.template.author),
				new TextComponent(this.template.recommendedPlayers),
				new TranslatableComponent("mco.template.select.narrate.version", this.template.version)
			);
			return new TranslatableComponent("narrator.select", component);
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

					RealmsSelectWorldTemplateScreen.this.clicks += 7;
					if (RealmsSelectWorldTemplateScreen.this.clicks >= 10) {
						RealmsSelectWorldTemplateScreen.this.selectTemplate();
					}

					return true;
				}
			}

			return super.mouseClicked(d, e, i);
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
		public void renderBackground(PoseStack poseStack) {
			RealmsSelectWorldTemplateScreen.this.renderBackground(poseStack);
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
