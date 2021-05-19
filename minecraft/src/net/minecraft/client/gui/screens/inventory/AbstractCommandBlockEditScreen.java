package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BaseCommandBlock;

@Environment(EnvType.CLIENT)
public abstract class AbstractCommandBlockEditScreen extends Screen {
	private static final Component SET_COMMAND_LABEL = new TranslatableComponent("advMode.setCommand");
	private static final Component COMMAND_LABEL = new TranslatableComponent("advMode.command");
	private static final Component PREVIOUS_OUTPUT_LABEL = new TranslatableComponent("advMode.previousOutput");
	protected EditBox commandEdit;
	protected EditBox previousEdit;
	protected Button doneButton;
	protected Button cancelButton;
	protected CycleButton<Boolean> outputButton;
	CommandSuggestions commandSuggestions;

	public AbstractCommandBlockEditScreen() {
		super(NarratorChatListener.NO_TITLE);
	}

	@Override
	public void tick() {
		this.commandEdit.tick();
	}

	abstract BaseCommandBlock getCommandBlock();

	abstract int getPreviousY();

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.doneButton = this.addRenderableWidget(
			new Button(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, CommonComponents.GUI_DONE, button -> this.onDone())
		);
		this.cancelButton = this.addRenderableWidget(
			new Button(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, CommonComponents.GUI_CANCEL, button -> this.onClose())
		);
		boolean bl = this.getCommandBlock().isTrackOutput();
		this.outputButton = this.addRenderableWidget(
			CycleButton.booleanBuilder(new TextComponent("O"), new TextComponent("X"))
				.withInitialValue(bl)
				.displayOnlyValue()
				.create(this.width / 2 + 150 - 20, this.getPreviousY(), 20, 20, new TranslatableComponent("advMode.trackOutput"), (cycleButton, boolean_) -> {
					BaseCommandBlock baseCommandBlock = this.getCommandBlock();
					baseCommandBlock.setTrackOutput(boolean_);
					this.updatePreviousOutput(boolean_);
				})
		);
		this.commandEdit = new EditBox(this.font, this.width / 2 - 150, 50, 300, 20, new TranslatableComponent("advMode.command")) {
			@Override
			protected MutableComponent createNarrationMessage() {
				return super.createNarrationMessage().append(AbstractCommandBlockEditScreen.this.commandSuggestions.getNarrationMessage());
			}
		};
		this.commandEdit.setMaxLength(32500);
		this.commandEdit.setResponder(this::onEdited);
		this.addWidget(this.commandEdit);
		this.previousEdit = new EditBox(this.font, this.width / 2 - 150, this.getPreviousY(), 276, 20, new TranslatableComponent("advMode.previousOutput"));
		this.previousEdit.setMaxLength(32500);
		this.previousEdit.setEditable(false);
		this.previousEdit.setValue("-");
		this.addWidget(this.previousEdit);
		this.setInitialFocus(this.commandEdit);
		this.commandEdit.setFocus(true);
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

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, SET_COMMAND_LABEL, this.width / 2, 20, 16777215);
		drawString(poseStack, this.font, COMMAND_LABEL, this.width / 2 - 150, 40, 10526880);
		this.commandEdit.render(poseStack, i, j, f);
		int k = 75;
		if (!this.previousEdit.getValue().isEmpty()) {
			k += 5 * 9 + 1 + this.getPreviousY() - 135;
			drawString(poseStack, this.font, PREVIOUS_OUTPUT_LABEL, this.width / 2 - 150, k + 4, 10526880);
			this.previousEdit.render(poseStack, i, j, f);
		}

		super.render(poseStack, i, j, f);
		this.commandSuggestions.render(poseStack, i, j);
	}
}
