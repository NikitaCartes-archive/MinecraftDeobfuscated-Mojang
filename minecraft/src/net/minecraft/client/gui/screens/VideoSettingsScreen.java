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

@Environment(EnvType.CLIENT)
public class VideoSettingsScreen extends OptionsSubScreen {
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
		super(screen, options, Component.translatable("options.videoTitle"));
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
					return Component.translatable("options.fullscreen.unavailable");
				} else {
					return integer == -1
						? Options.genericValueLabel(component, Component.translatable("options.fullscreen.current"))
						: Options.genericValueLabel(component, Component.literal(monitor.getMode(integer).toString()));
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
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
			this.minecraft.options.save();
			window.changeFullscreenVideoMode();
			this.minecraft.setScreen(this.lastScreen);
		}).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.basicListRender(poseStack, this.list, i, j, f);
	}
}
