package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.LevelSettings;

@Environment(EnvType.CLIENT)
public class DatapackLoadFailureScreen extends Screen {
	private final String levelId;
	private final List<FormattedText> lines = Lists.<FormattedText>newArrayList();
	@Nullable
	private final LevelSettings levelSettings;

	public DatapackLoadFailureScreen(String string, @Nullable LevelSettings levelSettings) {
		super(new TranslatableComponent("datapackFailure.title"));
		this.levelId = string;
		this.levelSettings = levelSettings;
	}

	@Override
	protected void init() {
		super.init();
		this.lines.clear();
		this.lines.addAll(this.font.split(this.getTitle(), this.width - 50));
		this.addButton(
			new Button(
				this.width / 2 - 155,
				this.height / 6 + 96,
				150,
				20,
				new TranslatableComponent("datapackFailure.safeMode"),
				button -> this.minecraft.selectLevel(this.levelId, this.levelSettings, true)
			)
		);
		this.addButton(
			new Button(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20, new TranslatableComponent("gui.toTitle"), button -> this.minecraft.setScreen(null))
		);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		int k = 70;

		for (FormattedText formattedText : this.lines) {
			this.drawCenteredString(poseStack, this.font, formattedText, this.width / 2, k, 16777215);
			k += 9;
		}

		super.render(poseStack, i, j, f);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}
