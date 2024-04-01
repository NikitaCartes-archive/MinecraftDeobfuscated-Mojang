package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FletchingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FletchingBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
	public static final int INGREDIENT_SLOT = 0;
	public static final int OUTPUT_SLOT = 1;
	public static final int FLETCHING_SLOT = 2;
	public static final int TOTAL_SLOTS = 3;
	public static final int DATA_PROGRESS = 0;
	public static final int DATA_QUALITY = 1;
	public static final int DATA_SOURCE_IMPURITIES = 2;
	public static final int DATA_RESULT_IMPURITIES = 3;
	public static final int DATA_PROCESSS_TIME = 4;
	public static final int DATA_EXPLORED = 5;
	public static final int NUM_DATA_VALUES = 6;
	short progresss;
	public static final char START_CLARITY_CODE = 'a';
	public static final char END_CLARITY_CODE = 'j';
	public static final char START_IMPURITY_CODE = 'a';
	public static final char END_IMPURITY_CODE = 'p';
	public static final int MAX_PROCESSS_TIME = 200;
	char quality;
	char impurities;
	char nextLevelImpurities;
	boolean explored;
	short processsTime;
	private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
	private static final int[] SLOTS_FOR_UP = new int[]{0};
	private static final int[] SLOTS_FOR_DOWN = new int[]{1};
	protected final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int i) {
			return switch (i) {
				case 0 -> FletchingBlockEntity.this.progresss;
				case 1 -> FletchingBlockEntity.this.quality;
				case 2 -> FletchingBlockEntity.this.impurities;
				case 3 -> FletchingBlockEntity.this.nextLevelImpurities;
				case 4 -> FletchingBlockEntity.this.processsTime;
				case 5 -> FletchingBlockEntity.this.explored ? 1 : 0;
				default -> 0;
			};
		}

		@Override
		public void set(int i, int j) {
			if (i == 0) {
				FletchingBlockEntity.this.progresss = (short)j;
			}
		}

		@Override
		public int getCount() {
			return 6;
		}
	};

	public FletchingBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.FLETCHING, blockPos, blockState);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.fletching");
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> nonNullList) {
		this.items = nonNullList;
	}

	@Override
	protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return new FletchingMenu(i, inventory, this, this.dataAccess);
	}

	@Override
	public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.load(compoundTag, provider);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compoundTag, this.items, provider);
		this.quality = compoundTag.getString("quality").charAt(0);
		this.impurities = compoundTag.getString("impurities").charAt(0);
		this.nextLevelImpurities = compoundTag.getString("nextLevelImpurities").charAt(0);
		this.processsTime = compoundTag.getShort("processsTime");
		this.explored = compoundTag.getBoolean("explored");
		this.progresss = compoundTag.getShort("progresss");
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		compoundTag.putShort("progresss", this.progresss);
		compoundTag.putString("quality", String.valueOf(this.quality));
		compoundTag.putString("impurities", String.valueOf(this.impurities));
		compoundTag.putString("nextLevelImpurities", String.valueOf(this.nextLevelImpurities));
		compoundTag.putShort("processsTime", this.processsTime);
		compoundTag.putBoolean("explored", this.explored);
		ContainerHelper.saveAllItems(compoundTag, this.items, provider);
	}

	@Override
	public void applyComponents(DataComponentMap dataComponentMap) {
		super.applyComponents(dataComponentMap);
		FletchingBlockEntity.Fletching fletching = dataComponentMap.getOrDefault(DataComponents.FLETCHING, FletchingBlockEntity.Fletching.EMPTY);
		this.quality = fletching.quality();
		this.impurities = fletching.impurities();
		this.nextLevelImpurities = fletching.nextLevelImpurities();
		this.processsTime = fletching.processsTime();
		this.explored = fletching.explored();
	}

	@Override
	public void collectComponents(DataComponentMap.Builder builder) {
		super.collectComponents(builder);
		builder.set(
			DataComponents.FLETCHING, new FletchingBlockEntity.Fletching(this.quality, this.impurities, this.nextLevelImpurities, this.processsTime, this.explored)
		);
	}

	@Override
	public void removeComponentsFromTag(CompoundTag compoundTag) {
		super.removeComponentsFromTag(compoundTag);
		compoundTag.remove("quality");
		compoundTag.remove("impurities");
		compoundTag.remove("nextLevelImpurities");
		compoundTag.remove("processsTime");
		compoundTag.remove("explored");
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public int[] getSlotsForFace(Direction direction) {
		return direction == Direction.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_UP;
	}

	@Override
	public boolean canPlaceItem(int i, ItemStack itemStack) {
		if (i == 1) {
			return false;
		} else if (i == 0) {
			return this.processsTime == 0 ? false : canAcceptItem(itemStack, this.quality, this.impurities);
		} else {
			return i != 2 ? true : this.progresss == 0 && itemStack.is(Items.FEATHER);
		}
	}

	public static boolean canAcceptItem(ItemStack itemStack, char c, char d) {
		if (!itemStack.is(Items.TOXIC_RESIN)) {
			return false;
		} else {
			FletchingBlockEntity.Resin resin = itemStack.getComponents().get(DataComponents.RESIN);
			if (resin == null) {
				throw new IllegalStateException("Resin item without resin quality");
			} else {
				return c == resin.quality() && resin.impurities() == d;
			}
		}
	}

	public static ItemStack createOutput(char c, char d) {
		if (c > 'j') {
			return new ItemStack(Items.AMBER_GEM);
		} else {
			ItemStack itemStack = new ItemStack(Items.TOXIC_RESIN);
			itemStack.set(DataComponents.RESIN, new FletchingBlockEntity.Resin(c, d));
			return itemStack;
		}
	}

	@Override
	public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
		return this.canPlaceItem(i, itemStack);
	}

	@Override
	public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
		return i != 0;
	}

	public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, FletchingBlockEntity fletchingBlockEntity) {
		if (fletchingBlockEntity.processsTime == 0) {
			fletchingBlockEntity.processsTime = (short)level.random.nextInt(10, 200);
			int i = 10;
			fletchingBlockEntity.quality = (char)(97 + level.random.nextInt(10));
			fletchingBlockEntity.impurities = FletchingBlockEntity.Resin.getRandomImpurities(level.getRandom());
			fletchingBlockEntity.nextLevelImpurities = FletchingBlockEntity.Resin.getRandomImpurities(level.getRandom());
			fletchingBlockEntity.explored = false;
		}

		ItemStack itemStack = fletchingBlockEntity.items.get(1);
		if (itemStack.isEmpty() || itemStack.getCount() != itemStack.getMaxStackSize()) {
			if (fletchingBlockEntity.progresss > 0) {
				fletchingBlockEntity.progresss--;
				if (fletchingBlockEntity.progresss <= 0) {
					ItemStack itemStack2 = createOutput((char)(fletchingBlockEntity.quality + 1), fletchingBlockEntity.nextLevelImpurities);
					if (!itemStack.isEmpty()) {
						itemStack2.setCount(itemStack.getCount() + 1);
					}

					fletchingBlockEntity.items.set(2, Items.FEATHER.getDefaultInstance());
					fletchingBlockEntity.items.set(1, itemStack2);
					fletchingBlockEntity.explored = true;
					setChanged(level, blockPos, blockState);
				}
			}

			ItemStack itemStack2 = fletchingBlockEntity.items.get(0);
			if (!itemStack2.isEmpty()) {
				if (fletchingBlockEntity.progresss <= 0 && fletchingBlockEntity.items.get(2).is(Items.FEATHER)) {
					fletchingBlockEntity.items.set(2, ItemStack.EMPTY);
					fletchingBlockEntity.progresss = fletchingBlockEntity.processsTime;
					itemStack2.shrink(1);
					setChanged(level, blockPos, blockState);
				}
			}
		}
	}

	public static final class Fletching implements TooltipProvider {
		public static final Codec<FletchingBlockEntity.Fletching> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.CHAR.fieldOf("quality").forGetter(FletchingBlockEntity.Fletching::quality),
						ExtraCodecs.CHAR.fieldOf("impurities").forGetter(FletchingBlockEntity.Fletching::impurities),
						ExtraCodecs.CHAR.fieldOf("next_level_impurities").forGetter(FletchingBlockEntity.Fletching::nextLevelImpurities),
						Codec.SHORT.fieldOf("processs_time").forGetter(FletchingBlockEntity.Fletching::processsTime),
						Codec.BOOL.optionalFieldOf("explored", Boolean.valueOf(false)).forGetter(FletchingBlockEntity.Fletching::explored)
					)
					.apply(instance, FletchingBlockEntity.Fletching::new)
		);
		public static final StreamCodec<ByteBuf, FletchingBlockEntity.Fletching> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.CHAR,
			FletchingBlockEntity.Fletching::quality,
			ByteBufCodecs.CHAR,
			FletchingBlockEntity.Fletching::impurities,
			ByteBufCodecs.CHAR,
			FletchingBlockEntity.Fletching::nextLevelImpurities,
			ByteBufCodecs.SHORT,
			FletchingBlockEntity.Fletching::processsTime,
			ByteBufCodecs.BOOL,
			FletchingBlockEntity.Fletching::explored,
			FletchingBlockEntity.Fletching::new
		);
		public static final FletchingBlockEntity.Fletching EMPTY = new FletchingBlockEntity.Fletching('a', 'a', 'a', (short)0, false);
		private final char quality;
		private final char impurities;
		private final char nextLevelImpurities;
		private final short processsTime;
		private final boolean explored;

		public Fletching(char c, char d, char e, short s, boolean bl) {
			this.quality = c;
			this.impurities = d;
			this.nextLevelImpurities = e;
			this.processsTime = s;
			this.explored = bl;
		}

		public char quality() {
			return this.quality;
		}

		public char impurities() {
			return this.impurities;
		}

		public char nextLevelImpurities() {
			return this.nextLevelImpurities;
		}

		public short processsTime() {
			return this.processsTime;
		}

		public boolean explored() {
			return this.explored;
		}

		public boolean equals(Object object) {
			if (object == this) {
				return true;
			} else if (object != null && object.getClass() == this.getClass()) {
				FletchingBlockEntity.Fletching fletching = (FletchingBlockEntity.Fletching)object;
				return this.quality == fletching.quality
					&& this.impurities == fletching.impurities
					&& this.nextLevelImpurities == fletching.nextLevelImpurities
					&& this.processsTime == fletching.processsTime
					&& this.explored == fletching.explored;
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.quality, this.impurities, this.nextLevelImpurities, this.processsTime, this.explored});
		}

		public String toString() {
			return "Fletching[quality="
				+ this.quality
				+ ", impurities="
				+ this.impurities
				+ ", nextLevelImpurities="
				+ this.nextLevelImpurities
				+ ", processsTime="
				+ this.processsTime
				+ ", explored="
				+ this.explored
				+ "]";
		}

		@Override
		public void addToTooltip(Consumer<Component> consumer, TooltipFlag tooltipFlag) {
			consumer.accept(Component.translatable("block.minecraft.fletching_table.from"));
			consumer.accept(CommonComponents.space().append(FletchingBlockEntity.Resin.getQualityComponent(this.quality)).withStyle(ChatFormatting.GRAY));
			consumer.accept(CommonComponents.space().append(FletchingBlockEntity.Resin.getImpuritiesComponent(this.impurities)).withStyle(ChatFormatting.GRAY));
			consumer.accept(Component.translatable("block.minecraft.fletching_table.to"));
			consumer.accept(
				CommonComponents.space()
					.append(
						this.quality >= 'j'
							? Component.translatable("item.minecraft.amber_gem").withStyle(ChatFormatting.GOLD)
							: FletchingBlockEntity.Resin.getImpuritiesComponent(!this.explored ? "unknown" : this.nextLevelImpurities).withStyle(ChatFormatting.GRAY)
					)
			);
		}
	}

	public static final class Resin implements TooltipProvider {
		public static final Codec<FletchingBlockEntity.Resin> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.CHAR.fieldOf("quality").forGetter(FletchingBlockEntity.Resin::quality),
						ExtraCodecs.CHAR.fieldOf("impurities").forGetter(FletchingBlockEntity.Resin::impurities)
					)
					.apply(instance, FletchingBlockEntity.Resin::new)
		);
		public static final StreamCodec<ByteBuf, FletchingBlockEntity.Resin> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.CHAR, FletchingBlockEntity.Resin::quality, ByteBufCodecs.CHAR, FletchingBlockEntity.Resin::impurities, FletchingBlockEntity.Resin::new
		);
		public static final FletchingBlockEntity.Resin EMPTY = new FletchingBlockEntity.Resin('a', 'a');
		private final char quality;
		private final char impurities;

		public Resin(char c, char d) {
			this.quality = c;
			this.impurities = d;
		}

		@Override
		public void addToTooltip(Consumer<Component> consumer, TooltipFlag tooltipFlag) {
			consumer.accept(getQualityComponent(this.quality).withStyle(ChatFormatting.GRAY));
			consumer.accept(getImpuritiesComponent(this.impurities).withStyle(ChatFormatting.GRAY));
		}

		public static MutableComponent getQualityComponent(char c) {
			return Component.translatable("item.resin.quality", Component.translatable("item.resin.clarity.adjective." + c));
		}

		public static MutableComponent getImpuritiesComponent(Object object) {
			return Component.translatable("item.resin.impurities", Component.translatable("item.resin.impurity.adjective." + object));
		}

		public char quality() {
			return this.quality;
		}

		public char impurities() {
			return this.impurities;
		}

		public static char getRandomImpurities(RandomSource randomSource) {
			int i = 16;
			return (char)(97 + randomSource.nextInt(16));
		}

		public boolean equals(Object object) {
			return !(object instanceof FletchingBlockEntity.Resin resin) ? false : this.quality == resin.quality && this.impurities == resin.impurities;
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.quality, this.impurities});
		}
	}
}
