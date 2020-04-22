package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;

@Environment(EnvType.CLIENT)
public class JigsawBlockEditScreen extends Screen {
	private final JigsawBlockEntity jigsawEntity;
	private EditBox nameEdit;
	private EditBox targetEdit;
	private EditBox poolEdit;
	private EditBox finalStateEdit;
	private int levels;
	private Button jointButton;
	private Button doneButton;
	private JigsawBlockEntity.JointType joint;

	public JigsawBlockEditScreen(JigsawBlockEntity jigsawBlockEntity) {
		super(NarratorChatListener.NO_TITLE);
		this.jigsawEntity = jigsawBlockEntity;
	}

	@Override
	public void tick() {
		this.nameEdit.tick();
		this.targetEdit.tick();
		this.poolEdit.tick();
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
					new ResourceLocation(this.nameEdit.getValue()),
					new ResourceLocation(this.targetEdit.getValue()),
					new ResourceLocation(this.poolEdit.getValue()),
					this.finalStateEdit.getValue(),
					this.joint
				)
			);
	}

	private void sendGenerate() {
		this.minecraft.getConnection().send(new ServerboundJigsawGeneratePacket(this.jigsawEntity.getBlockPos(), this.levels));
	}

	@Override
	public void onClose() {
		this.onCancel();
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.doneButton = this.addButton(new Button(this.width / 2 - 4 - 150, 210, 150, 20, CommonComponents.GUI_DONE, button -> this.onDone()));
		this.addButton(new Button(this.width / 2 + 4, 210, 150, 20, CommonComponents.GUI_CANCEL, button -> this.onCancel()));
		this.poolEdit = new EditBox(this.font, this.width / 2 - 152, 20, 300, 20, new TranslatableComponent("jigsaw_block.pool"));
		this.poolEdit.setMaxLength(128);
		this.poolEdit.setValue(this.jigsawEntity.getPool().toString());
		this.poolEdit.setResponder(string -> this.updateValidity());
		this.children.add(this.poolEdit);
		this.nameEdit = new EditBox(this.font, this.width / 2 - 152, 55, 300, 20, new TranslatableComponent("jigsaw_block.name"));
		this.nameEdit.setMaxLength(128);
		this.nameEdit.setValue(this.jigsawEntity.getName().toString());
		this.nameEdit.setResponder(string -> this.updateValidity());
		this.children.add(this.nameEdit);
		this.targetEdit = new EditBox(this.font, this.width / 2 - 152, 90, 300, 20, new TranslatableComponent("jigsaw_block.target"));
		this.targetEdit.setMaxLength(128);
		this.targetEdit.setValue(this.jigsawEntity.getTarget().toString());
		this.targetEdit.setResponder(string -> this.updateValidity());
		this.children.add(this.targetEdit);
		this.finalStateEdit = new EditBox(this.font, this.width / 2 - 152, 125, 300, 20, new TranslatableComponent("jigsaw_block.final_state"));
		this.finalStateEdit.setMaxLength(256);
		this.finalStateEdit.setValue(this.jigsawEntity.getFinalState());
		this.children.add(this.finalStateEdit);
		this.addButton(new AbstractSliderButton(this.width / 2 - 152, 180, 150, 20, TextComponent.EMPTY, 0.0) {
			{
				this.updateMessage();
			}

			@Override
			protected void updateMessage() {
				this.setMessage(new TranslatableComponent("jigsaw_block.levels", JigsawBlockEditScreen.this.levels));
			}

			@Override
			protected void applyValue() {
				JigsawBlockEditScreen.this.levels = Mth.floor(Mth.clampedLerp(0.0, 7.0, this.value));
			}
		});
		this.addButton(new Button(this.width / 2 + 4, 180, 150, 20, new TranslatableComponent("jigsaw_block.generate"), button -> this.sendGenerate()));
		this.joint = this.jigsawEntity.getJoint();
		int i = this.font.width(I18n.get("jigsaw_block.joint_label")) + 10;
		this.jointButton = this.addButton(new Button(this.width / 2 - 152 + i, 150, 300 - i, 20, this.getJointText(), button -> {
			JigsawBlockEntity.JointType[] jointTypes = JigsawBlockEntity.JointType.values();
			int ix = (this.joint.ordinal() + 1) % jointTypes.length;
			this.joint = jointTypes[ix];
			button.setMessage(this.getJointText());
		}));
		boolean bl = JigsawBlock.getFrontFacing(this.jigsawEntity.getBlockState()).getAxis().isVertical();
		this.jointButton.active = bl;
		this.jointButton.visible = bl;
		this.setInitialFocus(this.poolEdit);
		this.updateValidity();
	}

	private void updateValidity() {
		this.doneButton.active = ResourceLocation.isValidResourceLocation(this.nameEdit.getValue())
			&& ResourceLocation.isValidResourceLocation(this.targetEdit.getValue())
			&& ResourceLocation.isValidResourceLocation(this.poolEdit.getValue());
	}

	@Override
	public void resize(Minecraft minecraft, int i, int j) {
		String string = this.nameEdit.getValue();
		String string2 = this.targetEdit.getValue();
		String string3 = this.poolEdit.getValue();
		String string4 = this.finalStateEdit.getValue();
		int k = this.levels;
		JigsawBlockEntity.JointType jointType = this.joint;
		this.init(minecraft, i, j);
		this.nameEdit.setValue(string);
		this.targetEdit.setValue(string2);
		this.poolEdit.setValue(string3);
		this.finalStateEdit.setValue(string4);
		this.levels = k;
		this.joint = jointType;
		this.jointButton.setMessage(this.getJointText());
	}

	private Component getJointText() {
		return new TranslatableComponent("jigsaw_block.joint." + this.joint.getSerializedName());
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawString(poseStack, this.font, I18n.get("jigsaw_block.pool"), this.width / 2 - 153, 10, 10526880);
		this.poolEdit.render(poseStack, i, j, f);
		this.drawString(poseStack, this.font, I18n.get("jigsaw_block.name"), this.width / 2 - 153, 45, 10526880);
		this.nameEdit.render(poseStack, i, j, f);
		this.drawString(poseStack, this.font, I18n.get("jigsaw_block.target"), this.width / 2 - 153, 80, 10526880);
		this.targetEdit.render(poseStack, i, j, f);
		this.drawString(poseStack, this.font, I18n.get("jigsaw_block.final_state"), this.width / 2 - 153, 115, 10526880);
		this.finalStateEdit.render(poseStack, i, j, f);
		if (JigsawBlock.getFrontFacing(this.jigsawEntity.getBlockState()).getAxis().isVertical()) {
			this.drawString(poseStack, this.font, I18n.get("jigsaw_block.joint_label"), this.width / 2 - 153, 156, 16777215);
		}

		super.render(poseStack, i, j, f);
	}
}
