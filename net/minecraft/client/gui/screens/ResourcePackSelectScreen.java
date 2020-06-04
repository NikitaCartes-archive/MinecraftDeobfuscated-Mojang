/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.resources.ResourcePack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

@Environment(value=EnvType.CLIENT)
public class ResourcePackSelectScreen
extends PackSelectionScreen {
    public ResourcePackSelectScreen(Screen screen, Options options, PackRepository<ResourcePack> packRepository, Runnable runnable) {
        super(screen, new TranslatableComponent("resourcePack.title"), (Runnable runnable2) -> {
            packRepository.reload();
            ArrayList list3 = Lists.newArrayList(packRepository.getSelectedPacks());
            ArrayList list22 = Lists.newArrayList(packRepository.getAvailablePacks());
            list22.removeAll(list3);
            return new PackSelectionModel<ResourcePack>((Runnable)runnable2, ResourcePack::bindIcon, Lists.reverse(list3), list22, (list, list2, bl) -> {
                List<ResourcePack> list3 = Lists.reverse(list);
                List list4 = list3.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
                packRepository.setSelected(list4);
                options.resourcePacks.clear();
                options.incompatibleResourcePacks.clear();
                for (ResourcePack resourcePack : list3) {
                    if (resourcePack.isFixedPosition()) continue;
                    options.resourcePacks.add(resourcePack.getId());
                    if (resourcePack.getCompatibility().isCompatible()) continue;
                    options.incompatibleResourcePacks.add(resourcePack.getId());
                }
                options.save();
                if (!bl) {
                    runnable.run();
                }
            });
        }, Minecraft::getResourcePackDirectory);
    }
}

