package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.FullscreenResolutionProgressOption;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class VideoSettingsScreen extends OptionsSubScreen {
	@Nullable
	private List<FormattedText> tooltip;
	private OptionsList list;
	private static final Option[] OPTIONS = new Option[]{
		Option.GRAPHICS,
		Option.RENDER_DISTANCE,
		Option.AMBIENT_OCCLUSION,
		Option.FRAMERATE_LIMIT,
		Option.ENABLE_VSYNC,
		Option.VIEW_BOBBING,
		Option.GUI_SCALE,
		Option.ATTACK_INDICATOR,
		Option.GAMMA,
		Option.RENDER_CLOUDS,
		Option.USE_FULLSCREEN,
		Option.PARTICLES,
		Option.MIPMAP_LEVELS,
		Option.ENTITY_SHADOWS,
		Option.ENTITY_DISTANCE_SCALING
	};
	private int oldMipmaps;

	public VideoSettingsScreen(Screen screen, Options options) {
		super(screen, options, new TranslatableComponent("options.videoTitle"));
		this.oldMipmaps = options.mipmapLevels;
	}

	@Override
	protected void init() {
		this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		this.list.addBig(new FullscreenResolutionProgressOption(this.minecraft.getWindow()));
		this.list.addBig(Option.BIOME_BLEND_RADIUS);
		this.list.addSmall(OPTIONS);
		this.children.add(this.list);
		this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, button -> {
			this.minecraft.options.save();
			this.minecraft.getWindow().changeFullscreenVideoMode();
			this.minecraft.setScreen(this.lastScreen);
		}));
	}

	@Override
	public void removed() {
		if (this.options.mipmapLevels != this.oldMipmaps) {
			this.minecraft.updateMaxMipLevel(this.options.mipmapLevels);
			this.minecraft.delayTextureReload();
		}

		super.removed();
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		int j = this.options.guiScale;
		if (super.mouseClicked(d, e, i)) {
			if (this.options.guiScale != j) {
				this.minecraft.resizeDisplay();
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		int j = this.options.guiScale;
		if (super.mouseReleased(d, e, i)) {
			return true;
		} else if (this.list.mouseReleased(d, e, i)) {
			if (this.options.guiScale != j) {
				this.minecraft.resizeDisplay();
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.tooltip = null;
		Optional<AbstractWidget> optional = this.list.getMouseOver((double)i, (double)j);
		if (optional.isPresent() && optional.get() instanceof OptionButton) {
			Optional<TranslatableComponent> optional2 = ((OptionButton)optional.get()).getOption().getTooltip();
			if (optional2.isPresent()) {
				Builder<FormattedText> builder = ImmutableList.builder();
				this.font.split((FormattedText)optional2.get(), 200).forEach(builder::add);
				this.tooltip = builder.build();
			}
		}

		this.renderBackground(poseStack);
		this.list.render(poseStack, i, j, f);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 5, 16777215);
		super.render(poseStack, i, j, f);
		if (this.tooltip != null) {
			this.renderTooltip(poseStack, this.tooltip, i, j);
		}
	}
}
