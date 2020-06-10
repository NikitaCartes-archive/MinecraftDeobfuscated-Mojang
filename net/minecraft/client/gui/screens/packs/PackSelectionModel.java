/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;

@Environment(value=EnvType.CLIENT)
public class PackSelectionModel<T extends Pack> {
    private final PackRepository<T> repository;
    private final List<T> selected;
    private final List<T> unselected;
    private final BiConsumer<T, TextureManager> iconBinder;
    private final Runnable onListChanged;
    private final Consumer<PackRepository<T>> output;

    public PackSelectionModel(Runnable runnable, BiConsumer<T, TextureManager> biConsumer, PackRepository<T> packRepository, Consumer<PackRepository<T>> consumer) {
        this.onListChanged = runnable;
        this.iconBinder = biConsumer;
        this.repository = packRepository;
        this.selected = Lists.newArrayList(packRepository.getSelectedPacks());
        Collections.reverse(this.selected);
        this.unselected = Lists.newArrayList(packRepository.getAvailablePacks());
        this.unselected.removeAll(this.selected);
        this.output = consumer;
    }

    public Stream<Entry> getUnselected() {
        return this.unselected.stream().map(pack -> new UnselectedPackEntry(this, pack));
    }

    public Stream<Entry> getSelected() {
        return this.selected.stream().map(pack -> new SelectedPackEntry(this, pack));
    }

    public void commit() {
        this.repository.setSelected(Lists.reverse(this.selected).stream().map(Pack::getId).collect(ImmutableList.toImmutableList()));
        this.output.accept(this.repository);
    }

    public void findNewPacks() {
        this.repository.reload();
        this.unselected.clear();
        this.unselected.addAll(this.repository.getAvailablePacks());
        this.unselected.removeAll(this.selected);
    }

    @Environment(value=EnvType.CLIENT)
    static class UnselectedPackEntry
    extends EntryBase {
        final /* synthetic */ PackSelectionModel field_25463;

        public UnselectedPackEntry(T pack) {
            this.field_25463 = packSelectionModel;
            super((PackSelectionModel)packSelectionModel, pack);
        }

        @Override
        protected List<T> getSelfList() {
            return this.field_25463.unselected;
        }

        @Override
        protected List<T> getOtherList() {
            return this.field_25463.selected;
        }

        @Override
        public boolean isSelected() {
            return false;
        }

        @Override
        public void select() {
            this.toggleSelection();
        }

        @Override
        public void unselect() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class SelectedPackEntry
    extends EntryBase {
        final /* synthetic */ PackSelectionModel field_25462;

        public SelectedPackEntry(T pack) {
            this.field_25462 = packSelectionModel;
            super((PackSelectionModel)packSelectionModel, pack);
        }

        @Override
        protected List<T> getSelfList() {
            return this.field_25462.selected;
        }

        @Override
        protected List<T> getOtherList() {
            return this.field_25462.unselected;
        }

        @Override
        public boolean isSelected() {
            return true;
        }

        @Override
        public void select() {
        }

        @Override
        public void unselect() {
            this.toggleSelection();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static abstract class EntryBase
    implements Entry {
        private final T pack;
        final /* synthetic */ PackSelectionModel field_25460;

        public EntryBase(T pack) {
            this.field_25460 = packSelectionModel;
            this.pack = pack;
        }

        protected abstract List<T> getSelfList();

        protected abstract List<T> getOtherList();

        @Override
        public void bindIcon(TextureManager textureManager) {
            this.field_25460.iconBinder.accept(this.pack, textureManager);
        }

        @Override
        public PackCompatibility getCompatibility() {
            return ((Pack)this.pack).getCompatibility();
        }

        @Override
        public Component getTitle() {
            return ((Pack)this.pack).getTitle();
        }

        @Override
        public Component getDescription() {
            return ((Pack)this.pack).getDescription();
        }

        @Override
        public PackSource getPackSource() {
            return ((Pack)this.pack).getPackSource();
        }

        @Override
        public boolean isFixedPosition() {
            return ((Pack)this.pack).isFixedPosition();
        }

        @Override
        public boolean isRequired() {
            return ((Pack)this.pack).isRequired();
        }

        protected void toggleSelection() {
            this.getSelfList().remove(this.pack);
            ((Pack)this.pack).getDefaultPosition().insert(this.getOtherList(), this.pack, Function.identity(), true);
            this.field_25460.onListChanged.run();
        }

        protected void move(int i) {
            List list = this.getSelfList();
            int j = list.indexOf(this.pack);
            list.remove(j);
            list.add(j + i, this.pack);
            this.field_25460.onListChanged.run();
        }

        @Override
        public boolean canMoveUp() {
            List list = this.getSelfList();
            int i = list.indexOf(this.pack);
            return i > 0 && !((Pack)list.get(i - 1)).isFixedPosition();
        }

        @Override
        public void moveUp() {
            this.move(-1);
        }

        @Override
        public boolean canMoveDown() {
            List list = this.getSelfList();
            int i = list.indexOf(this.pack);
            return i >= 0 && i < list.size() - 1 && !((Pack)list.get(i + 1)).isFixedPosition();
        }

        @Override
        public void moveDown() {
            this.move(1);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Entry {
        public void bindIcon(TextureManager var1);

        public PackCompatibility getCompatibility();

        public Component getTitle();

        public Component getDescription();

        public PackSource getPackSource();

        default public FormattedText getExtendedDescription() {
            return this.getPackSource().decorate(this.getDescription());
        }

        public boolean isFixedPosition();

        public boolean isRequired();

        public void select();

        public void unselect();

        public void moveUp();

        public void moveDown();

        public boolean isSelected();

        default public boolean canSelect() {
            return !this.isSelected();
        }

        default public boolean canUnselect() {
            return this.isSelected() && !this.isRequired();
        }

        public boolean canMoveUp();

        public boolean canMoveDown();
    }
}

