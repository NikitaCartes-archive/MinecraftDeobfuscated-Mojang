/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;

@Environment(value=EnvType.CLIENT)
public class JigsawBlockEditScreen
extends Screen {
    private static final int MAX_LEVELS = 7;
    private static final Component JOINT_LABEL = Component.translatable("jigsaw_block.joint_label");
    private static final Component POOL_LABEL = Component.translatable("jigsaw_block.pool");
    private static final Component NAME_LABEL = Component.translatable("jigsaw_block.name");
    private static final Component TARGET_LABEL = Component.translatable("jigsaw_block.target");
    private static final Component FINAL_STATE_LABEL = Component.translatable("jigsaw_block.final_state");
    private final JigsawBlockEntity jigsawEntity;
    private EditBox nameEdit;
    private EditBox targetEdit;
    private EditBox poolEdit;
    private EditBox finalStateEdit;
    int levels;
    private boolean keepJigsaws = true;
    private CycleButton<JigsawBlockEntity.JointType> jointButton;
    private Button doneButton;
    private Button generateButton;
    private JigsawBlockEntity.JointType joint;

    public JigsawBlockEditScreen(JigsawBlockEntity jigsawBlockEntity) {
        super(GameNarrator.NO_TITLE);
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
        this.minecraft.getConnection().send(new ServerboundSetJigsawBlockPacket(this.jigsawEntity.getBlockPos(), new ResourceLocation(this.nameEdit.getValue()), new ResourceLocation(this.targetEdit.getValue()), new ResourceLocation(this.poolEdit.getValue()), this.finalStateEdit.getValue(), this.joint));
    }

    private void sendGenerate() {
        this.minecraft.getConnection().send(new ServerboundJigsawGeneratePacket(this.jigsawEntity.getBlockPos(), this.levels, this.keepJigsaws));
    }

    @Override
    public void onClose() {
        this.onCancel();
    }

    @Override
    protected void init() {
        boolean bl;
        this.poolEdit = new EditBox(this.font, this.width / 2 - 152, 20, 300, 20, Component.translatable("jigsaw_block.pool"));
        this.poolEdit.setMaxLength(128);
        this.poolEdit.setValue(this.jigsawEntity.getPool().location().toString());
        this.poolEdit.setResponder(string -> this.updateValidity());
        this.addWidget(this.poolEdit);
        this.nameEdit = new EditBox(this.font, this.width / 2 - 152, 55, 300, 20, Component.translatable("jigsaw_block.name"));
        this.nameEdit.setMaxLength(128);
        this.nameEdit.setValue(this.jigsawEntity.getName().toString());
        this.nameEdit.setResponder(string -> this.updateValidity());
        this.addWidget(this.nameEdit);
        this.targetEdit = new EditBox(this.font, this.width / 2 - 152, 90, 300, 20, Component.translatable("jigsaw_block.target"));
        this.targetEdit.setMaxLength(128);
        this.targetEdit.setValue(this.jigsawEntity.getTarget().toString());
        this.targetEdit.setResponder(string -> this.updateValidity());
        this.addWidget(this.targetEdit);
        this.finalStateEdit = new EditBox(this.font, this.width / 2 - 152, 125, 300, 20, Component.translatable("jigsaw_block.final_state"));
        this.finalStateEdit.setMaxLength(256);
        this.finalStateEdit.setValue(this.jigsawEntity.getFinalState());
        this.addWidget(this.finalStateEdit);
        this.joint = this.jigsawEntity.getJoint();
        int i = this.font.width(JOINT_LABEL) + 10;
        this.jointButton = this.addRenderableWidget(CycleButton.builder(JigsawBlockEntity.JointType::getTranslatedName).withValues((JigsawBlockEntity.JointType[])JigsawBlockEntity.JointType.values()).withInitialValue(this.joint).displayOnlyValue().create(this.width / 2 - 152 + i, 150, 300 - i, 20, JOINT_LABEL, (cycleButton, jointType) -> {
            this.joint = jointType;
        }));
        this.jointButton.active = bl = JigsawBlock.getFrontFacing(this.jigsawEntity.getBlockState()).getAxis().isVertical();
        this.jointButton.visible = bl;
        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 154, 180, 100, 20, CommonComponents.EMPTY, 0.0){
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                this.setMessage(Component.translatable("jigsaw_block.levels", JigsawBlockEditScreen.this.levels));
            }

            @Override
            protected void applyValue() {
                JigsawBlockEditScreen.this.levels = Mth.floor(Mth.clampedLerp(0.0, 7.0, this.value));
            }
        });
        this.addRenderableWidget(CycleButton.onOffBuilder(this.keepJigsaws).create(this.width / 2 - 50, 180, 100, 20, Component.translatable("jigsaw_block.keep_jigsaws"), (cycleButton, boolean_) -> {
            this.keepJigsaws = boolean_;
        }));
        this.generateButton = this.addRenderableWidget(Button.builder(Component.translatable("jigsaw_block.generate"), button -> {
            this.onDone();
            this.sendGenerate();
        }).bounds(this.width / 2 + 54, 180, 100, 20).build());
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onCancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
        this.setInitialFocus(this.poolEdit);
        this.updateValidity();
    }

    private void updateValidity() {
        boolean bl;
        this.doneButton.active = bl = ResourceLocation.isValidResourceLocation(this.nameEdit.getValue()) && ResourceLocation.isValidResourceLocation(this.targetEdit.getValue()) && ResourceLocation.isValidResourceLocation(this.poolEdit.getValue());
        this.generateButton.active = bl;
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
        this.jointButton.setValue(jointType);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        if (this.doneButton.active && (i == 257 || i == 335)) {
            this.onDone();
            return true;
        }
        return false;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        JigsawBlockEditScreen.drawString(poseStack, this.font, POOL_LABEL, this.width / 2 - 153, 10, 0xA0A0A0);
        this.poolEdit.render(poseStack, i, j, f);
        JigsawBlockEditScreen.drawString(poseStack, this.font, NAME_LABEL, this.width / 2 - 153, 45, 0xA0A0A0);
        this.nameEdit.render(poseStack, i, j, f);
        JigsawBlockEditScreen.drawString(poseStack, this.font, TARGET_LABEL, this.width / 2 - 153, 80, 0xA0A0A0);
        this.targetEdit.render(poseStack, i, j, f);
        JigsawBlockEditScreen.drawString(poseStack, this.font, FINAL_STATE_LABEL, this.width / 2 - 153, 115, 0xA0A0A0);
        this.finalStateEdit.render(poseStack, i, j, f);
        if (JigsawBlock.getFrontFacing(this.jigsawEntity.getBlockState()).getAxis().isVertical()) {
            JigsawBlockEditScreen.drawString(poseStack, this.font, JOINT_LABEL, this.width / 2 - 153, 156, 0xFFFFFF);
        }
        super.render(poseStack, i, j, f);
    }
}

