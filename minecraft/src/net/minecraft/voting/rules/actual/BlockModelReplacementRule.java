package net.minecraft.voting.rules.actual;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.MapRule;
import net.minecraft.voting.rules.ResourceKeyReplacementRule;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockModelReplacementRule extends ResourceKeyReplacementRule<Block> {
	private final Map<ResourceKey<Block>, BlockModelReplacementRule.Replacements> blockStateReplacements = new HashMap();
	private boolean isDirty;

	public BlockModelReplacementRule() {
		super(Registries.BLOCK);
	}

	protected Component description(ResourceKey<Block> resourceKey, ResourceKey<Block> resourceKey2) {
		Component component = BuiltInRegistries.BLOCK.get(resourceKey.location()).getName();
		Component component2 = BuiltInRegistries.BLOCK.get(resourceKey2.location()).getName();
		return Component.translatable("rule.replace_block_model", component, component2);
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		Registry<Block> registry = minecraftServer.registryAccess().registryOrThrow(Registries.BLOCK);
		Set<BlockModelReplacementRule.PotentialEntry> set = new HashSet();

		for (int j = 0; j < 30; j++) {
			Holder.Reference<Block> reference = (Holder.Reference<Block>)registry.getRandom(randomSource).orElse(null);
			Holder.Reference<Block> reference2 = (Holder.Reference<Block>)registry.getRandom(randomSource).orElse(null);
			if (reference != null
				&& !reference.equals(reference2)
				&& reference2 != null
				&& reference.value().defaultBlockState().getRenderShape() == RenderShape.MODEL
				&& reference2.value().defaultBlockState().getRenderShape() == RenderShape.MODEL
				&& canReplace(reference.value(), reference2.value())) {
				set.add(new BlockModelReplacementRule.PotentialEntry(reference, reference2));
			}
		}

		List<BlockModelReplacementRule.PotentialEntry> list = new ArrayList(set);
		list.sort(Comparator.comparingInt(potentialEntry -> -potentialEntry.target.value().getStateDefinition().getProperties().size()));
		return list.stream().limit((long)i).map(potentialEntry -> new MapRule.MapRuleChange(potentialEntry.source().key(), potentialEntry.target().key()));
	}

	@Override
	protected void remove(ResourceKey<Block> resourceKey) {
		super.remove(resourceKey);
		if (this.blockStateReplacements.remove(resourceKey) != null) {
			this.isDirty = true;
		}
	}

	public boolean getAndClearDirtyStatus() {
		boolean bl = this.isDirty;
		this.isDirty = false;
		return bl;
	}

	@Override
	protected void set(ResourceKey<Block> resourceKey, ResourceKey<Block> resourceKey2) {
		super.set(resourceKey, resourceKey2);
		Block block = BuiltInRegistries.BLOCK.get(resourceKey2);
		Block block2 = BuiltInRegistries.BLOCK.get(resourceKey);
		if (block != null && block2 != null) {
			Builder<BlockState, BlockState> builder = ImmutableMap.builder();
			replace(block2, block, builder::put);
			Map<BlockState, BlockState> map = builder.build();
			this.blockStateReplacements.put(resourceKey, new BlockModelReplacementRule.Replacements(map));
			this.isDirty = true;
		}
	}

	public BlockState getReplacement(BlockState blockState) {
		BlockModelReplacementRule.Replacements replacements = (BlockModelReplacementRule.Replacements)this.blockStateReplacements
			.get(blockState.getBlock().builtInRegistryHolder().key());
		return replacements != null ? replacements.getReplacedState(blockState) : blockState;
	}

	private static boolean areValuesSubset(Property<?> property, Property<?> property2) {
		Set<Object> set = Set.copyOf(property2.getPossibleValues());
		Set<Object> set2 = new HashSet(property.getPossibleValues());
		set2.removeAll(set);
		return set2.isEmpty();
	}

	public static boolean canReplace(Block block, Block block2) {
		StateDefinition<Block, BlockState> stateDefinition = block2.getStateDefinition();
		StateDefinition<Block, BlockState> stateDefinition2 = block.getStateDefinition();
		Set<Property<?>> set = new HashSet(stateDefinition.getProperties());
		if (set.isEmpty()) {
			return true;
		} else {
			for (Property<?> property : stateDefinition2.getProperties()) {
				Property<?> property2 = stateDefinition.getProperty(property.getName());
				if (property2 != null && areValuesSubset(property, property2)) {
					set.remove(property2);
				}
			}

			return set.isEmpty();
		}
	}

	public static void replace(Block block, Block block2, BiConsumer<BlockState, BlockState> biConsumer) {
		StateDefinition<Block, BlockState> stateDefinition = block2.getStateDefinition();
		StateDefinition<Block, BlockState> stateDefinition2 = block.getStateDefinition();
		Set<String> set = new HashSet();

		for (Property<?> property : stateDefinition2.getProperties()) {
			String string = property.getName();
			Property<?> property2 = stateDefinition.getProperty(string);
			if (property2 != null && areValuesSubset(property, property2)) {
				set.add(string);
			}
		}

		BlockState blockState = block2.defaultBlockState();

		for (BlockState blockState2 : stateDefinition2.getPossibleStates()) {
			BlockState blockState3 = blockState;

			for (String string2 : set) {
				blockState3 = setProperty(blockState2, stateDefinition2, blockState3, stateDefinition, string2);
			}

			biConsumer.accept(blockState2, blockState3);
		}
	}

	private static <T extends Comparable<T>> BlockState setProperty(
		BlockState blockState,
		StateDefinition<Block, BlockState> stateDefinition,
		BlockState blockState2,
		StateDefinition<Block, BlockState> stateDefinition2,
		String string
	) {
		try {
			Property<T> property = (Property<T>)stateDefinition.getProperty(string);
			Property<T> property2 = (Property<T>)stateDefinition2.getProperty(string);
			T comparable = blockState.getValue(property);
			return blockState2.setValue(property2, comparable);
		} catch (Exception var8) {
			return blockState2;
		}
	}

	static record PotentialEntry(Holder.Reference<Block> source, Holder.Reference<Block> target) {
	}

	static record Replacements(Map<BlockState, BlockState> replacements) {
		public BlockState getReplacedState(BlockState blockState) {
			return (BlockState)this.replacements.getOrDefault(blockState, blockState);
		}
	}
}
