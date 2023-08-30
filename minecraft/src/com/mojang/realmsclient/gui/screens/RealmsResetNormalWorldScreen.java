package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.util.LevelType;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsResetNormalWorldScreen extends RealmsScreen {
	private static final Component SEED_LABEL = Component.translatable("mco.reset.world.seed");
	public static final Component TITLE = Component.translatable("mco.reset.world.generate");
	private static final int BUTTON_SPACING = 10;
	private static final int CONTENT_WIDTH = 210;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final Consumer<WorldGenerationInfo> callback;
	private EditBox seedEdit;
	private LevelType levelType = LevelType.DEFAULT;
	private boolean generateStructures = true;
	private final Component buttonTitle;

	public RealmsResetNormalWorldScreen(Consumer<WorldGenerationInfo> consumer, Component component) {
		super(TITLE);
		this.callback = consumer;
		this.buttonTitle = component;
	}

	@Override
	public void init() {
		this.seedEdit = new EditBox(this.font, 210, 20, Component.translatable("mco.reset.world.seed"));
		this.seedEdit.setMaxLength(32);
		this.setInitialFocus(this.seedEdit);
		this.layout.addToHeader(new StringWidget(this.title, this.font));
		LinearLayout linearLayout = this.layout.addToContents(LinearLayout.vertical()).spacing(10);
		linearLayout.addChild(CommonLayouts.labeledElement(this.font, this.seedEdit, SEED_LABEL));
		linearLayout.addChild(
			CycleButton.<LevelType>builder(LevelType::getName)
				.withValues(LevelType.values())
				.withInitialValue(this.levelType)
				.create(0, 0, 210, 20, Component.translatable("selectWorld.mapType"), (cycleButton, levelType) -> this.levelType = levelType)
		);
		linearLayout.addChild(
			CycleButton.onOffBuilder(this.generateStructures)
				.create(0, 0, 210, 20, Component.translatable("selectWorld.mapFeatures"), (cycleButton, boolean_) -> this.generateStructures = boolean_)
		);
		LinearLayout linearLayout2 = this.layout.addToFooter(LinearLayout.horizontal().spacing(10));
		linearLayout2.addChild(Button.builder(this.buttonTitle, button -> this.callback.accept(this.createWorldGenerationInfo())).build());
		linearLayout2.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).build());
		this.layout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
		this.repositionElements();
	}

	private WorldGenerationInfo createWorldGenerationInfo() {
		return new WorldGenerationInfo(this.seedEdit.getValue(), this.levelType, this.generateStructures);
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	public void onClose() {
		this.callback.accept(null);
	}
}
