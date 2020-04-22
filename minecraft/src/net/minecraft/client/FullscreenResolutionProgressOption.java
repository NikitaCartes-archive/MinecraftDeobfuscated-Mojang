package net.minecraft.client;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class FullscreenResolutionProgressOption extends ProgressOption {
	public FullscreenResolutionProgressOption(Window window) {
		this(window, window.findBestMonitor());
	}

	private FullscreenResolutionProgressOption(Window window, @Nullable Monitor monitor) {
		super(
			"options.fullscreen.resolution",
			-1.0,
			monitor != null ? (double)(monitor.getModeCount() - 1) : -1.0,
			1.0F,
			options -> {
				if (monitor == null) {
					return -1.0;
				} else {
					Optional<VideoMode> optional = window.getPreferredFullscreenVideoMode();
					return (Double)optional.map(videoMode -> (double)monitor.getVideoModeIndex(videoMode)).orElse(-1.0);
				}
			},
			(options, double_) -> {
				if (monitor != null) {
					if (double_ == -1.0) {
						window.setPreferredFullscreenVideoMode(Optional.empty());
					} else {
						window.setPreferredFullscreenVideoMode(Optional.of(monitor.getMode(double_.intValue())));
					}
				}
			},
			(options, progressOption) -> {
				if (monitor == null) {
					return new TranslatableComponent("options.fullscreen.unavailable");
				} else {
					double d = progressOption.get(options);
					MutableComponent mutableComponent = progressOption.createCaption();
					return d == -1.0
						? mutableComponent.append(new TranslatableComponent("options.fullscreen.current"))
						: mutableComponent.append(monitor.getMode((int)d).toString());
				}
			}
		);
	}
}
