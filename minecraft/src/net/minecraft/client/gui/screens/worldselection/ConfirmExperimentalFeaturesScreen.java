package net.minecraft.client.gui.screens.worldselection;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Collection;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.flag.FeatureFlags;

@Environment(EnvType.CLIENT)
public class ConfirmExperimentalFeaturesScreen extends Screen {
	private static final Component TITLE = Component.translatable("selectWorld.experimental.title");
	private static final Component MESSAGE = Component.translatable("selectWorld.experimental.message");
	private static final Component DETAILS_BUTTON = Component.translatable("selectWorld.experimental.details");
	private static final int COLUMN_SPACING = 10;
	private static final int DETAILS_BUTTON_WIDTH = 100;
	private final BooleanConsumer callback;
	final Collection<Pack> enabledPacks;
	private final GridLayout layout = new GridLayout().columnSpacing(10).rowSpacing(20);

	public ConfirmExperimentalFeaturesScreen(Collection<Pack> collection, BooleanConsumer booleanConsumer) {
		super(TITLE);
		this.enabledPacks = collection;
		this.callback = booleanConsumer;
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(super.getNarrationMessage(), MESSAGE);
	}

	@Override
	protected void init() {
		super.init();
		GridLayout.RowHelper rowHelper = this.layout.createRowHelper(2);
		LayoutSettings layoutSettings = rowHelper.newCellSettings().alignHorizontallyCenter();
		rowHelper.addChild(new StringWidget(this.title, this.font), 2, layoutSettings);
		MultiLineTextWidget multiLineTextWidget = rowHelper.addChild(new MultiLineTextWidget(MESSAGE, this.font).setCentered(true), 2, layoutSettings);
		multiLineTextWidget.setMaxWidth(310);
		rowHelper.addChild(
			Button.builder(DETAILS_BUTTON, button -> this.minecraft.setScreen(new ConfirmExperimentalFeaturesScreen.DetailsScreen())).width(100).build(),
			2,
			layoutSettings
		);
		rowHelper.addChild(Button.builder(CommonComponents.GUI_PROCEED, button -> this.callback.accept(true)).build());
		rowHelper.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.callback.accept(false)).build());
		this.layout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
		this.layout.arrangeElements();
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		FrameLayout.alignInRectangle(this.layout, 0, 0, this.width, this.height, 0.5F, 0.5F);
	}

	@Override
	public void onClose() {
		this.callback.accept(false);
	}

	@Environment(EnvType.CLIENT)
	class DetailsScreen extends Screen {
		private static final Component TITLE = Component.translatable("selectWorld.experimental.details.title");
		final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
		@Nullable
		private ConfirmExperimentalFeaturesScreen.DetailsScreen.PackList list;

		DetailsScreen() {
			super(TITLE);
		}

		@Override
		protected void init() {
			this.layout.addTitleHeader(TITLE, this.font);
			this.list = this.layout
				.addToContents(new ConfirmExperimentalFeaturesScreen.DetailsScreen.PackList(this.minecraft, ConfirmExperimentalFeaturesScreen.this.enabledPacks));
			this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).build());
			this.layout.visitWidgets(guiEventListener -> {
				AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
			});
			this.repositionElements();
		}

		@Override
		protected void repositionElements() {
			if (this.list != null) {
				this.list.updateSize(this.width, this.layout);
			}

			this.layout.arrangeElements();
		}

		@Override
		public void onClose() {
			this.minecraft.setScreen(ConfirmExperimentalFeaturesScreen.this);
		}

		@Environment(EnvType.CLIENT)
		class PackList extends ObjectSelectionList<ConfirmExperimentalFeaturesScreen.DetailsScreen.PackListEntry> {
			public PackList(final Minecraft minecraft, final Collection<Pack> collection) {
				super(minecraft, DetailsScreen.this.width, DetailsScreen.this.layout.getContentHeight(), DetailsScreen.this.layout.getHeaderHeight(), (9 + 2) * 3);

				for (Pack pack : collection) {
					String string = FeatureFlags.printMissingFlags(FeatureFlags.VANILLA_SET, pack.getRequestedFeatures());
					if (!string.isEmpty()) {
						Component component = ComponentUtils.mergeStyles(pack.getTitle().copy(), Style.EMPTY.withBold(true));
						Component component2 = Component.translatable("selectWorld.experimental.details.entry", string);
						this.addEntry(DetailsScreen.this.new PackListEntry(component, component2, MultiLineLabel.create(DetailsScreen.this.font, component2, this.getRowWidth())));
					}
				}
			}

			@Override
			public int getRowWidth() {
				return this.width * 3 / 4;
			}
		}

		@Environment(EnvType.CLIENT)
		class PackListEntry extends ObjectSelectionList.Entry<ConfirmExperimentalFeaturesScreen.DetailsScreen.PackListEntry> {
			private final Component packId;
			private final Component message;
			private final MultiLineLabel splitMessage;

			PackListEntry(final Component component, final Component component2, final MultiLineLabel multiLineLabel) {
				this.packId = component;
				this.message = component2;
				this.splitMessage = multiLineLabel;
			}

			@Override
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				guiGraphics.drawString(DetailsScreen.this.minecraft.font, this.packId, k, j, -1);
				this.splitMessage.renderLeftAligned(guiGraphics, k, j + 12, 9, -1);
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.packId, this.message));
			}
		}
	}
}
