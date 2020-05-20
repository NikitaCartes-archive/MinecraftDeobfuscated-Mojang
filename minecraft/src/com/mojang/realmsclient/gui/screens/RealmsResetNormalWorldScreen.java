package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsResetNormalWorldScreen extends RealmsScreen {
	private final RealmsResetWorldScreen lastScreen;
	private RealmsLabel titleLabel;
	private EditBox seedEdit;
	private Boolean generateStructures = true;
	private Integer levelTypeIndex = 0;
	private Component[] levelTypes = new Component[]{
		new TranslatableComponent("generator.default"),
		new TranslatableComponent("generator.flat"),
		new TranslatableComponent("generator.large_biomes"),
		new TranslatableComponent("generator.amplified")
	};
	private Component buttonTitle;

	public RealmsResetNormalWorldScreen(RealmsResetWorldScreen realmsResetWorldScreen, Component component) {
		this.lastScreen = realmsResetWorldScreen;
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
		this.addButton(new Button(this.width / 2 - 102, row(4), 205, 20, this.levelTypeTitle(), button -> {
			this.levelTypeIndex = (this.levelTypeIndex + 1) % this.levelTypes.length;
			button.setMessage(this.levelTypeTitle());
		}));
		this.addButton(new Button(this.width / 2 - 102, row(6) - 2, 205, 20, this.generateStructuresTitle(), button -> {
			this.generateStructures = !this.generateStructures;
			button.setMessage(this.generateStructuresTitle());
		}));
		this.addButton(
			new Button(
				this.width / 2 - 102,
				row(12),
				97,
				20,
				this.buttonTitle,
				button -> this.lastScreen.resetWorld(new RealmsResetWorldScreen.ResetWorldInfo(this.seedEdit.getValue(), this.levelTypeIndex, this.generateStructures))
			)
		);
		this.addButton(new Button(this.width / 2 + 8, row(12), 97, 20, CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)));
		this.narrateLabels();
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.minecraft.setScreen(this.lastScreen);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.titleLabel.render(this, poseStack);
		this.font.draw(poseStack, I18n.get("mco.reset.world.seed"), (float)(this.width / 2 - 100), (float)row(1), 10526880);
		this.seedEdit.render(poseStack, i, j, f);
		super.render(poseStack, i, j, f);
	}

	private Component levelTypeTitle() {
		return new TranslatableComponent("selectWorld.mapType").append(" ").append(this.levelTypes[this.levelTypeIndex]);
	}

	private Component generateStructuresTitle() {
		String string = this.generateStructures ? "mco.configure.world.on" : "mco.configure.world.off";
		return new TranslatableComponent("selectWorld.mapFeatures").append(" ").append(new TranslatableComponent(string));
	}
}
