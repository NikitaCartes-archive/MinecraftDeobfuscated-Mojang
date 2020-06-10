package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collection;
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

@Environment(EnvType.CLIENT)
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
		this.selected = Lists.<T>newArrayList(packRepository.getSelectedPacks());
		Collections.reverse(this.selected);
		this.unselected = Lists.<T>newArrayList(packRepository.getAvailablePacks());
		this.unselected.removeAll(this.selected);
		this.output = consumer;
	}

	public Stream<PackSelectionModel.Entry> getUnselected() {
		return this.unselected.stream().map(pack -> new PackSelectionModel.UnselectedPackEntry(pack));
	}

	public Stream<PackSelectionModel.Entry> getSelected() {
		return this.selected.stream().map(pack -> new PackSelectionModel.SelectedPackEntry(pack));
	}

	public void commit() {
		this.repository.setSelected((Collection<String>)Lists.reverse(this.selected).stream().map(Pack::getId).collect(ImmutableList.toImmutableList()));
		this.output.accept(this.repository);
	}

	public void findNewPacks() {
		this.repository.reload();
		this.unselected.clear();
		this.unselected.addAll(this.repository.getAvailablePacks());
		this.unselected.removeAll(this.selected);
	}

	@Environment(EnvType.CLIENT)
	public interface Entry {
		void bindIcon(TextureManager textureManager);

		PackCompatibility getCompatibility();

		Component getTitle();

		Component getDescription();

		PackSource getPackSource();

		default FormattedText getExtendedDescription() {
			return this.getPackSource().decorate(this.getDescription());
		}

		boolean isFixedPosition();

		boolean isRequired();

		void select();

		void unselect();

		void moveUp();

		void moveDown();

		boolean isSelected();

		default boolean canSelect() {
			return !this.isSelected();
		}

		default boolean canUnselect() {
			return this.isSelected() && !this.isRequired();
		}

		boolean canMoveUp();

		boolean canMoveDown();
	}

	@Environment(EnvType.CLIENT)
	abstract class EntryBase implements PackSelectionModel.Entry {
		private final T pack;

		public EntryBase(T pack) {
			this.pack = pack;
		}

		protected abstract List<T> getSelfList();

		protected abstract List<T> getOtherList();

		@Override
		public void bindIcon(TextureManager textureManager) {
			PackSelectionModel.this.iconBinder.accept(this.pack, textureManager);
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
			List<T> list = this.getSelfList();
			int j = list.indexOf(this.pack);
			list.remove(j);
			list.add(j + i, this.pack);
			PackSelectionModel.this.onListChanged.run();
		}

		@Override
		public boolean canMoveUp() {
			List<T> list = this.getSelfList();
			int i = list.indexOf(this.pack);
			return i > 0 && !((Pack)list.get(i - 1)).isFixedPosition();
		}

		@Override
		public void moveUp() {
			this.move(-1);
		}

		@Override
		public boolean canMoveDown() {
			List<T> list = this.getSelfList();
			int i = list.indexOf(this.pack);
			return i >= 0 && i < list.size() - 1 && !((Pack)list.get(i + 1)).isFixedPosition();
		}

		@Override
		public void moveDown() {
			this.move(1);
		}
	}

	@Environment(EnvType.CLIENT)
	class SelectedPackEntry extends PackSelectionModel<T>.EntryBase {
		public SelectedPackEntry(T pack) {
			super(pack);
		}

		@Override
		protected List<T> getSelfList() {
			return PackSelectionModel.this.selected;
		}

		@Override
		protected List<T> getOtherList() {
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

	@Environment(EnvType.CLIENT)
	class UnselectedPackEntry extends PackSelectionModel<T>.EntryBase {
		public UnselectedPackEntry(T pack) {
			super(pack);
		}

		@Override
		protected List<T> getSelfList() {
			return PackSelectionModel.this.unselected;
		}

		@Override
		protected List<T> getOtherList() {
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
}
