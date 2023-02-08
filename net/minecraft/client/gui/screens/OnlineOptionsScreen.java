/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OnlineOptionsScreen
extends SimpleOptionsSubScreen {
    @Nullable
    private final OptionInstance<Unit> difficultyDisplay;

    public static OnlineOptionsScreen createOnlineOptionsScreen(Minecraft minecraft, Screen screen, Options options) {
        ArrayList<OptionInstance> list = Lists.newArrayList();
        list.add(options.realmsNotifications());
        list.add(options.allowServerListing());
        OptionInstance optionInstance = Optionull.map(minecraft.level, clientLevel -> {
            Difficulty difficulty = clientLevel.getDifficulty();
            return new OptionInstance<Unit>("options.difficulty.online", OptionInstance.noTooltip(), (component, unit) -> difficulty.getDisplayName(), new OptionInstance.Enum<Unit>(List.of(Unit.INSTANCE), Codec.EMPTY.codec()), Unit.INSTANCE, unit -> {});
        });
        if (optionInstance != null) {
            list.add(optionInstance);
        }
        return new OnlineOptionsScreen(screen, options, list.toArray(new OptionInstance[0]), optionInstance);
    }

    private OnlineOptionsScreen(Screen screen, Options options, OptionInstance<?>[] optionInstances, @Nullable OptionInstance<Unit> optionInstance) {
        super(screen, options, Component.translatable("options.online.title"), optionInstances);
        this.difficultyDisplay = optionInstance;
    }

    @Override
    protected void init() {
        AbstractWidget abstractWidget;
        super.init();
        if (this.difficultyDisplay != null && (abstractWidget = this.list.findOption(this.difficultyDisplay)) != null) {
            abstractWidget.active = false;
        }
        if ((abstractWidget = this.list.findOption(this.options.telemetryOptInExtra())) != null) {
            abstractWidget.active = this.minecraft.extraTelemetryAvailable();
        }
    }
}

