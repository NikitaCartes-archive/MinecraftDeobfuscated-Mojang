package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public class BeaconBlockEntity extends BlockEntity implements MenuProvider {
	private static final int MAX_LEVELS = 4;
	public static final MobEffect[][] BEACON_EFFECTS = new MobEffect[][]{
		{MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED}, {MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP}, {MobEffects.DAMAGE_BOOST}, {MobEffects.REGENERATION}
	};
	private static final Set<MobEffect> VALID_EFFECTS = (Set<MobEffect>)Arrays.stream(BEACON_EFFECTS).flatMap(Arrays::stream).collect(Collectors.toSet());
	public static final int DATA_LEVELS = 0;
	public static final int DATA_PRIMARY = 1;
	public static final int DATA_SECONDARY = 2;
	public static final int NUM_DATA_VALUES = 3;
	private static final int BLOCKS_CHECK_PER_TICK = 10;
	List<BeaconBlockEntity.BeaconBeamSection> beamSections = Lists.<BeaconBlockEntity.BeaconBeamSection>newArrayList();
	private List<BeaconBlockEntity.BeaconBeamSection> checkingBeamSections = Lists.<BeaconBlockEntity.BeaconBeamSection>newArrayList();
	int levels;
	private int lastCheckY;
	@Nullable
	MobEffect primaryPower;
	@Nullable
	MobEffect secondaryPower;
	@Nullable
	private Component name;
	private LockCode lockKey = LockCode.NO_LOCK;
	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int i) {
			switch (i) {
				case 0:
					return BeaconBlockEntity.this.levels;
				case 1:
					return MobEffect.getId(BeaconBlockEntity.this.primaryPower);
				case 2:
					return MobEffect.getId(BeaconBlockEntity.this.secondaryPower);
				default:
					return 0;
			}
		}

		@Override
		public void set(int i, int j) {
			switch (i) {
				case 0:
					BeaconBlockEntity.this.levels = j;
					break;
				case 1:
					if (!BeaconBlockEntity.this.level.isClientSide && !BeaconBlockEntity.this.beamSections.isEmpty()) {
						BeaconBlockEntity.playSound(BeaconBlockEntity.this.level, BeaconBlockEntity.this.worldPosition, SoundEvents.BEACON_POWER_SELECT);
					}

					BeaconBlockEntity.this.primaryPower = BeaconBlockEntity.getValidEffectById(j);
					break;
				case 2:
					BeaconBlockEntity.this.secondaryPower = BeaconBlockEntity.getValidEffectById(j);
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
	};

	public BeaconBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.BEACON, blockPos, blockState);
	}

	public static void tick(Level level, BlockPos blockPos, BlockState blockState, BeaconBlockEntity beaconBlockEntity) {
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		BlockPos blockPos2;
		if (beaconBlockEntity.lastCheckY < j) {
			blockPos2 = blockPos;
			beaconBlockEntity.checkingBeamSections = Lists.<BeaconBlockEntity.BeaconBeamSection>newArrayList();
			beaconBlockEntity.lastCheckY = blockPos.getY() - 1;
		} else {
			blockPos2 = new BlockPos(i, beaconBlockEntity.lastCheckY + 1, k);
		}

		BeaconBlockEntity.BeaconBeamSection beaconBeamSection = beaconBlockEntity.checkingBeamSections.isEmpty()
			? null
			: (BeaconBlockEntity.BeaconBeamSection)beaconBlockEntity.checkingBeamSections.get(beaconBlockEntity.checkingBeamSections.size() - 1);
		int l = level.getHeight(Heightmap.Types.WORLD_SURFACE, i, k);

		for (int m = 0; m < 10 && blockPos2.getY() <= l; m++) {
			BlockState blockState2 = level.getBlockState(blockPos2);
			Block block = blockState2.getBlock();
			if (block instanceof BeaconBeamBlock) {
				float[] fs = ((BeaconBeamBlock)block).getColor().getTextureDiffuseColors();
				if (beaconBlockEntity.checkingBeamSections.size() <= 1) {
					beaconBeamSection = new BeaconBlockEntity.BeaconBeamSection(fs);
					beaconBlockEntity.checkingBeamSections.add(beaconBeamSection);
				} else if (beaconBeamSection != null) {
					if (Arrays.equals(fs, beaconBeamSection.color)) {
						beaconBeamSection.increaseHeight();
					} else {
						beaconBeamSection = new BeaconBlockEntity.BeaconBeamSection(
							new float[]{(beaconBeamSection.color[0] + fs[0]) / 2.0F, (beaconBeamSection.color[1] + fs[1]) / 2.0F, (beaconBeamSection.color[2] + fs[2]) / 2.0F}
						);
						beaconBlockEntity.checkingBeamSections.add(beaconBeamSection);
					}
				}
			} else {
				if (beaconBeamSection == null || blockState2.getLightBlock(level, blockPos2) >= 15 && !blockState2.is(Blocks.BEDROCK)) {
					beaconBlockEntity.checkingBeamSections.clear();
					beaconBlockEntity.lastCheckY = l;
					break;
				}

				beaconBeamSection.increaseHeight();
			}

			blockPos2 = blockPos2.above();
			beaconBlockEntity.lastCheckY++;
		}

		int m = beaconBlockEntity.levels;
		if (level.getGameTime() % 80L == 0L) {
			if (!beaconBlockEntity.beamSections.isEmpty()) {
				beaconBlockEntity.levels = updateBase(level, i, j, k);
			}

			if (beaconBlockEntity.levels > 0 && !beaconBlockEntity.beamSections.isEmpty()) {
				applyEffects(level, blockPos, beaconBlockEntity.levels, beaconBlockEntity.primaryPower, beaconBlockEntity.secondaryPower);
				playSound(level, blockPos, SoundEvents.BEACON_AMBIENT);
			}
		}

		if (beaconBlockEntity.lastCheckY >= l) {
			beaconBlockEntity.lastCheckY = level.getMinBuildHeight() - 1;
			boolean bl = m > 0;
			beaconBlockEntity.beamSections = beaconBlockEntity.checkingBeamSections;
			if (!level.isClientSide) {
				boolean bl2 = beaconBlockEntity.levels > 0;
				if (!bl && bl2) {
					playSound(level, blockPos, SoundEvents.BEACON_ACTIVATE);

					for (ServerPlayer serverPlayer : level.getEntitiesOfClass(
						ServerPlayer.class, new AABB((double)i, (double)j, (double)k, (double)i, (double)(j - 4), (double)k).inflate(10.0, 5.0, 10.0)
					)) {
						CriteriaTriggers.CONSTRUCT_BEACON.trigger(serverPlayer, beaconBlockEntity.levels);
					}
				} else if (bl && !bl2) {
					playSound(level, blockPos, SoundEvents.BEACON_DEACTIVATE);
				}
			}
		}
	}

	private static int updateBase(Level level, int i, int j, int k) {
		int l = 0;

		for (int m = 1; m <= 4; l = m++) {
			int n = j - m;
			if (n < level.getMinBuildHeight()) {
				break;
			}

			boolean bl = true;

			for (int o = i - m; o <= i + m && bl; o++) {
				for (int p = k - m; p <= k + m; p++) {
					if (!level.getBlockState(new BlockPos(o, n, p)).is(BlockTags.BEACON_BASE_BLOCKS)) {
						bl = false;
						break;
					}
				}
			}

			if (!bl) {
				break;
			}
		}

		return l;
	}

	@Override
	public void setRemoved() {
		playSound(this.level, this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
		super.setRemoved();
	}

	private static void applyEffects(Level level, BlockPos blockPos, int i, @Nullable MobEffect mobEffect, @Nullable MobEffect mobEffect2) {
		if (!level.isClientSide && mobEffect != null) {
			double d = (double)(i * 10 + 10);
			int j = 0;
			if (i >= 4 && mobEffect == mobEffect2) {
				j = 1;
			}

			int k = (9 + i * 2) * 20;
			AABB aABB = new AABB(blockPos).inflate(d).expandTowards(0.0, (double)level.getHeight(), 0.0);
			List<Player> list = level.getEntitiesOfClass(Player.class, aABB);

			for (Player player : list) {
				player.addEffect(new MobEffectInstance(mobEffect, k, j, true, true));
			}

			if (i >= 4 && mobEffect != mobEffect2 && mobEffect2 != null) {
				for (Player player : list) {
					player.addEffect(new MobEffectInstance(mobEffect2, k, 0, true, true));
				}
			}
		}
	}

	public static void playSound(Level level, BlockPos blockPos, SoundEvent soundEvent) {
		level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	public List<BeaconBlockEntity.BeaconBeamSection> getBeamSections() {
		return (List<BeaconBlockEntity.BeaconBeamSection>)(this.levels == 0 ? ImmutableList.of() : this.beamSections);
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 3, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}

	@Nullable
	static MobEffect getValidEffectById(int i) {
		MobEffect mobEffect = MobEffect.byId(i);
		return VALID_EFFECTS.contains(mobEffect) ? mobEffect : null;
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.primaryPower = getValidEffectById(compoundTag.getInt("Primary"));
		this.secondaryPower = getValidEffectById(compoundTag.getInt("Secondary"));
		if (compoundTag.contains("CustomName", 8)) {
			this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
		}

		this.lockKey = LockCode.fromTag(compoundTag);
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		compoundTag.putInt("Primary", MobEffect.getId(this.primaryPower));
		compoundTag.putInt("Secondary", MobEffect.getId(this.secondaryPower));
		compoundTag.putInt("Levels", this.levels);
		if (this.name != null) {
			compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
		}

		this.lockKey.addToTag(compoundTag);
		return compoundTag;
	}

	public void setCustomName(@Nullable Component component) {
		this.name = component;
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		return BaseContainerBlockEntity.canUnlock(player, this.lockKey, this.getDisplayName())
			? new BeaconMenu(i, inventory, this.dataAccess, ContainerLevelAccess.create(this.level, this.getBlockPos()))
			: null;
	}

	@Override
	public Component getDisplayName() {
		return (Component)(this.name != null ? this.name : new TranslatableComponent("container.beacon"));
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		this.lastCheckY = level.getMinBuildHeight() - 1;
	}

	public static class BeaconBeamSection {
		final float[] color;
		private int height;

		public BeaconBeamSection(float[] fs) {
			this.color = fs;
			this.height = 1;
		}

		protected void increaseHeight() {
			this.height++;
		}

		public float[] getColor() {
			return this.color;
		}

		public int getHeight() {
			return this.height;
		}
	}
}
