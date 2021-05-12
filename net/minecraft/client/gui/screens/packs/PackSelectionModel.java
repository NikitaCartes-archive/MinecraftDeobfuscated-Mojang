/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;

@Environment(value=EnvType.CLIENT)
public class PackSelectionModel {
    private final PackRepository repository;
    final List<Pack> selected;
    final List<Pack> unselected;
    final Function<Pack, ResourceLocation> iconGetter;
    final Runnable onListChanged;
    private final Consumer<PackRepository> output;

    public PackSelectionModel(Runnable runnable, Function<Pack, ResourceLocation> function, PackRepository packRepository, Consumer<PackRepository> consumer) {
        this.onListChanged = runnable;
        this.iconGetter = function;
        this.repository = packRepository;
        this.selected = Lists.newArrayList(packRepository.getSelectedPacks());
        Collections.reverse(this.selected);
        this.unselected = Lists.newArrayList(packRepository.getAvailablePacks());
        this.unselected.removeAll(this.selected);
        this.output = consumer;
    }

    public Stream<Entry> getUnselected() {
        return this.unselected.stream().map(pack -> new UnselectedPackEntry((Pack)pack));
    }

    public Stream<Entry> getSelected() {
        return this.selected.stream().map(pack -> new SelectedPackEntry((Pack)pack));
    }

    public void commit() {
        this.repository.setSelected(Lists.reverse(this.selected).stream().map(Pack::getId).collect(ImmutableList.toImmutableList()));
        this.output.accept(this.repository);
    }

    public void findNewPacks() {
        this.repository.reload();
        this.selected.retainAll(this.repository.getAvailablePacks());
        this.unselected.clear();
        this.unselected.addAll(this.repository.getAvailablePacks());
        this.unselected.removeAll(this.selected);
    }

    @Environment(value=EnvType.CLIENT)
    class SelectedPackEntry
    extends EntryBase {
        public SelectedPackEntry(Pack pack) {
            super(pack);
        }

        @Override
        protected List<Pack> getSelfList() {
            return PackSelectionModel.this.selected;
        }

        @Override
        protected List<Pack> getOtherList() {
            return PackSelectionModel.this.unselected;
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
    class UnselectedPackEntry
    extends EntryBase {
        public UnselectedPackEntry(Pack pack) {
            super(pack);
        }

        @Override
        protected List<Pack> getSelfList() {
            return PackSelectionModel.this.unselected;
        }

        @Override
        protected List<Pack> getOtherList() {
            return PackSelectionModel.this.selected;
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
    abstract class EntryBase
    implements Entry {
        private final Pack pack;

        public EntryBase(Pack pack) {
            this.pack = pack;
        }

        protected abstract List<Pack> getSelfList();

        protected abstract List<Pack> getOtherList();

        @Override
        public ResourceLocation getIconTexture() {
            return PackSelectionModel.this.iconGetter.apply(this.pack);
        }

        @Override
        public PackCompatibility getCompatibility() {
            return this.pack.getCompatibility();
        }

        @Override
        public Component getTitle() {
            return this.pack.getTitle();
        }

        @Override
        public Component getDescription() {
            return this.pack.getDescription();
        }

        @Override
        public PackSource getPackSource() {
            return this.pack.getPackSource();
        }

        @Override
        public boolean isFixedPosition() {
            return this.pack.isFixedPosition();
        }

        @Override
        public boolean isRequired() {
            return this.pack.isRequired();
        }

        protected void toggleSelection() {
            this.getSelfList().remove(this.pack);
            this.pack.getDefaultPosition().insert(this.getOtherList(), this.pack, Function.identity(), true);
            PackSelectionModel.this.onListChanged.run();
        }

        protected void move(int i) {
            List<Pack> list = this.getSelfList();
            int j = list.indexOf(this.pack);
            list.remove(j);
            list.add(j + i, this.pack);
            PackSelectionModel.this.onListChanged.run();
        }

        @Override
        public boolean canMoveUp() {
            List<Pack> list = this.getSelfList();
            int i = list.indexOf(this.pack);
            return i > 0 && !list.get(i - 1).isFixedPosition();
        }

        @Override
        public void moveUp() {
            this.move(-1);
        }

        @Override
        public boolean canMoveDown() {
            List<Pack> list = this.getSelfList();
            int i = list.indexOf(this.pack);
            return i >= 0 && i < list.size() - 1 && !list.get(i + 1).isFixedPosition();
        }

        @Override
        public void moveDown() {
            this.move(1);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Entry {
        public ResourceLocation getIconTexture();

        public PackCompatibility getCompatibility();

        public Component getTitle();

        public Component getDescription();

        public PackSource getPackSource();

        default public Component getExtendedDescription() {
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

