package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class VideoSettingsScreen extends OptionsSubScreen {
	private static final Component TITLE = Component.translatable("options.videoTitle");
	private static final Component FABULOUS = Component.translatable("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC);
	private static final Component WARNING_MESSAGE = Component.translatable("options.graphics.warning.message", FABULOUS, FABULOUS);
	private static final Component WARNING_TITLE = Component.translatable("options.graphics.warning.title").withStyle(ChatFormatting.RED);
	private static final Component BUTTON_ACCEPT = Component.translatable("options.graphics.warning.accept");
	private static final Component BUTTON_CANCEL = Component.translatable("options.graphics.warning.cancel");
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
			options.showAutosaveIndicator(),
			options.glintSpeed(),
			options.glintStrength()
		};
	}

	public VideoSettingsScreen(Screen screen, Options options) {
		super(screen, options, TITLE);
		this.gpuWarnlistManager = screen.minecraft.getGpuWarnlistManager();
		this.gpuWarnlistManager.resetWarnings();
		if (options.graphicsMode().get() == GraphicsStatus.FABULOUS) {
			this.gpuWarnlistManager.dismissWarning();
		}

		this.oldMipmaps = options.mipmapLevels().get();
	}

	@Override
	protected void init() {
		this.list = this.addRenderableWidget(new OptionsList(this.minecraft, this.width, this.height, this));
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
					return Component.translatable("options.fullscreen.unavailable");
				} else if (integer == -1) {
					return Options.genericValueLabel(component, Component.translatable("options.fullscreen.current"));
				} else {
					VideoMode videoMode = monitor.getMode(integer);
					return Options.genericValueLabel(
						component,
						Component.translatable(
							"options.fullscreen.entry",
							videoMode.getWidth(),
							videoMode.getHeight(),
							videoMode.getRefreshRate(),
							videoMode.getRedBits() + videoMode.getGreenBits() + videoMode.getBlueBits()
						)
					);
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
		super.init();
	}

	@Override
	public void onClose() {
		this.minecraft.options.save();
		this.minecraft.getWindow().changeFullscreenVideoMode();
		super.onClose();
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		this.list.updateSize(this.width, this.layout);
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
				List<Component> list = Lists.<Component>newArrayList(WARNING_MESSAGE, CommonComponents.NEW_LINE);
				String string = this.gpuWarnlistManager.getRendererWarnings();
				if (string != null) {
					list.add(CommonComponents.NEW_LINE);
					list.add(Component.translatable("options.graphics.warning.renderer", string).withStyle(ChatFormatting.GRAY));
				}

				String string2 = this.gpuWarnlistManager.getVendorWarnings();
				if (string2 != null) {
					list.add(CommonComponents.NEW_LINE);
					list.add(Component.translatable("options.graphics.warning.vendor", string2).withStyle(ChatFormatting.GRAY));
				}

				String string3 = this.gpuWarnlistManager.getVersionWarnings();
				if (string3 != null) {
					list.add(CommonComponents.NEW_LINE);
					list.add(Component.translatable("options.graphics.warning.version", string3).withStyle(ChatFormatting.GRAY));
				}

				this.minecraft
					.setScreen(
						new UnsupportedGraphicsWarningScreen(WARNING_TITLE, list, ImmutableList.of(new UnsupportedGraphicsWarningScreen.ButtonOption(BUTTON_ACCEPT, button -> {
							this.options.graphicsMode().set(GraphicsStatus.FABULOUS);
							Minecraft.getInstance().levelRenderer.allChanged();
							this.gpuWarnlistManager.dismissWarning();
							this.minecraft.setScreen(this);
						}), new UnsupportedGraphicsWarningScreen.ButtonOption(BUTTON_CANCEL, button -> {
							this.gpuWarnlistManager.dismissWarningAndSkipFabulous();
							this.minecraft.setScreen(this);
						})))
					);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f, double g) {
		if (Screen.hasControlDown()) {
			OptionInstance<Integer> optionInstance = this.options.guiScale();
			if (optionInstance.values() instanceof OptionInstance.ClampingLazyMaxIntRange clampingLazyMaxIntRange) {
				int i = optionInstance.get() + (int)Math.signum(g);
				if (i != 0 && i <= clampingLazyMaxIntRange.maxInclusive()) {
					CycleButton<Integer> cycleButton = (CycleButton<Integer>)this.list.findOption(optionInstance);
					if (cycleButton != null) {
						optionInstance.set(i);
						cycleButton.setValue(i);
					}

					if (optionInstance.get() == i) {
						this.minecraft.resizeDisplay();
						this.list.setScrollAmount(0.0);
						return true;
					}
				}
			}

			return false;
		} else {
			return super.mouseScrolled(d, e, f, g);
		}
	}
}
