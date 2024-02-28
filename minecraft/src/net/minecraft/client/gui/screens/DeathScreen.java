package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class DeathScreen extends Screen {
	private static final ResourceLocation DRAFT_REPORT_SPRITE = new ResourceLocation("icon/draft_report");
	private int delayTicker;
	private final Component causeOfDeath;
	private final boolean hardcore;
	private Component deathScore;
	private final List<Button> exitButtons = Lists.<Button>newArrayList();
	@Nullable
	private Button exitToTitleButton;

	public DeathScreen(@Nullable Component component, boolean bl) {
		super(Component.translatable(bl ? "deathScreen.title.hardcore" : "deathScreen.title"));
		this.causeOfDeath = component;
		this.hardcore = bl;
	}

	@Override
	protected void init() {
		this.delayTicker = 0;
		this.exitButtons.clear();
		Component component = this.hardcore ? Component.translatable("deathScreen.spectate") : Component.translatable("deathScreen.respawn");
		this.exitButtons.add(this.addRenderableWidget(Button.builder(component, button -> {
			this.minecraft.player.respawn();
			button.active = false;
		}).bounds(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()));
		this.exitToTitleButton = this.addRenderableWidget(
			Button.builder(
					Component.translatable("deathScreen.titleScreen"),
					button -> this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, this::handleExitToTitleScreen, true)
				)
				.bounds(this.width / 2 - 100, this.height / 4 + 96, 200, 20)
				.build()
		);
		this.exitButtons.add(this.exitToTitleButton);
		this.setButtonsActive(false);
		this.deathScore = Component.translatable(
			"deathScreen.score.value", Component.literal(Integer.toString(this.minecraft.player.getScore())).withStyle(ChatFormatting.YELLOW)
		);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	private void handleExitToTitleScreen() {
		if (this.hardcore) {
			this.exitToTitleScreen();
		} else {
			ConfirmScreen confirmScreen = new DeathScreen.TitleConfirmScreen(
				bl -> {
					if (bl) {
						this.exitToTitleScreen();
					} else {
						this.minecraft.player.respawn();
						this.minecraft.setScreen(null);
					}
				},
				Component.translatable("deathScreen.quit.confirm"),
				CommonComponents.EMPTY,
				Component.translatable("deathScreen.titleScreen"),
				Component.translatable("deathScreen.respawn")
			);
			this.minecraft.setScreen(confirmScreen);
			confirmScreen.setDelay(20);
		}
	}

	private void exitToTitleScreen() {
		if (this.minecraft.level != null) {
			this.minecraft.level.disconnect();
		}

		this.minecraft.disconnect(new GenericMessageScreen(Component.translatable("menu.savingLevel")));
		this.minecraft.setScreen(new TitleScreen());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.pose().pushPose();
		guiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2 / 2, 30, 16777215);
		guiGraphics.pose().popPose();
		if (this.causeOfDeath != null) {
			guiGraphics.drawCenteredString(this.font, this.causeOfDeath, this.width / 2, 85, 16777215);
		}

		guiGraphics.drawCenteredString(this.font, this.deathScore, this.width / 2, 100, 16777215);
		if (this.causeOfDeath != null && j > 85 && j < 85 + 9) {
			Style style = this.getClickedComponentStyleAt(i);
			guiGraphics.renderComponentHoverEffect(this.font, style, i, j);
		}

		if (this.exitToTitleButton != null && this.minecraft.getReportingContext().hasDraftReport()) {
			guiGraphics.blitSprite(
				DRAFT_REPORT_SPRITE, this.exitToTitleButton.getX() + this.exitToTitleButton.getWidth() - 17, this.exitToTitleButton.getY() + 3, 15, 15
			);
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		guiGraphics.fillGradient(0, 0, this.width, this.height, 1615855616, -1602211792);
	}

	@Nullable
	private Style getClickedComponentStyleAt(int i) {
		if (this.causeOfDeath == null) {
			return null;
		} else {
			int j = this.minecraft.font.width(this.causeOfDeath);
			int k = this.width / 2 - j / 2;
			int l = this.width / 2 + j / 2;
			return i >= k && i <= l ? this.minecraft.font.getSplitter().componentStyleAtWidth(this.causeOfDeath, i - k) : null;
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.causeOfDeath != null && e > 85.0 && e < (double)(85 + 9)) {
			Style style = this.getClickedComponentStyleAt((int)d);
			if (style != null && style.getClickEvent() != null && style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
				this.handleComponentClicked(style);
				return false;
			}
		}

		return super.mouseClicked(d, e, i);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		this.delayTicker++;
		if (this.delayTicker == 20) {
			this.setButtonsActive(true);
		}
	}

	private void setButtonsActive(boolean bl) {
		for (Button button : this.exitButtons) {
			button.active = bl;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class TitleConfirmScreen extends ConfirmScreen {
		public TitleConfirmScreen(BooleanConsumer booleanConsumer, Component component, Component component2, Component component3, Component component4) {
			super(booleanConsumer, component, component2, component3, component4);
		}
	}
}
