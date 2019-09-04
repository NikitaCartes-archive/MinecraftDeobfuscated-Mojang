package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class UseOnContext {
	protected final Player player;
	protected final InteractionHand hand;
	protected final BlockHitResult hitResult;
	protected final Level level;
	protected final ItemStack itemStack;

	public UseOnContext(Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		this(player.level, player, interactionHand, player.getItemInHand(interactionHand), blockHitResult);
	}

	protected UseOnContext(Level level, @Nullable Player player, InteractionHand interactionHand, ItemStack itemStack, BlockHitResult blockHitResult) {
		this.player = player;
		this.hand = interactionHand;
		this.hitResult = blockHitResult;
		this.itemStack = itemStack;
		this.level = level;
	}

	public BlockPos getClickedPos() {
		return this.hitResult.getBlockPos();
	}

	public Direction getClickedFace() {
		return this.hitResult.getDirection();
	}

	public Vec3 getClickLocation() {
		return this.hitResult.getLocation();
	}

	public boolean isInside() {
		return this.hitResult.isInside();
	}

	public ItemStack getItemInHand() {
		return this.itemStack;
	}

	@Nullable
	public Player getPlayer() {
		return this.player;
	}

	public InteractionHand getHand() {
		return this.hand;
	}

	public Level getLevel() {
		return this.level;
	}

	public Direction getHorizontalDirection() {
		return this.player == null ? Direction.NORTH : this.player.getDirection();
	}

	public boolean isSecondaryUseActive() {
		return this.player != null && this.player.isSecondaryUseActive();
	}

	public float getRotation() {
		return this.player == null ? 0.0F : this.player.yRot;
	}
}
