package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class InfestedBlock extends Block {
	public static final MapCodec<InfestedBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("host").forGetter(InfestedBlock::getHostBlock), propertiesCodec())
				.apply(instance, InfestedBlock::new)
	);
	private final Block hostBlock;
	private static final Map<Block, Block> BLOCK_BY_HOST_BLOCK = Maps.<Block, Block>newIdentityHashMap();
	private static final Map<BlockState, BlockState> HOST_TO_INFESTED_STATES = Maps.<BlockState, BlockState>newIdentityHashMap();
	private static final Map<BlockState, BlockState> INFESTED_TO_HOST_STATES = Maps.<BlockState, BlockState>newIdentityHashMap();

	@Override
	public MapCodec<? extends InfestedBlock> codec() {
		return CODEC;
	}

	public InfestedBlock(Block block, BlockBehaviour.Properties properties) {
		super(properties.destroyTime(block.defaultDestroyTime() / 2.0F).explosionResistance(0.75F));
		this.hostBlock = block;
		BLOCK_BY_HOST_BLOCK.put(block, this);
	}

	public Block getHostBlock() {
		return this.hostBlock;
	}

	public static boolean isCompatibleHostBlock(BlockState blockState) {
		return BLOCK_BY_HOST_BLOCK.containsKey(blockState.getBlock());
	}

	private void spawnInfestation(ServerLevel serverLevel, BlockPos blockPos) {
		Silverfish silverfish = EntityType.SILVERFISH.create(serverLevel);
		if (silverfish != null) {
			silverfish.moveTo((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5, 0.0F, 0.0F);
			serverLevel.addFreshEntity(silverfish);
			silverfish.spawnAnim();
		}
	}

	@Override
	protected void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
		super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
		if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !EnchantmentHelper.hasTag(itemStack, EnchantmentTags.PREVENTS_INFESTED_SPAWNS)) {
			this.spawnInfestation(serverLevel, blockPos);
		}
	}

	public static BlockState infestedStateByHost(BlockState blockState) {
		return getNewStateWithProperties(HOST_TO_INFESTED_STATES, blockState, () -> ((Block)BLOCK_BY_HOST_BLOCK.get(blockState.getBlock())).defaultBlockState());
	}

	public BlockState hostStateByInfested(BlockState blockState) {
		return getNewStateWithProperties(INFESTED_TO_HOST_STATES, blockState, () -> this.getHostBlock().defaultBlockState());
	}

	private static BlockState getNewStateWithProperties(Map<BlockState, BlockState> map, BlockState blockState, Supplier<BlockState> supplier) {
		return (BlockState)map.computeIfAbsent(blockState, blockStatex -> {
			BlockState blockState2 = (BlockState)supplier.get();

			for (Property property : blockStatex.getProperties()) {
				blockState2 = blockState2.hasProperty(property) ? blockState2.setValue(property, blockStatex.getValue(property)) : blockState2;
			}

			return blockState2;
		});
	}
}
