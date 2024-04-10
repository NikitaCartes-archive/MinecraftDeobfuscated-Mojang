package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;

@Environment(EnvType.CLIENT)
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
		this.selected = Lists.<Pack>newArrayList(packRepository.getSelectedPacks());
		Collections.reverse(this.selected);
		this.unselected = Lists.<Pack>newArrayList(packRepository.getAvailablePacks());
		this.unselected.removeAll(this.selected);
		this.output = consumer;
	}

	public Stream<PackSelectionModel.Entry> getUnselected() {
		return this.unselected.stream().map(pack -> new PackSelectionModel.UnselectedPackEntry(pack));
	}

	public Stream<PackSelectionModel.Entry> getSelected() {
		return this.selected.stream().map(pack -> new PackSelectionModel.SelectedPackEntry(pack));
	}

	void updateRepoSelectedList() {
		this.repository.setSelected((Collection<String>)Lists.reverse(this.selected).stream().map(Pack::getId).collect(ImmutableList.toImmutableList()));
	}

	public void commit() {
		this.updateRepoSelectedList();
		this.output.accept(this.repository);
	}

	public void findNewPacks() {
		this.repository.reload();
		this.selected.retainAll(this.repository.getAvailablePacks());
		this.unselected.clear();
		this.unselected.addAll(this.repository.getAvailablePacks());
		this.unselected.removeAll(this.selected);
	}

	@Environment(EnvType.CLIENT)
	public interface Entry {
		ResourceLocation getIconTexture();

		PackCompatibility getCompatibility();

		String getId();

		Component getTitle();

		Component getDescription();

		PackSource getPackSource();

		default Component getExtendedDescription() {
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
		private final Pack pack;

		public EntryBase(final Pack pack) {
			this.pack = pack;
		}

		protected abstract List<Pack> getSelfList();

		protected abstract List<Pack> getOtherList();

		@Override
		public ResourceLocation getIconTexture() {
			return (ResourceLocation)PackSelectionModel.this.iconGetter.apply(this.pack);
		}

		@Override
		public PackCompatibility getCompatibility() {
			return this.pack.getCompatibility();
		}

		@Override
		public String getId() {
			return this.pack.getId();
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
			this.pack.getDefaultPosition().insert(this.getOtherList(), this.pack, Pack::selectionConfig, true);
			PackSelectionModel.this.onListChanged.run();
			PackSelectionModel.this.updateRepoSelectedList();
			this.updateHighContrastOptionInstance();
		}

		private void updateHighContrastOptionInstance() {
			if (this.pack.getId().equals("high_contrast")) {
				OptionInstance<Boolean> optionInstance = Minecraft.getInstance().options.highContrast();
				optionInstance.set(!optionInstance.get());
			}
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
			return i > 0 && !((Pack)list.get(i - 1)).isFixedPosition();
		}

		@Override
		public void moveUp() {
			this.move(-1);
		}

		@Override
		public boolean canMoveDown() {
			List<Pack> list = this.getSelfList();
			int i = list.indexOf(this.pack);
			return i >= 0 && i < list.size() - 1 && !((Pack)list.get(i + 1)).isFixedPosition();
		}

		@Override
		public void moveDown() {
			this.move(1);
		}
	}

	@Environment(EnvType.CLIENT)
	class SelectedPackEntry extends PackSelectionModel.EntryBase {
		public SelectedPackEntry(final Pack pack) {
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

	@Environment(EnvType.CLIENT)
	class UnselectedPackEntry extends PackSelectionModel.EntryBase {
		public UnselectedPackEntry(final Pack pack) {
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
}
