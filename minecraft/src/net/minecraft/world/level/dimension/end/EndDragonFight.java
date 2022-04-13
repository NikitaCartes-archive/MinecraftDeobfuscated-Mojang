package net.minecraft.world.level.dimension.end;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class EndDragonFight {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_TICKS_BEFORE_DRAGON_RESPAWN = 1200;
	private static final int TIME_BETWEEN_CRYSTAL_SCANS = 100;
	private static final int TIME_BETWEEN_PLAYER_SCANS = 20;
	private static final int ARENA_SIZE_CHUNKS = 8;
	public static final int ARENA_TICKET_LEVEL = 9;
	private static final int GATEWAY_COUNT = 20;
	private static final int GATEWAY_DISTANCE = 96;
	public static final int DRAGON_SPAWN_Y = 128;
	private static final Predicate<Entity> VALID_PLAYER = EntitySelector.ENTITY_STILL_ALIVE.and(EntitySelector.withinDistance(0.0, 128.0, 0.0, 192.0));
	private final ServerBossEvent dragonEvent = (ServerBossEvent)new ServerBossEvent(
			new TranslatableComponent("entity.minecraft.ender_dragon"), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS
		)
		.setPlayBossMusic(true)
		.setCreateWorldFog(true);
	private final ServerLevel level;
	private final ObjectArrayList<Integer> gateways = new ObjectArrayList<>();
	private final BlockPattern exitPortalPattern;
	private int ticksSinceDragonSeen;
	private int crystalsAlive;
	private int ticksSinceCrystalsScanned;
	private int ticksSinceLastPlayerScan;
	private boolean dragonKilled;
	private boolean previouslyKilled;
	@Nullable
	private UUID dragonUUID;
	private boolean needsStateScanning = true;
	@Nullable
	private BlockPos portalLocation;
	@Nullable
	private DragonRespawnAnimation respawnStage;
	private int respawnTime;
	@Nullable
	private List<EndCrystal> respawnCrystals;

	public EndDragonFight(ServerLevel serverLevel, long l, CompoundTag compoundTag) {
		this.level = serverLevel;
		if (compoundTag.contains("NeedsStateScanning")) {
			this.needsStateScanning = compoundTag.getBoolean("NeedsStateScanning");
		}

		if (compoundTag.contains("DragonKilled", 99)) {
			if (compoundTag.hasUUID("Dragon")) {
				this.dragonUUID = compoundTag.getUUID("Dragon");
			}

			this.dragonKilled = compoundTag.getBoolean("DragonKilled");
			this.previouslyKilled = compoundTag.getBoolean("PreviouslyKilled");
			if (compoundTag.getBoolean("IsRespawning")) {
				this.respawnStage = DragonRespawnAnimation.START;
			}

			if (compoundTag.contains("ExitPortalLocation", 10)) {
				this.portalLocation = NbtUtils.readBlockPos(compoundTag.getCompound("ExitPortalLocation"));
			}
		} else {
			this.dragonKilled = true;
			this.previouslyKilled = true;
		}

		if (compoundTag.contains("Gateways", 9)) {
			ListTag listTag = compoundTag.getList("Gateways", 3);

			for (int i = 0; i < listTag.size(); i++) {
				this.gateways.add(listTag.getInt(i));
			}
		} else {
			this.gateways.addAll(ContiguousSet.create(Range.closedOpen(0, 20), DiscreteDomain.integers()));
			Util.shuffle(this.gateways, RandomSource.create(l));
		}

		this.exitPortalPattern = BlockPatternBuilder.start()
			.aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
			.aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
			.aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ")
			.aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ")
			.aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ")
			.where('#', BlockInWorld.hasState(BlockPredicate.forBlock(Blocks.BEDROCK)))
			.build();
	}

	public CompoundTag saveData() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putBoolean("NeedsStateScanning", this.needsStateScanning);
		if (this.dragonUUID != null) {
			compoundTag.putUUID("Dragon", this.dragonUUID);
		}

		compoundTag.putBoolean("DragonKilled", this.dragonKilled);
		compoundTag.putBoolean("PreviouslyKilled", this.previouslyKilled);
		if (this.portalLocation != null) {
			compoundTag.put("ExitPortalLocation", NbtUtils.writeBlockPos(this.portalLocation));
		}

		ListTag listTag = new ListTag();

		for (int i : this.gateways) {
			listTag.add(IntTag.valueOf(i));
		}

		compoundTag.put("Gateways", listTag);
		return compoundTag;
	}

	public void tick() {
		this.dragonEvent.setVisible(!this.dragonKilled);
		if (++this.ticksSinceLastPlayerScan >= 20) {
			this.updatePlayers();
			this.ticksSinceLastPlayerScan = 0;
		}

		if (!this.dragonEvent.getPlayers().isEmpty()) {
			this.level.getChunkSource().addRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
			boolean bl = this.isArenaLoaded();
			if (this.needsStateScanning && bl) {
				this.scanState();
				this.needsStateScanning = false;
			}

			if (this.respawnStage != null) {
				if (this.respawnCrystals == null && bl) {
					this.respawnStage = null;
					this.tryRespawn();
				}

				this.respawnStage.tick(this.level, this, this.respawnCrystals, this.respawnTime++, this.portalLocation);
			}

			if (!this.dragonKilled) {
				if ((this.dragonUUID == null || ++this.ticksSinceDragonSeen >= 1200) && bl) {
					this.findOrCreateDragon();
					this.ticksSinceDragonSeen = 0;
				}

				if (++this.ticksSinceCrystalsScanned >= 100 && bl) {
					this.updateCrystalCount();
					this.ticksSinceCrystalsScanned = 0;
				}
			}
		} else {
			this.level.getChunkSource().removeRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
		}
	}

	private void scanState() {
		LOGGER.info("Scanning for legacy world dragon fight...");
		boolean bl = this.hasActiveExitPortal();
		if (bl) {
			LOGGER.info("Found that the dragon has been killed in this world already.");
			this.previouslyKilled = true;
		} else {
			LOGGER.info("Found that the dragon has not yet been killed in this world.");
			this.previouslyKilled = false;
			if (this.findExitPortal() == null) {
				this.spawnExitPortal(false);
			}
		}

		List<? extends EnderDragon> list = this.level.getDragons();
		if (list.isEmpty()) {
			this.dragonKilled = true;
		} else {
			EnderDragon enderDragon = (EnderDragon)list.get(0);
			this.dragonUUID = enderDragon.getUUID();
			LOGGER.info("Found that there's a dragon still alive ({})", enderDragon);
			this.dragonKilled = false;
			if (!bl) {
				LOGGER.info("But we didn't have a portal, let's remove it.");
				enderDragon.discard();
				this.dragonUUID = null;
			}
		}

		if (!this.previouslyKilled && this.dragonKilled) {
			this.dragonKilled = false;
		}
	}

	private void findOrCreateDragon() {
		List<? extends EnderDragon> list = this.level.getDragons();
		if (list.isEmpty()) {
			LOGGER.debug("Haven't seen the dragon, respawning it");
			this.createNewDragon();
		} else {
			LOGGER.debug("Haven't seen our dragon, but found another one to use.");
			this.dragonUUID = ((EnderDragon)list.get(0)).getUUID();
		}
	}

	protected void setRespawnStage(DragonRespawnAnimation dragonRespawnAnimation) {
		if (this.respawnStage == null) {
			throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");
		} else {
			this.respawnTime = 0;
			if (dragonRespawnAnimation == DragonRespawnAnimation.END) {
				this.respawnStage = null;
				this.dragonKilled = false;
				EnderDragon enderDragon = this.createNewDragon();

				for (ServerPlayer serverPlayer : this.dragonEvent.getPlayers()) {
					CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, enderDragon);
				}
			} else {
				this.respawnStage = dragonRespawnAnimation;
			}
		}
	}

	private boolean hasActiveExitPortal() {
		for (int i = -8; i <= 8; i++) {
			for (int j = -8; j <= 8; j++) {
				LevelChunk levelChunk = this.level.getChunk(i, j);

				for (BlockEntity blockEntity : levelChunk.getBlockEntities().values()) {
					if (blockEntity instanceof TheEndPortalBlockEntity) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Nullable
	private BlockPattern.BlockPatternMatch findExitPortal() {
		for (int i = -8; i <= 8; i++) {
			for (int j = -8; j <= 8; j++) {
				LevelChunk levelChunk = this.level.getChunk(i, j);

				for (BlockEntity blockEntity : levelChunk.getBlockEntities().values()) {
					if (blockEntity instanceof TheEndPortalBlockEntity) {
						BlockPattern.BlockPatternMatch blockPatternMatch = this.exitPortalPattern.find(this.level, blockEntity.getBlockPos());
						if (blockPatternMatch != null) {
							BlockPos blockPos = blockPatternMatch.getBlock(3, 3, 3).getPos();
							if (this.portalLocation == null) {
								this.portalLocation = blockPos;
							}

							return blockPatternMatch;
						}
					}
				}
			}
		}

		int i = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION).getY();

		for (int j = i; j >= this.level.getMinBuildHeight(); j--) {
			BlockPattern.BlockPatternMatch blockPatternMatch2 = this.exitPortalPattern
				.find(this.level, new BlockPos(EndPodiumFeature.END_PODIUM_LOCATION.getX(), j, EndPodiumFeature.END_PODIUM_LOCATION.getZ()));
			if (blockPatternMatch2 != null) {
				if (this.portalLocation == null) {
					this.portalLocation = blockPatternMatch2.getBlock(3, 3, 3).getPos();
				}

				return blockPatternMatch2;
			}
		}

		return null;
	}

	private boolean isArenaLoaded() {
		for (int i = -8; i <= 8; i++) {
			for (int j = 8; j <= 8; j++) {
				ChunkAccess chunkAccess = this.level.getChunk(i, j, ChunkStatus.FULL, false);
				if (!(chunkAccess instanceof LevelChunk)) {
					return false;
				}

				ChunkHolder.FullChunkStatus fullChunkStatus = ((LevelChunk)chunkAccess).getFullStatus();
				if (!fullChunkStatus.isOrAfter(ChunkHolder.FullChunkStatus.TICKING)) {
					return false;
				}
			}
		}

		return true;
	}

	private void updatePlayers() {
		Set<ServerPlayer> set = Sets.<ServerPlayer>newHashSet();

		for (ServerPlayer serverPlayer : this.level.getPlayers(VALID_PLAYER)) {
			this.dragonEvent.addPlayer(serverPlayer);
			set.add(serverPlayer);
		}

		Set<ServerPlayer> set2 = Sets.<ServerPlayer>newHashSet(this.dragonEvent.getPlayers());
		set2.removeAll(set);

		for (ServerPlayer serverPlayer2 : set2) {
			this.dragonEvent.removePlayer(serverPlayer2);
		}
	}

	private void updateCrystalCount() {
		this.ticksSinceCrystalsScanned = 0;
		this.crystalsAlive = 0;

		for (SpikeFeature.EndSpike endSpike : SpikeFeature.getSpikesForLevel(this.level)) {
			this.crystalsAlive = this.crystalsAlive + this.level.getEntitiesOfClass(EndCrystal.class, endSpike.getTopBoundingBox()).size();
		}

		LOGGER.debug("Found {} end crystals still alive", this.crystalsAlive);
	}

	public void setDragonKilled(EnderDragon enderDragon) {
		if (enderDragon.getUUID().equals(this.dragonUUID)) {
			this.dragonEvent.setProgress(0.0F);
			this.dragonEvent.setVisible(false);
			this.spawnExitPortal(true);
			this.spawnNewGateway();
			if (!this.previouslyKilled) {
				this.level
					.setBlockAndUpdate(
						this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION), Blocks.DRAGON_EGG.defaultBlockState()
					);
			}

			this.previouslyKilled = true;
			this.dragonKilled = true;
		}
	}

	private void spawnNewGateway() {
		if (!this.gateways.isEmpty()) {
			int i = this.gateways.remove(this.gateways.size() - 1);
			int j = Mth.floor(96.0 * Math.cos(2.0 * (-Math.PI + (Math.PI / 20) * (double)i)));
			int k = Mth.floor(96.0 * Math.sin(2.0 * (-Math.PI + (Math.PI / 20) * (double)i)));
			this.spawnNewGateway(new BlockPos(j, 75, k));
		}
	}

	private void spawnNewGateway(BlockPos blockPos) {
		this.level.levelEvent(3000, blockPos, 0);
		EndFeatures.END_GATEWAY_DELAYED.value().place(this.level, this.level.getChunkSource().getGenerator(), RandomSource.create(), blockPos);
	}

	private void spawnExitPortal(boolean bl) {
		EndPodiumFeature endPodiumFeature = new EndPodiumFeature(bl);
		if (this.portalLocation == null) {
			this.portalLocation = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION).below();

			while (this.level.getBlockState(this.portalLocation).is(Blocks.BEDROCK) && this.portalLocation.getY() > this.level.getSeaLevel()) {
				this.portalLocation = this.portalLocation.below();
			}
		}

		endPodiumFeature.place(FeatureConfiguration.NONE, this.level, this.level.getChunkSource().getGenerator(), RandomSource.create(), this.portalLocation);
	}

	private EnderDragon createNewDragon() {
		this.level.getChunkAt(new BlockPos(0, 128, 0));
		EnderDragon enderDragon = EntityType.ENDER_DRAGON.create(this.level);
		enderDragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
		enderDragon.moveTo(0.0, 128.0, 0.0, this.level.random.nextFloat() * 360.0F, 0.0F);
		this.level.addFreshEntity(enderDragon);
		this.dragonUUID = enderDragon.getUUID();
		return enderDragon;
	}

	public void updateDragon(EnderDragon enderDragon) {
		if (enderDragon.getUUID().equals(this.dragonUUID)) {
			this.dragonEvent.setProgress(enderDragon.getHealth() / enderDragon.getMaxHealth());
			this.ticksSinceDragonSeen = 0;
			if (enderDragon.hasCustomName()) {
				this.dragonEvent.setName(enderDragon.getDisplayName());
			}
		}
	}

	public int getCrystalsAlive() {
		return this.crystalsAlive;
	}

	public void onCrystalDestroyed(EndCrystal endCrystal, DamageSource damageSource) {
		if (this.respawnStage != null && this.respawnCrystals.contains(endCrystal)) {
			LOGGER.debug("Aborting respawn sequence");
			this.respawnStage = null;
			this.respawnTime = 0;
			this.resetSpikeCrystals();
			this.spawnExitPortal(true);
		} else {
			this.updateCrystalCount();
			Entity entity = this.level.getEntity(this.dragonUUID);
			if (entity instanceof EnderDragon) {
				((EnderDragon)entity).onCrystalDestroyed(endCrystal, endCrystal.blockPosition(), damageSource);
			}
		}
	}

	public boolean hasPreviouslyKilledDragon() {
		return this.previouslyKilled;
	}

	public void tryRespawn() {
		if (this.dragonKilled && this.respawnStage == null) {
			BlockPos blockPos = this.portalLocation;
			if (blockPos == null) {
				LOGGER.debug("Tried to respawn, but need to find the portal first.");
				BlockPattern.BlockPatternMatch blockPatternMatch = this.findExitPortal();
				if (blockPatternMatch == null) {
					LOGGER.debug("Couldn't find a portal, so we made one.");
					this.spawnExitPortal(true);
				} else {
					LOGGER.debug("Found the exit portal & saved its location for next time.");
				}

				blockPos = this.portalLocation;
			}

			List<EndCrystal> list = Lists.<EndCrystal>newArrayList();
			BlockPos blockPos2 = blockPos.above(1);

			for (Direction direction : Direction.Plane.HORIZONTAL) {
				List<EndCrystal> list2 = this.level.getEntitiesOfClass(EndCrystal.class, new AABB(blockPos2.relative(direction, 2)));
				if (list2.isEmpty()) {
					return;
				}

				list.addAll(list2);
			}

			LOGGER.debug("Found all crystals, respawning dragon.");
			this.respawnDragon(list);
		}
	}

	private void respawnDragon(List<EndCrystal> list) {
		if (this.dragonKilled && this.respawnStage == null) {
			for (BlockPattern.BlockPatternMatch blockPatternMatch = this.findExitPortal(); blockPatternMatch != null; blockPatternMatch = this.findExitPortal()) {
				for (int i = 0; i < this.exitPortalPattern.getWidth(); i++) {
					for (int j = 0; j < this.exitPortalPattern.getHeight(); j++) {
						for (int k = 0; k < this.exitPortalPattern.getDepth(); k++) {
							BlockInWorld blockInWorld = blockPatternMatch.getBlock(i, j, k);
							if (blockInWorld.getState().is(Blocks.BEDROCK) || blockInWorld.getState().is(Blocks.END_PORTAL)) {
								this.level.setBlockAndUpdate(blockInWorld.getPos(), Blocks.END_STONE.defaultBlockState());
							}
						}
					}
				}
			}

			this.respawnStage = DragonRespawnAnimation.START;
			this.respawnTime = 0;
			this.spawnExitPortal(false);
			this.respawnCrystals = list;
		}
	}

	public void resetSpikeCrystals() {
		for (SpikeFeature.EndSpike endSpike : SpikeFeature.getSpikesForLevel(this.level)) {
			for (EndCrystal endCrystal : this.level.getEntitiesOfClass(EndCrystal.class, endSpike.getTopBoundingBox())) {
				endCrystal.setInvulnerable(false);
				endCrystal.setBeamTarget(null);
			}
		}
	}
}
