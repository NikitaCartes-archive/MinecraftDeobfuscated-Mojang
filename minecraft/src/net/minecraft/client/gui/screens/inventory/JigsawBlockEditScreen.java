package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;

@Environment(EnvType.CLIENT)
public class JigsawBlockEditScreen extends Screen {
	private final JigsawBlockEntity jigsawEntity;
	private EditBox attachementTypeEdit;
	private EditBox targetPoolEdit;
	private EditBox finalStateEdit;
	private Button doneButton;

	public JigsawBlockEditScreen(JigsawBlockEntity jigsawBlockEntity) {
		super(NarratorChatListener.NO_TITLE);
		this.jigsawEntity = jigsawBlockEntity;
	}

	@Override
	public void tick() {
		this.attachementTypeEdit.tick();
		this.targetPoolEdit.tick();
		this.finalStateEdit.tick();
	}

	private void onDone() {
		this.sendToServer();
		this.minecraft.setScreen(null);
	}

	private void onCancel() {
		this.minecraft.setScreen(null);
	}

	private void sendToServer() {
		this.minecraft
			.getConnection()
			.send(
				new ServerboundSetJigsawBlockPacket(
					this.jigsawEntity.getBlockPos(),
					new ResourceLocation(this.attachementTypeEdit.getValue()),
					new ResourceLocation(this.targetPoolEdit.getValue()),
					this.finalStateEdit.getValue()
				)
			);
	}

	@Override
	public void onClose() {
		this.onCancel();
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.doneButton = this.addButton(new Button(this.width / 2 - 4 - 150, 210, 150, 20, I18n.get("gui.done"), button -> this.onDone()));
		this.addButton(new Button(this.width / 2 + 4, 210, 150, 20, I18n.get("gui.cancel"), button -> this.onCancel()));
		this.targetPoolEdit = new EditBox(this.font, this.width / 2 - 152, 40, 300, 20, I18n.get("jigsaw_block.target_pool"));
		this.targetPoolEdit.setMaxLength(128);
		this.targetPoolEdit.setValue(this.jigsawEntity.getTargetPool().toString());
		this.targetPoolEdit.setResponder(string -> this.updateValidity());
		this.children.add(this.targetPoolEdit);
		this.attachementTypeEdit = new EditBox(this.font, this.width / 2 - 152, 80, 300, 20, I18n.get("jigsaw_block.attachement_type"));
		this.attachementTypeEdit.setMaxLength(128);
		this.attachementTypeEdit.setValue(this.jigsawEntity.getAttachementType().toString());
		this.attachementTypeEdit.setResponder(string -> this.updateValidity());
		this.children.add(this.attachementTypeEdit);
		this.finalStateEdit = new EditBox(this.font, this.width / 2 - 152, 120, 300, 20, I18n.get("jigsaw_block.final_state"));
		this.finalStateEdit.setMaxLength(256);
		this.finalStateEdit.setValue(this.jigsawEntity.getFinalState());
		this.children.add(this.finalStateEdit);
		this.setInitialFocus(this.targetPoolEdit);
		this.updateValidity();
	}

	protected void updateValidity() {
		this.doneButton.active = ResourceLocation.isValidResourceLocation(this.attachementTypeEdit.getValue())
			& ResourceLocation.isValidResourceLocation(this.targetPoolEdit.getValue());
	}

	@Override
	public void resize(Minecraft minecraft, int i, int j) {
		String string = this.attachementTypeEdit.getValue();
		String string2 = this.targetPoolEdit.getValue();
		String string3 = this.finalStateEdit.getValue();
		this.init(minecraft, i, j);
		this.attachementTypeEdit.setValue(string);
		this.targetPoolEdit.setValue(string2);
		this.finalStateEdit.setValue(string3);
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (super.keyPressed(i, j, k)) {
			return true;
		} else if (!this.doneButton.active || i != 257 && i != 335) {
			return false;
		} else {
			this.onDone();
			return true;
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawString(this.font, I18n.get("jigsaw_block.target_pool"), this.width / 2 - 153, 30, 10526880);
		this.targetPoolEdit.render(i, j, f);
		this.drawString(this.font, I18n.get("jigsaw_block.attachement_type"), this.width / 2 - 153, 70, 10526880);
		this.attachementTypeEdit.render(i, j, f);
		this.drawString(this.font, I18n.get("jigsaw_block.final_state"), this.width / 2 - 153, 110, 10526880);
		this.finalStateEdit.render(i, j, f);
		super.render(i, j, f);
	}
}
