package net.minecraft.world.level.storage.loot.parameters;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LootContextParams {
	public static final LootContextParam<Entity> THIS_ENTITY = create("this_entity");
	public static final LootContextParam<Player> LAST_DAMAGE_PLAYER = create("last_damage_player");
	public static final LootContextParam<DamageSource> DAMAGE_SOURCE = create("damage_source");
	public static final LootContextParam<Entity> ATTACKING_ENTITY = create("attacking_entity");
	public static final LootContextParam<Entity> DIRECT_ATTACKING_ENTITY = create("direct_attacking_entity");
	public static final LootContextParam<Vec3> ORIGIN = create("origin");
	public static final LootContextParam<BlockState> BLOCK_STATE = create("block_state");
	public static final LootContextParam<BlockEntity> BLOCK_ENTITY = create("block_entity");
	public static final LootContextParam<ItemStack> TOOL = create("tool");
	public static final LootContextParam<Float> EXPLOSION_RADIUS = create("explosion_radius");
	public static final LootContextParam<Integer> ENCHANTMENT_LEVEL = create("enchantment_level");
	public static final LootContextParam<Boolean> ENCHANTMENT_ACTIVE = create("enchantment_active");

	private static <T> LootContextParam<T> create(String string) {
		return new LootContextParam<>(ResourceLocation.withDefaultNamespace(string));
	}
}
