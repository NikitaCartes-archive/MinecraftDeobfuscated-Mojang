package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class HoeItem extends TieredItem {
	private final float attackSpeed;
	protected static final Map<Block, BlockState> TILLABLES = Maps.<Block, BlockState>newHashMap(
		ImmutableMap.of(
			Blocks.GRASS_BLOCK,
			Blocks.FARMLAND.defaultBlockState(),
			Blocks.GRASS_PATH,
			Blocks.FARMLAND.defaultBlockState(),
			Blocks.DIRT,
			Blocks.FARMLAND.defaultBlockState(),
			Blocks.COARSE_DIRT,
			Blocks.DIRT.defaultBlockState()
		)
	);

	public HoeItem(Tier tier, float f, Item.Properties properties) {
		super(tier, properties);
		this.attackSpeed = f;
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		if (useOnContext.getClickedFace() != Direction.DOWN && level.getBlockState(blockPos.above()).isAir()) {
			BlockState blockState = (BlockState)TILLABLES.get(level.getBlockState(blockPos).getBlock());
			if (blockState != null) {
				Player player = useOnContext.getPlayer();
				level.playSound(player, blockPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				if (!level.isClientSide) {
					level.setBlock(blockPos, blockState, 11);
					if (player != null) {
						useOnContext.getItemInHand().hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(useOnContext.getHand()));
					}
				}

				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	@Override
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		itemStack.hurtAndBreak(1, livingEntity2, livingEntityx -> livingEntityx.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		return true;
	}

	@Override
	public Multimap<String, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getDefaultAttributeModifiers(equipmentSlot);
		if (equipmentSlot == EquipmentSlot.MAINHAND) {
			multimap.put(
				SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
				new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 0.0, AttributeModifier.Operation.ADDITION)
			);
			multimap.put(
				SharedMonsterAttributes.ATTACK_SPEED.getName(),
				new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double)this.attackSpeed, AttributeModifier.Operation.ADDITION)
			);
		}

		return multimap;
	}
}
