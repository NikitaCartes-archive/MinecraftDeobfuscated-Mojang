package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class AdventureModePredicate {
	private static final Codec<AdventureModePredicate> SIMPLE_CODEC = BlockPredicate.CODEC
		.flatComapMap(blockPredicate -> new AdventureModePredicate(List.of(blockPredicate), true), adventureModePredicate -> DataResult.error(() -> "Cannot encode"));
	private static final Codec<AdventureModePredicate> FULL_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.nonEmptyList(BlockPredicate.CODEC.listOf()).fieldOf("predicates").forGetter(adventureModePredicate -> adventureModePredicate.predicates),
					ExtraCodecs.strictOptionalField(Codec.BOOL, "show_in_tooltip", true).forGetter(AdventureModePredicate::showInTooltip)
				)
				.apply(instance, AdventureModePredicate::new)
	);
	public static final Codec<AdventureModePredicate> CODEC = ExtraCodecs.withAlternative(FULL_CODEC, SIMPLE_CODEC);
	public static final StreamCodec<RegistryFriendlyByteBuf, AdventureModePredicate> STREAM_CODEC = StreamCodec.composite(
		BlockPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()),
		adventureModePredicate -> adventureModePredicate.predicates,
		ByteBufCodecs.BOOL,
		AdventureModePredicate::showInTooltip,
		AdventureModePredicate::new
	);
	public static final Component CAN_BREAK_HEADER = Component.translatable("item.canBreak").withStyle(ChatFormatting.GRAY);
	public static final Component CAN_PLACE_HEADER = Component.translatable("item.canPlace").withStyle(ChatFormatting.GRAY);
	private static final Component UNKNOWN_USE = Component.translatable("item.canUse.unknown").withStyle(ChatFormatting.GRAY);
	private final List<BlockPredicate> predicates;
	private final boolean showInTooltip;
	private final List<Component> tooltip;
	@Nullable
	private BlockInWorld lastCheckedBlock;
	private boolean lastResult;
	private boolean checksBlockEntity;

	public AdventureModePredicate(List<BlockPredicate> list, boolean bl) {
		this.predicates = list;
		this.showInTooltip = bl;
		this.tooltip = computeTooltip(list);
	}

	private static boolean areSameBlocks(BlockInWorld blockInWorld, @Nullable BlockInWorld blockInWorld2, boolean bl) {
		if (blockInWorld2 == null || blockInWorld.getState() != blockInWorld2.getState()) {
			return false;
		} else if (!bl) {
			return true;
		} else if (blockInWorld.getEntity() == null && blockInWorld2.getEntity() == null) {
			return true;
		} else if (blockInWorld.getEntity() != null && blockInWorld2.getEntity() != null) {
			RegistryAccess registryAccess = blockInWorld.getLevel().registryAccess();
			return Objects.equals(blockInWorld.getEntity().saveWithId(registryAccess), blockInWorld2.getEntity().saveWithId(registryAccess));
		} else {
			return false;
		}
	}

	public boolean test(BlockInWorld blockInWorld) {
		if (areSameBlocks(blockInWorld, this.lastCheckedBlock, this.checksBlockEntity)) {
			return this.lastResult;
		} else {
			this.lastCheckedBlock = blockInWorld;
			this.checksBlockEntity = false;

			for (BlockPredicate blockPredicate : this.predicates) {
				if (blockPredicate.matches(blockInWorld)) {
					this.checksBlockEntity = this.checksBlockEntity | blockPredicate.requiresNbt();
					this.lastResult = true;
					return true;
				}
			}

			this.lastResult = false;
			return false;
		}
	}

	public void addToTooltip(Consumer<Component> consumer) {
		this.tooltip.forEach(consumer);
	}

	private static List<Component> computeTooltip(List<BlockPredicate> list) {
		for (BlockPredicate blockPredicate : list) {
			if (blockPredicate.blocks().isEmpty()) {
				return List.of(UNKNOWN_USE);
			}
		}

		return list.stream()
			.flatMap(blockPredicatex -> ((HolderSet)blockPredicatex.blocks().orElseThrow()).stream())
			.distinct()
			.map(holder -> ((Block)holder.value()).getName().withStyle(ChatFormatting.DARK_GRAY))
			.toList();
	}

	public boolean showInTooltip() {
		return this.showInTooltip;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof AdventureModePredicate adventureModePredicate)
				? false
				: this.predicates.equals(adventureModePredicate.predicates) && this.showInTooltip == adventureModePredicate.showInTooltip;
		}
	}

	public int hashCode() {
		return this.predicates.hashCode() * 31 + (this.showInTooltip ? 1 : 0);
	}

	public String toString() {
		return "AdventureModePredicate{predicates=" + this.predicates + ", showInTooltip=" + this.showInTooltip + "}";
	}
}
