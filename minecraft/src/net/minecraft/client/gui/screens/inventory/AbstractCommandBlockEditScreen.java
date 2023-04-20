package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.BaseCommandBlock;

@Environment(EnvType.CLIENT)
public abstract class AbstractCommandBlockEditScreen extends Screen {
	private static final Component SET_COMMAND_LABEL = Component.translatable("advMode.setCommand");
	private static final Component COMMAND_LABEL = Component.translatable("advMode.command");
	private static final Component PREVIOUS_OUTPUT_LABEL = Component.translatable("advMode.previousOutput");
	protected EditBox commandEdit;
	protected EditBox previousEdit;
	protected Button doneButton;
	protected Button cancelButton;
	protected CycleButton<Boolean> outputButton;
	CommandSuggestions commandSuggestions;

	public AbstractCommandBlockEditScreen() {
		super(GameNarrator.NO_TITLE);
	}

	@Override
	public void tick() {
		this.commandEdit.tick();
	}

	abstract BaseCommandBlock getCommandBlock();

	abstract int getPreviousY();

	@Override
	protected void init() {
		this.doneButton = this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20).build()
		);
		this.cancelButton = this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).bounds(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20).build()
		);
		boolean bl = this.getCommandBlock().isTrackOutput();
		this.outputButton = this.addRenderableWidget(
			CycleButton.booleanBuilder(Component.literal("O"), Component.literal("X"))
				.withInitialValue(bl)
				.displayOnlyValue()
				.create(this.width / 2 + 150 - 20, this.getPreviousY(), 20, 20, Component.translatable("advMode.trackOutput"), (cycleButton, boolean_) -> {
					BaseCommandBlock baseCommandBlock = this.getCommandBlock();
					baseCommandBlock.setTrackOutput(boolean_);
					this.updatePreviousOutput(boolean_);
				})
		);
		this.commandEdit = new EditBox(this.font, this.width / 2 - 150, 50, 300, 20, Component.translatable("advMode.command")) {
			@Override
			protected MutableComponent createNarrationMessage() {
				return super.createNarrationMessage().append(AbstractCommandBlockEditScreen.this.commandSuggestions.getNarrationMessage());
			}
		};
		this.commandEdit.setMaxLength(32500);
		this.commandEdit.setResponder(this::onEdited);
		this.addWidget(this.commandEdit);
		this.previousEdit = new EditBox(this.font, this.width / 2 - 150, this.getPreviousY(), 276, 20, Component.translatable("advMode.previousOutput"));
		this.previousEdit.setMaxLength(32500);
		this.previousEdit.setEditable(false);
		this.previousEdit.setValue("-");
		this.addWidget(this.previousEdit);
		this.setInitialFocus(this.commandEdit);
		this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.commandEdit, this.font, true, true, 0, 7, false, Integer.MIN_VALUE);
		this.commandSuggestions.setAllowSuggestions(true);
		this.commandSuggestions.updateCommandInfo();
		this.updatePreviousOutput(bl);
	}

	@Override
	public void resize(Minecraft minecraft, int i, int j) {
		String string = this.commandEdit.getValue();
		this.init(minecraft, i, j);
		this.commandEdit.setValue(string);
		this.commandSuggestions.updateCommandInfo();
	}

	protected void updatePreviousOutput(boolean bl) {
		this.previousEdit.setValue(bl ? this.getCommandBlock().getLastOutput().getString() : "-");
	}

	protected void onDone() {
		BaseCommandBlock baseCommandBlock = this.getCommandBlock();
		this.populateAndSendPacket(baseCommandBlock);
		if (!baseCommandBlock.isTrackOutput()) {
			baseCommandBlock.setLastOutput(null);
		}

		this.minecraft.setScreen(null);
	}

	protected abstract void populateAndSendPacket(BaseCommandBlock baseCommandBlock);

	private void onEdited(String string) {
		this.commandSuggestions.updateCommandInfo();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (this.commandSuggestions.keyPressed(i, j, k)) {
			return true;
		} else if (super.keyPressed(i, j, k)) {
			return true;
		} else if (i != 257 && i != 335) {
			return false;
		} else {
			this.onDone();
			return true;
		}
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		return this.commandSuggestions.mouseScrolled(f) ? true : super.mouseScrolled(d, e, f);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		return this.commandSuggestions.mouseClicked(d, e, i) ? true : super.mouseClicked(d, e, i);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderBackground(guiGraphics);
		guiGraphics.drawCenteredString(this.font, SET_COMMAND_LABEL, this.width / 2, 20, 16777215);
		guiGraphics.drawString(this.font, COMMAND_LABEL, this.width / 2 - 150, 40, 10526880);
		this.commandEdit.render(guiGraphics, i, j, f);
		int k = 75;
		if (!this.previousEdit.getValue().isEmpty()) {
			k += 5 * 9 + 1 + this.getPreviousY() - 135;
			guiGraphics.drawString(this.font, PREVIOUS_OUTPUT_LABEL, this.width / 2 - 150, k + 4, 10526880);
			this.previousEdit.render(guiGraphics, i, j, f);
		}

		super.render(guiGraphics, i, j, f);
		this.commandSuggestions.render(guiGraphics, i, j);
	}
}
