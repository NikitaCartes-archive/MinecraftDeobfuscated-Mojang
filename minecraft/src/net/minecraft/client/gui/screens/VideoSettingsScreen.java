package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
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
	private OptionsList list;
	private final GpuWarnlistManager gpuWarnlistManager;
	private final int oldMipmaps;

	private static OptionInstance<?>[] options(Options options) {
		return new OptionInstance[]{
			options.graphicsMode(),
			options.renderDistance(),
			options.prioritizeChunkUpdates(),
			options.simulationDistance(),
			options.ambientOcclusion(),
			options.framerateLimit(),
			options.enableVsync(),
			options.bobView(),
			options.guiScale(),
			options.attackIndicator(),
			options.gamma(),
			options.cloudStatus(),
			options.fullscreen(),
			options.particles(),
			options.mipmapLevels(),
			options.entityShadows(),
			options.screenEffectScale(),
			options.entityDistanceScaling(),
			options.fovEffectScale(),
			options.showAutosaveIndicator()
		};
	}

	public VideoSettingsScreen(Screen screen, Options options) {
		super(screen, options, new TranslatableComponent("options.videoTitle"));
		this.gpuWarnlistManager = screen.minecraft.getGpuWarnlistManager();
		this.gpuWarnlistManager.resetWarnings();
		if (options.graphicsMode().get() == GraphicsStatus.FABULOUS) {
			this.gpuWarnlistManager.dismissWarning();
		}

		this.oldMipmaps = options.mipmapLevels().get();
	}

	@Override
	protected void init() {
		this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		int i = -1;
		Window window = this.minecraft.getWindow();
		Monitor monitor = window.findBestMonitor();
		int j;
		if (monitor == null) {
			j = -1;
		} else {
			Optional<VideoMode> optional = window.getPreferredFullscreenVideoMode();
			j = (Integer)optional.map(monitor::getVideoModeIndex).orElse(-1);
		}

		OptionInstance<Integer> optionInstance = new OptionInstance<>(
			"options.fullscreen.resolution",
			OptionInstance.noTooltip(),
			(component, integer) -> {
				if (monitor == null) {
					return new TranslatableComponent("options.fullscreen.unavailable");
				} else {
					return integer == -1
						? Options.genericValueLabel(component, new TranslatableComponent("options.fullscreen.current"))
						: Options.genericValueLabel(component, new TextComponent(monitor.getMode(integer).toString()));
				}
			},
			new OptionInstance.IntRange(-1, monitor != null ? monitor.getModeCount() - 1 : -1),
			j,
			integer -> {
				if (monitor != null) {
					window.setPreferredFullscreenVideoMode(integer == -1 ? Optional.empty() : Optional.of(monitor.getMode(integer)));
				}
			}
		);
		this.list.addBig(optionInstance);
		this.list.addBig(this.options.biomeBlendRadius());
		this.list.addSmall(options(this.options));
		this.addWidget(this.list);
		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, button -> {
			this.minecraft.options.save();
			window.changeFullscreenVideoMode();
			this.minecraft.setScreen(this.lastScreen);
		}));
	}

	@Override
	public void removed() {
		if (this.options.mipmapLevels().get() != this.oldMipmaps) {
			this.minecraft.updateMaxMipLevel(this.options.mipmapLevels().get());
			this.minecraft.delayTextureReload();
		}

		super.removed();
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		int j = this.options.guiScale().get();
		if (super.mouseClicked(d, e, i)) {
			if (this.options.guiScale().get() != j) {
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
					this.options.graphicsMode().set(GraphicsStatus.FABULOUS);
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
		int j = this.options.guiScale().get();
		if (super.mouseReleased(d, e, i)) {
			return true;
		} else if (this.list.mouseReleased(d, e, i)) {
			if (this.options.guiScale().get() != j) {
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
		this.renderTooltip(poseStack, list, i, j);
	}
}
