package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class DeathScreen extends Screen {
	private int delayTicker;
	private final Component causeOfDeath;
	private final boolean hardcore;

	public DeathScreen(@Nullable Component component, boolean bl) {
		super(new TranslatableComponent(bl ? "deathScreen.title.hardcore" : "deathScreen.title"));
		this.causeOfDeath = component;
		this.hardcore = bl;
	}

	@Override
	protected void init() {
		this.delayTicker = 0;
		this.addButton(
			new Button(
				this.width / 2 - 100,
				this.height / 4 + 72,
				200,
				20,
				this.hardcore ? new TranslatableComponent("deathScreen.spectate") : new TranslatableComponent("deathScreen.respawn"),
				buttonx -> {
					this.minecraft.player.respawn();
					this.minecraft.setScreen(null);
				}
			)
		);
		Button button = this.addButton(
			new Button(
				this.width / 2 - 100,
				this.height / 4 + 96,
				200,
				20,
				new TranslatableComponent("deathScreen.titleScreen"),
				buttonx -> {
					if (this.hardcore) {
						this.exitToTitleScreen();
					} else {
						ConfirmScreen confirmScreen = new ConfirmScreen(
							this::confirmResult,
							new TranslatableComponent("deathScreen.quit.confirm"),
							TextComponent.EMPTY,
							new TranslatableComponent("deathScreen.titleScreen"),
							new TranslatableComponent("deathScreen.respawn")
						);
						this.minecraft.setScreen(confirmScreen);
						confirmScreen.setDelay(20);
					}
				}
			)
		);
		if (!this.hardcore && this.minecraft.getUser() == null) {
			button.active = false;
		}

		for (AbstractWidget abstractWidget : this.buttons) {
			abstractWidget.active = false;
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	private void confirmResult(boolean bl) {
		if (bl) {
			this.exitToTitleScreen();
		} else {
			this.minecraft.player.respawn();
			this.minecraft.setScreen(null);
		}
	}

	private void exitToTitleScreen() {
		if (this.minecraft.level != null) {
			this.minecraft.level.disconnect();
		}

		this.minecraft.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
		this.minecraft.setScreen(new TitleScreen());
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.fillGradient(poseStack, 0, 0, this.width, this.height, 1615855616, -1602211792);
		RenderSystem.pushMatrix();
		RenderSystem.scalef(2.0F, 2.0F, 2.0F);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2 / 2, 30, 16777215);
		RenderSystem.popMatrix();
		if (this.causeOfDeath != null) {
			this.drawCenteredString(poseStack, this.font, this.causeOfDeath, this.width / 2, 85, 16777215);
		}

		this.drawCenteredString(
			poseStack, this.font, I18n.get("deathScreen.score") + ": " + ChatFormatting.YELLOW + this.minecraft.player.getScore(), this.width / 2, 100, 16777215
		);
		if (this.causeOfDeath != null && j > 85 && j < 85 + 9) {
			Component component = this.getClickedComponentAt(i);
			this.renderComponentHoverEffect(poseStack, component, i, j);
		}

		super.render(poseStack, i, j, f);
	}

	@Nullable
	public Component getClickedComponentAt(int i) {
		if (this.causeOfDeath == null) {
			return null;
		} else {
			int j = this.minecraft.font.width(this.causeOfDeath);
			int k = this.width / 2 - j / 2;
			int l = this.width / 2 + j / 2;
			return i >= k && i <= l ? this.minecraft.font.getSplitter().componentAtWidth(this.causeOfDeath, i - k) : null;
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.causeOfDeath != null && e > 85.0 && e < (double)(85 + 9)) {
			Component component = this.getClickedComponentAt((int)d);
			if (component != null && component.getStyle().getClickEvent() != null && component.getStyle().getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
				this.handleComponentClicked(component);
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
			for (AbstractWidget abstractWidget : this.buttons) {
				abstractWidget.active = true;
			}
		}
	}
}
