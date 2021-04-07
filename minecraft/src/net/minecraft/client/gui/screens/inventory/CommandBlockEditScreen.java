package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

@Environment(EnvType.CLIENT)
public class CommandBlockEditScreen extends AbstractCommandBlockEditScreen {
	private final CommandBlockEntity autoCommandBlock;
	private CycleButton<CommandBlockEntity.Mode> modeButton;
	private CycleButton<Boolean> conditionalButton;
	private CycleButton<Boolean> autoexecButton;
	private CommandBlockEntity.Mode mode = CommandBlockEntity.Mode.REDSTONE;
	private boolean conditional;
	private boolean autoexec;

	public CommandBlockEditScreen(CommandBlockEntity commandBlockEntity) {
		this.autoCommandBlock = commandBlockEntity;
	}

	@Override
	BaseCommandBlock getCommandBlock() {
		return this.autoCommandBlock.getCommandBlock();
	}

	@Override
	int getPreviousY() {
		return 135;
	}

	@Override
	protected void init() {
		super.init();
		this.modeButton = this.addButton(
			CycleButton.<CommandBlockEntity.Mode>builder(mode -> {
					switch (mode) {
						case SEQUENCE:
							return new TranslatableComponent("advMode.mode.sequence");
						case AUTO:
							return new TranslatableComponent("advMode.mode.auto");
						case REDSTONE:
						default:
							return new TranslatableComponent("advMode.mode.redstone");
					}
				})
				.withValues(CommandBlockEntity.Mode.values())
				.displayOnlyValue()
				.withInitialValue(this.mode)
				.create(this.width / 2 - 50 - 100 - 4, 165, 100, 20, new TranslatableComponent("advMode.mode"), (cycleButton, mode) -> this.mode = mode)
		);
		this.conditionalButton = this.addButton(
			CycleButton.booleanBuilder(new TranslatableComponent("advMode.mode.conditional"), new TranslatableComponent("advMode.mode.unconditional"))
				.displayOnlyValue()
				.withInitialValue(this.conditional)
				.create(this.width / 2 - 50, 165, 100, 20, new TranslatableComponent("advMode.type"), (cycleButton, boolean_) -> this.conditional = boolean_)
		);
		this.autoexecButton = this.addButton(
			CycleButton.booleanBuilder(new TranslatableComponent("advMode.mode.autoexec.bat"), new TranslatableComponent("advMode.mode.redstoneTriggered"))
				.displayOnlyValue()
				.withInitialValue(this.autoexec)
				.create(this.width / 2 + 50 + 4, 165, 100, 20, new TranslatableComponent("advMode.triggering"), (cycleButton, boolean_) -> this.autoexec = boolean_)
		);
		this.enableControls(false);
	}

	private void enableControls(boolean bl) {
		this.doneButton.active = bl;
		this.outputButton.active = bl;
		this.modeButton.active = bl;
		this.conditionalButton.active = bl;
		this.autoexecButton.active = bl;
	}

	public void updateGui() {
		BaseCommandBlock baseCommandBlock = this.autoCommandBlock.getCommandBlock();
		this.commandEdit.setValue(baseCommandBlock.getCommand());
		boolean bl = baseCommandBlock.isTrackOutput();
		this.mode = this.autoCommandBlock.getMode();
		this.conditional = this.autoCommandBlock.isConditional();
		this.autoexec = this.autoCommandBlock.isAutomatic();
		this.outputButton.setValue(bl);
		this.modeButton.setValue(this.mode);
		this.conditionalButton.setValue(this.conditional);
		this.autoexecButton.setValue(this.autoexec);
		this.updatePreviousOutput(bl);
		this.enableControls(true);
	}

	@Override
	public void resize(Minecraft minecraft, int i, int j) {
		super.resize(minecraft, i, j);
		this.enableControls(true);
	}

	@Override
	protected void populateAndSendPacket(BaseCommandBlock baseCommandBlock) {
		this.minecraft
			.getConnection()
			.send(
				new ServerboundSetCommandBlockPacket(
					new BlockPos(baseCommandBlock.getPosition()), this.commandEdit.getValue(), this.mode, baseCommandBlock.isTrackOutput(), this.conditional, this.autoexec
				)
			);
	}
}
