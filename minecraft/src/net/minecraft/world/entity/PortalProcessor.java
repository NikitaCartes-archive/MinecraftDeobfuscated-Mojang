package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.portal.TeleportTransition;

public class PortalProcessor {
	private final Portal portal;
	private BlockPos entryPosition;
	private int portalTime;
	private boolean insidePortalThisTick;

	public PortalProcessor(Portal portal, BlockPos blockPos) {
		this.portal = portal;
		this.entryPosition = blockPos;
		this.insidePortalThisTick = true;
	}

	public boolean processPortalTeleportation(ServerLevel serverLevel, Entity entity, boolean bl) {
		if (!this.insidePortalThisTick) {
			this.decayTick();
			return false;
		} else {
			this.insidePortalThisTick = false;
			return bl && this.portalTime++ >= this.portal.getPortalTransitionTime(serverLevel, entity);
		}
	}

	@Nullable
	public TeleportTransition getPortalDestination(ServerLevel serverLevel, Entity entity) {
		return this.portal.getPortalDestination(serverLevel, entity, this.entryPosition);
	}

	public Portal.Transition getPortalLocalTransition() {
		return this.portal.getLocalTransition();
	}

	private void decayTick() {
		this.portalTime = Math.max(this.portalTime - 4, 0);
	}

	public boolean hasExpired() {
		return this.portalTime <= 0;
	}

	public BlockPos getEntryPosition() {
		return this.entryPosition;
	}

	public void updateEntryPosition(BlockPos blockPos) {
		this.entryPosition = blockPos;
	}

	public int getPortalTime() {
		return this.portalTime;
	}

	public boolean isInsidePortalThisTick() {
		return this.insidePortalThisTick;
	}

	public void setAsInsidePortalThisTick(boolean bl) {
		this.insidePortalThisTick = bl;
	}

	public boolean isSamePortal(Portal portal) {
		return this.portal == portal;
	}
}
