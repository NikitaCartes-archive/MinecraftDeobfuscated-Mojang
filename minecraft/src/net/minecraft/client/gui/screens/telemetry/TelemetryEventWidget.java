package net.minecraft.client.gui.screens.telemetry;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleConsumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(EnvType.CLIENT)
public class TelemetryEventWidget extends AbstractScrollWidget {
	private static final int HEADER_HORIZONTAL_PADDING = 32;
	private static final String TELEMETRY_REQUIRED_TRANSLATION_KEY = "telemetry.event.required";
	private static final String TELEMETRY_OPTIONAL_TRANSLATION_KEY = "telemetry.event.optional";
	private static final Component PROPERTY_TITLE = Component.translatable("telemetry_info.property_title").withStyle(ChatFormatting.UNDERLINE);
	private final Font font;
	private TelemetryEventWidget.Content content;
	@Nullable
	private DoubleConsumer onScrolledListener;

	public TelemetryEventWidget(int i, int j, int k, int l, Font font) {
		super(i, j, k, l, Component.empty());
		this.font = font;
		this.content = this.buildContent(Minecraft.getInstance().telemetryOptInExtra());
	}

	public void onOptInChanged(boolean bl) {
		this.content = this.buildContent(bl);
		this.setScrollAmount(this.scrollAmount());
	}

	private TelemetryEventWidget.Content buildContent(boolean bl) {
		TelemetryEventWidget.ContentBuilder contentBuilder = new TelemetryEventWidget.ContentBuilder(this.containerWidth());
		List<TelemetryEventType> list = new ArrayList(TelemetryEventType.values());
		list.sort(Comparator.comparing(TelemetryEventType::isOptIn));
		if (!bl) {
			list.removeIf(TelemetryEventType::isOptIn);
		}

		for (int i = 0; i < list.size(); i++) {
			TelemetryEventType telemetryEventType = (TelemetryEventType)list.get(i);
			this.addEventType(contentBuilder, telemetryEventType);
			if (i < list.size() - 1) {
				contentBuilder.addSpacer(9);
			}
		}

		return contentBuilder.build();
	}

	public void setOnScrolledListener(@Nullable DoubleConsumer doubleConsumer) {
		this.onScrolledListener = doubleConsumer;
	}

	@Override
	protected void setScrollAmount(double d) {
		super.setScrollAmount(d);
		if (this.onScrolledListener != null) {
			this.onScrolledListener.accept(this.scrollAmount());
		}
	}

	@Override
	protected int getInnerHeight() {
		return this.content.container().getHeight();
	}

	@Override
	protected boolean scrollbarVisible() {
		return this.getInnerHeight() > this.height;
	}

	@Override
	protected double scrollRate() {
		return 9.0;
	}

	@Override
	protected void renderContents(PoseStack poseStack, int i, int j, float f) {
		int k = this.getY() + this.innerPadding();
		int l = this.getX() + this.innerPadding();
		poseStack.pushPose();
		poseStack.translate((double)l, (double)k, 0.0);
		this.content.container().visitWidgets(abstractWidget -> abstractWidget.render(poseStack, i, j, f));
		poseStack.popPose();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.content.narration());
	}

	private void addEventType(TelemetryEventWidget.ContentBuilder contentBuilder, TelemetryEventType telemetryEventType) {
		String string = telemetryEventType.isOptIn() ? "telemetry.event.optional" : "telemetry.event.required";
		contentBuilder.addHeader(this.font, Component.translatable(string, telemetryEventType.title()));
		contentBuilder.addHeader(this.font, telemetryEventType.description().withStyle(ChatFormatting.GRAY));
		contentBuilder.addSpacer(9 / 2);
		contentBuilder.addLine(this.font, PROPERTY_TITLE, 2);
		this.addEventTypeProperties(telemetryEventType, contentBuilder);
	}

	private void addEventTypeProperties(TelemetryEventType telemetryEventType, TelemetryEventWidget.ContentBuilder contentBuilder) {
		for (TelemetryProperty<?> telemetryProperty : telemetryEventType.properties()) {
			contentBuilder.addLine(this.font, telemetryProperty.title());
		}
	}

	private int containerWidth() {
		return this.width - this.totalInnerPadding();
	}

	@Environment(EnvType.CLIENT)
	static record Content(GridLayout container, Component narration) {
	}

	@Environment(EnvType.CLIENT)
	static class ContentBuilder {
		private final int width;
		private final GridLayout grid;
		private final GridLayout.RowHelper helper;
		private final LayoutSettings alignHeader;
		private final MutableComponent narration = Component.empty();

		public ContentBuilder(int i) {
			this.width = i;
			this.grid = new GridLayout();
			this.grid.defaultCellSetting().alignHorizontallyLeft();
			this.helper = this.grid.createRowHelper(1);
			this.helper.addChild(SpacerElement.width(i));
			this.alignHeader = this.helper.newCellSettings().alignHorizontallyCenter().paddingHorizontal(32);
		}

		public void addLine(Font font, Component component) {
			this.addLine(font, component, 0);
		}

		public void addLine(Font font, Component component, int i) {
			this.helper.addChild(MultiLineTextWidget.create(this.width, font, component), this.helper.newCellSettings().paddingBottom(i));
			this.narration.append(component).append("\n");
		}

		public void addHeader(Font font, Component component) {
			this.helper.addChild(MultiLineTextWidget.createCentered(this.width - 64, font, component), this.alignHeader);
			this.narration.append(component).append("\n");
		}

		public void addSpacer(int i) {
			this.helper.addChild(SpacerElement.height(i));
		}

		public TelemetryEventWidget.Content build() {
			this.grid.arrangeElements();
			return new TelemetryEventWidget.Content(this.grid, this.narration);
		}
	}
}
