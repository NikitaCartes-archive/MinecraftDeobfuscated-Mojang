/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BaseCommandBlock;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractCommandBlockEditScreen
extends Screen {
    protected EditBox commandEdit;
    protected EditBox previousEdit;
    protected Button doneButton;
    protected Button cancelButton;
    protected Button outputButton;
    protected boolean trackOutput;
    private CommandSuggestions commandSuggestions;

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
        this.doneButton = this.addButton(new Button(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, CommonComponents.GUI_DONE, button -> this.onDone()));
        this.cancelButton = this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, CommonComponents.GUI_CANCEL, button -> this.onClose()));
        this.outputButton = this.addButton(new Button(this.width / 2 + 150 - 20, this.getPreviousY(), 20, 20, new TextComponent("O"), button -> {
            BaseCommandBlock baseCommandBlock;
            baseCommandBlock.setTrackOutput(!(baseCommandBlock = this.getCommandBlock()).isTrackOutput());
            this.updateCommandOutput();
        }));
        this.commandEdit = new EditBox(this.font, this.width / 2 - 150, 50, 300, 20, (Component)new TranslatableComponent("advMode.command")){

            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(AbstractCommandBlockEditScreen.this.commandSuggestions.getNarrationMessage());
            }
        };
        this.commandEdit.setMaxLength(32500);
        this.commandEdit.setResponder(this::onEdited);
        this.children.add(this.commandEdit);
        this.previousEdit = new EditBox(this.font, this.width / 2 - 150, this.getPreviousY(), 276, 20, new TranslatableComponent("advMode.previousOutput"));
        this.previousEdit.setMaxLength(32500);
        this.previousEdit.setEditable(false);
        this.previousEdit.setValue("-");
        this.children.add(this.previousEdit);
        this.setInitialFocus(this.commandEdit);
        this.commandEdit.setFocus(true);
        this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.commandEdit, this.font, true, true, 0, 7, false, Integer.MIN_VALUE);
        this.commandSuggestions.setAllowSuggestions(true);
        this.commandSuggestions.updateCommandInfo();
    }

    @Override
    public void resize(Minecraft minecraft, int i, int j) {
        String string = this.commandEdit.getValue();
        this.init(minecraft, i, j);
        this.commandEdit.setValue(string);
        this.commandSuggestions.updateCommandInfo();
    }

    protected void updateCommandOutput() {
        if (this.getCommandBlock().isTrackOutput()) {
            this.outputButton.setMessage(new TextComponent("O"));
            this.previousEdit.setValue(this.getCommandBlock().getLastOutput().getString());
        } else {
            this.outputButton.setMessage(new TextComponent("X"));
            this.previousEdit.setValue("-");
        }
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

    protected abstract void populateAndSendPacket(BaseCommandBlock var1);

    @Override
    public void onClose() {
        this.getCommandBlock().setTrackOutput(this.trackOutput);
        this.minecraft.setScreen(null);
    }

    private void onEdited(String string) {
        this.commandSuggestions.updateCommandInfo();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (this.commandSuggestions.keyPressed(i, j, k)) {
            return true;
        }
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        if (i == 257 || i == 335) {
            this.onDone();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (this.commandSuggestions.mouseScrolled(f)) {
            return true;
        }
        return super.mouseScrolled(d, e, f);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (this.commandSuggestions.mouseClicked(d, e, i)) {
            return true;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.drawCenteredString(poseStack, this.font, I18n.get("advMode.setCommand", new Object[0]), this.width / 2, 20, 0xFFFFFF);
        this.drawString(poseStack, this.font, I18n.get("advMode.command", new Object[0]), this.width / 2 - 150, 40, 0xA0A0A0);
        this.commandEdit.render(poseStack, i, j, f);
        int k = 75;
        if (!this.previousEdit.getValue().isEmpty()) {
            this.drawString(poseStack, this.font, I18n.get("advMode.previousOutput", new Object[0]), this.width / 2 - 150, (k += 5 * this.font.lineHeight + 1 + this.getPreviousY() - 135) + 4, 0xA0A0A0);
            this.previousEdit.render(poseStack, i, j, f);
        }
        super.render(poseStack, i, j, f);
        this.commandSuggestions.render(poseStack, i, j);
    }
}

