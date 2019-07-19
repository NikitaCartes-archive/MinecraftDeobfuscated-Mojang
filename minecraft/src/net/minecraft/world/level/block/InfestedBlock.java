package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class InfestedBlock extends Block {
	private final Block hostBlock;
	private static final Map<Block, Block> BLOCK_BY_HOST_BLOCK = Maps.<Block, Block>newIdentityHashMap();

	public InfestedBlock(Block block, Block.Properties properties) {
		super(properties);
		this.hostBlock = block;
		BLOCK_BY_HOST_BLOCK.put(block, this);
	}

	public Block getHostBlock() {
		return this.hostBlock;
	}

	public static boolean isCompatibleHostBlock(BlockState blockState) {
		return BLOCK_BY_HOST_BLOCK.containsKey(blockState.getBlock());
	}

	@Override
	public void spawnAfterBreak(BlockState blockState, Level level, BlockPos blockPos, ItemStack itemStack) {
		super.spawnAfterBreak(blockState, level, blockPos, itemStack);
		if (!level.isClientSide
			&& level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)
			&& EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
			Silverfish silverfish = EntityType.SILVERFISH.create(level);
			silverfish.moveTo((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5, 0.0F, 0.0F);
			level.addFreshEntity(silverfish);
			silverfish.spawnAnim();
		}
	}

	public static BlockState stateByHostBlock(Block block) {
		return ((Block)BLOCK_BY_HOST_BLOCK.get(block)).defaultBlockState();
	}
}
