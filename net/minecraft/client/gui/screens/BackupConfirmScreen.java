/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BackupConfirmScreen
extends Screen {
    @Nullable
    private final Screen lastScreen;
    protected final Listener listener;
    private final Component description;
    private final boolean promptForCacheErase;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    protected int id;
    private Checkbox eraseCache;

    public BackupConfirmScreen(@Nullable Screen screen, Listener listener, Component component, Component component2, boolean bl) {
        super(component);
        this.lastScreen = screen;
        this.listener = listener;
        this.description = component2;
        this.promptForCacheErase = bl;
    }

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, (FormattedText)this.description, this.width - 50);
        int i = (this.message.getLineCount() + 1) * this.font.lineHeight;
        this.addButton(new Button(this.width / 2 - 155, 100 + i, 150, 20, new TranslatableComponent("selectWorld.backupJoinConfirmButton"), button -> this.listener.proceed(true, this.eraseCache.selected())));
        this.addButton(new Button(this.width / 2 - 155 + 160, 100 + i, 150, 20, new TranslatableComponent("selectWorld.backupJoinSkipButton"), button -> this.listener.proceed(false, this.eraseCache.selected())));
        this.addButton(new Button(this.width / 2 - 155 + 80, 124 + i, 150, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen)));
        this.eraseCache = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, new TranslatableComponent("selectWorld.backupEraseCache"), false);
        if (this.promptForCacheErase) {
            this.addButton(this.eraseCache);
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        BackupConfirmScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 0xFFFFFF);
        this.message.renderCentered(poseStack, this.width / 2, 70);
        super.render(poseStack, i, j, f);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Listener {
        public void proceed(boolean var1, boolean var2);
    }
}

