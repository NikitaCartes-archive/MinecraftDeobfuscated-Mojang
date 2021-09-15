package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.FullscreenResolutionProgressOption;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public class VideoSettingsScreen extends OptionsSubScreen {
	private static final Component FABULOUS = new TranslatableComponent("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC);
	private static final Component WARNING_MESSAGE = new TranslatableComponent("options.graphics.warning.message", FABULOUS, FABULOUS);
	private static final Component WARNING_TITLE = new TranslatableComponent("options.graphics.warning.title").withStyle(ChatFormatting.RED);
	private static final Component BUTTON_ACCEPT = new TranslatableComponent("options.graphics.warning.accept");
	private static final Component BUTTON_CANCEL = new TranslatableComponent("options.graphics.warning.cancel");
	private static final Component NEW_LINE = new TextComponent("\n");
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
		Option.SCREEN_EFFECTS_SCALE,
		Option.ENTITY_DISTANCE_SCALING,
		Option.FOV_EFFECTS_SCALE,
		Option.PRIORITIZE_CHUNK_UPDATES
	};
	private OptionsList list;
	private final GpuWarnlistManager gpuWarnlistManager;
	private final int oldMipmaps;

	public VideoSettingsScreen(Screen screen, Options options) {
		super(screen, options, new TranslatableComponent("options.videoTitle"));
		this.gpuWarnlistManager = screen.minecraft.getGpuWarnlistManager();
		this.gpuWarnlistManager.resetWarnings();
		if (options.graphicsMode == GraphicsStatus.FABULOUS) {
			this.gpuWarnlistManager.dismissWarning();
		}

		this.oldMipmaps = options.mipmapLevels;
	}

	@Override
	protected void init() {
		this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		this.list.addBig(new FullscreenResolutionProgressOption(this.minecraft.getWindow()));
		this.list.addBig(Option.BIOME_BLEND_RADIUS);
		this.list.addSmall(OPTIONS);
		this.addWidget(this.list);
		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, button -> {
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

			if (this.gpuWarnlistManager.isShowingWarning()) {
				List<Component> list = Lists.<Component>newArrayList(WARNING_MESSAGE, NEW_LINE);
				String string = this.gpuWarnlistManager.getRendererWarnings();
				if (string != null) {
					list.add(NEW_LINE);
					list.add(new TranslatableComponent("options.graphics.warning.renderer", string).withStyle(ChatFormatting.GRAY));
				}

				String string2 = this.gpuWarnlistManager.getVendorWarnings();
				if (string2 != null) {
					list.add(NEW_LINE);
					list.add(new TranslatableComponent("options.graphics.warning.vendor", string2).withStyle(ChatFormatting.GRAY));
				}

				String string3 = this.gpuWarnlistManager.getVersionWarnings();
				if (string3 != null) {
					list.add(NEW_LINE);
					list.add(new TranslatableComponent("options.graphics.warning.version", string3).withStyle(ChatFormatting.GRAY));
				}

				this.minecraft.setScreen(new PopupScreen(WARNING_TITLE, list, ImmutableList.of(new PopupScreen.ButtonOption(BUTTON_ACCEPT, button -> {
					this.options.graphicsMode = GraphicsStatus.FABULOUS;
					Minecraft.getInstance().levelRenderer.allChanged();
					this.gpuWarnlistManager.dismissWarning();
					this.minecraft.setScreen(this);
				}), new PopupScreen.ButtonOption(BUTTON_CANCEL, button -> {
					this.gpuWarnlistManager.dismissWarningAndSkipFabulous();
					this.minecraft.setScreen(this);
				}))));
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
		this.renderBackground(poseStack);
		this.list.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 5, 16777215);
		super.render(poseStack, i, j, f);
		List<FormattedCharSequence> list = tooltipAt(this.list, i, j);
		if (list != null) {
			this.renderTooltip(poseStack, list, i, j);
		}
	}
}
