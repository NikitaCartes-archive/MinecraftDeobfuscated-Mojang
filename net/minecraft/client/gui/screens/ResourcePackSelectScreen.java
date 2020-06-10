/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import java.io.File;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.resources.ResourcePack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.PackRepository;

@Environment(value=EnvType.CLIENT)
public class ResourcePackSelectScreen
extends PackSelectionScreen {
    public ResourcePackSelectScreen(Screen screen, PackRepository<ResourcePack> packRepository, Consumer<PackRepository<ResourcePack>> consumer, File file) {
        super(screen, new TranslatableComponent("resourcePack.title"), (Runnable runnable) -> new PackSelectionModel<ResourcePack>((Runnable)runnable, ResourcePack::bindIcon, packRepository, consumer), file);
    }
}

