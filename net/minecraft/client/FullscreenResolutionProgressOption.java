/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class FullscreenResolutionProgressOption
extends ProgressOption {
    public FullscreenResolutionProgressOption(Window window) {
        this(window, window.findBestMonitor());
    }

    private FullscreenResolutionProgressOption(Window window, @Nullable Monitor monitor) {
        super("options.fullscreen.resolution", -1.0, monitor != null ? (double)(monitor.getModeCount() - 1) : -1.0, 1.0f, options -> {
            if (monitor == null) {
                return -1.0;
            }
            Optional<VideoMode> optional = window.getPreferredFullscreenVideoMode();
            return optional.map(videoMode -> monitor.getVideoModeIndex((VideoMode)videoMode)).orElse(-1.0);
        }, (options, double_) -> {
            if (monitor == null) {
                return;
            }
            if (double_ == -1.0) {
                window.setPreferredFullscreenVideoMode(Optional.empty());
            } else {
                window.setPreferredFullscreenVideoMode(Optional.of(monitor.getMode(double_.intValue())));
            }
        }, (options, progressOption) -> {
            if (monitor == null) {
                return new TranslatableComponent("options.fullscreen.unavailable");
            }
            double d = progressOption.get((Options)options);
            MutableComponent mutableComponent = progressOption.createCaption();
            if (d == -1.0) {
                return mutableComponent.append(new TranslatableComponent("options.fullscreen.current"));
            }
            return mutableComponent.append(monitor.getMode((int)d).toString());
        });
    }
}

