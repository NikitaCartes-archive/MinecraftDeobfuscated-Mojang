/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.DataPackConfig;

@Environment(value=EnvType.CLIENT)
public class DataPackSelectScreen
extends PackSelectionScreen {
    private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");

    public DataPackSelectScreen(Screen screen, final DataPackConfig dataPackConfig, final BiConsumer<DataPackConfig, PackRepository<Pack>> biConsumer, final File file) {
        super(screen, new TranslatableComponent("dataPack.title"), new Function<Runnable, PackSelectionModel<?>>(){
            private DataPackConfig workingConfig;
            private final PackRepository<Pack> repository;
            {
                this.workingConfig = dataPackConfig;
                this.repository = new PackRepository<Pack>(Pack::new, new ServerPacksSource(), new FolderRepositorySource(file, PackSource.DEFAULT));
            }

            @Override
            public PackSelectionModel<?> apply(Runnable runnable) {
                this.repository.reload();
                List<String> list3 = this.workingConfig.getEnabled();
                List list22 = DataPackSelectScreen.getPacksByName(this.repository, list3.stream());
                List list32 = DataPackSelectScreen.getPacksByName(this.repository, this.repository.getAvailableIds().stream().filter(string -> !list3.contains(string)));
                return new PackSelectionModel<Pack>(runnable, (pack, textureManager) -> textureManager.bind(DEFAULT_ICON), Lists.reverse(list22), list32, (list, list2, bl) -> {
                    List list3 = Lists.reverse(list).stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
                    List list4 = list2.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
                    this.workingConfig = new DataPackConfig(list3, list4);
                    if (!bl) {
                        this.repository.setSelected(list3);
                        biConsumer.accept(this.workingConfig, this.repository);
                    }
                });
            }

            @Override
            public /* synthetic */ Object apply(Object object) {
                return this.apply((Runnable)object);
            }
        }, (Minecraft minecraft) -> file);
    }

    private static List<Pack> getPacksByName(PackRepository<Pack> packRepository, Stream<String> stream) {
        return stream.map(packRepository::getPack).filter(Objects::nonNull).collect(ImmutableList.toImmutableList());
    }
}

