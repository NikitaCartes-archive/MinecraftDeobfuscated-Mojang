package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.util.LevelType;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsResetNormalWorldScreen extends RealmsScreen {
	private static final Component SEED_LABEL = new TranslatableComponent("mco.reset.world.seed");
	private final Consumer<WorldGenerationInfo> callback;
	private RealmsLabel titleLabel;
	private EditBox seedEdit;
	private LevelType levelType = LevelType.DEFAULT;
	private boolean generateStructures = true;
	private final Component buttonTitle;

	public RealmsResetNormalWorldScreen(Consumer<WorldGenerationInfo> consumer, Component component) {
		this.callback = consumer;
		this.buttonTitle = component;
	}

	@Override
	public void tick() {
		this.seedEdit.tick();
		super.tick();
	}

	@Override
	public void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.titleLabel = new RealmsLabel(new TranslatableComponent("mco.reset.world.generate"), this.width / 2, 17, 16777215);
		this.addWidget(this.titleLabel);
		this.seedEdit = new EditBox(this.minecraft.font, this.width / 2 - 100, row(2), 200, 20, null, new TranslatableComponent("mco.reset.world.seed"));
		this.seedEdit.setMaxLength(32);
		this.addWidget(this.seedEdit);
		this.setInitialFocus(this.seedEdit);
		this.addButton(
			CycleButton.<LevelType>builder(LevelType::getName)
				.withValues(LevelType.values())
				.withInitialValue(this.levelType)
				.create(this.width / 2 - 102, row(4), 205, 20, new TranslatableComponent("selectWorld.mapType"), (cycleButton, levelType) -> this.levelType = levelType)
		);
		this.addButton(
			CycleButton.onOffBuilder(this.generateStructures)
				.create(
					this.width / 2 - 102,
					row(6) - 2,
					205,
					20,
					new TranslatableComponent("selectWorld.mapFeatures"),
					(cycleButton, boolean_) -> this.generateStructures = boolean_
				)
		);
		this.addButton(
			new Button(
				this.width / 2 - 102,
				row(12),
				97,
				20,
				this.buttonTitle,
				button -> this.callback.accept(new WorldGenerationInfo(this.seedEdit.getValue(), this.levelType, this.generateStructures))
			)
		);
		this.addButton(new Button(this.width / 2 + 8, row(12), 97, 20, CommonComponents.GUI_BACK, button -> this.onClose()));
		this.narrateLabels();
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public void onClose() {
		this.callback.accept(null);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.titleLabel.render(this, poseStack);
		this.font.draw(poseStack, SEED_LABEL, (float)(this.width / 2 - 100), (float)row(1), 10526880);
		this.seedEdit.render(poseStack, i, j, f);
		super.render(poseStack, i, j, f);
	}
}
