package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public class BeaconBlockEntity extends BlockEntity implements MenuProvider, TickableBlockEntity {
	public static final MobEffect[][] BEACON_EFFECTS = new MobEffect[][]{
		{MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED}, {MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP}, {MobEffects.DAMAGE_BOOST}, {MobEffects.REGENERATION}
	};
	private static final Set<MobEffect> VALID_EFFECTS = (Set<MobEffect>)Arrays.stream(BEACON_EFFECTS).flatMap(Arrays::stream).collect(Collectors.toSet());
	private List<BeaconBlockEntity.BeaconBeamSection> beamSections = Lists.<BeaconBlockEntity.BeaconBeamSection>newArrayList();
	private List<BeaconBlockEntity.BeaconBeamSection> checkingBeamSections = Lists.<BeaconBlockEntity.BeaconBeamSection>newArrayList();
	private int levels;
	private int lastCheckY = -1;
	@Nullable
	private MobEffect primaryPower;
	@Nullable
	private MobEffect secondaryPower;
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
						BeaconBlockEntity.this.playSound(SoundEvents.BEACON_POWER_SELECT);
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

	public BeaconBlockEntity() {
		super(BlockEntityType.BEACON);
	}

	@Override
	public void tick() {
		int i = this.worldPosition.getX();
		int j = this.worldPosition.getY();
		int k = this.worldPosition.getZ();
		BlockPos blockPos;
		if (this.lastCheckY < j) {
			blockPos = this.worldPosition;
			this.checkingBeamSections = Lists.<BeaconBlockEntity.BeaconBeamSection>newArrayList();
			this.lastCheckY = blockPos.getY() - 1;
		} else {
			blockPos = new BlockPos(i, this.lastCheckY + 1, k);
		}

		BeaconBlockEntity.BeaconBeamSection beaconBeamSection = this.checkingBeamSections.isEmpty()
			? null
			: (BeaconBlockEntity.BeaconBeamSection)this.checkingBeamSections.get(this.checkingBeamSections.size() - 1);
		int l = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, i, k);

		for (int m = 0; m < 10 && blockPos.getY() <= l; m++) {
			BlockState blockState = this.level.getBlockState(blockPos);
			Block block = blockState.getBlock();
			if (block instanceof BeaconBeamBlock) {
				float[] fs = ((BeaconBeamBlock)block).getColor().getTextureDiffuseColors();
				if (this.checkingBeamSections.size() <= 1) {
					beaconBeamSection = new BeaconBlockEntity.BeaconBeamSection(fs);
					this.checkingBeamSections.add(beaconBeamSection);
				} else if (beaconBeamSection != null) {
					if (Arrays.equals(fs, beaconBeamSection.color)) {
						beaconBeamSection.increaseHeight();
					} else {
						beaconBeamSection = new BeaconBlockEntity.BeaconBeamSection(
							new float[]{(beaconBeamSection.color[0] + fs[0]) / 2.0F, (beaconBeamSection.color[1] + fs[1]) / 2.0F, (beaconBeamSection.color[2] + fs[2]) / 2.0F}
						);
						this.checkingBeamSections.add(beaconBeamSection);
					}
				}
			} else {
				if (beaconBeamSection == null || blockState.getLightBlock(this.level, blockPos) >= 15 && block != Blocks.BEDROCK) {
					this.checkingBeamSections.clear();
					this.lastCheckY = l;
					break;
				}

				beaconBeamSection.increaseHeight();
			}

			blockPos = blockPos.above();
			this.lastCheckY++;
		}

		int m = this.levels;
		if (this.level.getGameTime() % 80L == 0L) {
			if (!this.beamSections.isEmpty()) {
				this.updateBase(i, j, k);
			}

			if (this.levels > 0 && !this.beamSections.isEmpty()) {
				this.applyEffects();
				this.playSound(SoundEvents.BEACON_AMBIENT);
			}
		}

		if (this.lastCheckY >= l) {
			this.lastCheckY = -1;
			boolean bl = m > 0;
			this.beamSections = this.checkingBeamSections;
			if (!this.level.isClientSide) {
				boolean bl2 = this.levels > 0;
				if (!bl && bl2) {
					this.playSound(SoundEvents.BEACON_ACTIVATE);

					for (ServerPlayer serverPlayer : this.level
						.getEntitiesOfClass(ServerPlayer.class, new AABB((double)i, (double)j, (double)k, (double)i, (double)(j - 4), (double)k).inflate(10.0, 5.0, 10.0))) {
						CriteriaTriggers.CONSTRUCT_BEACON.trigger(serverPlayer, this);
					}
				} else if (bl && !bl2) {
					this.playSound(SoundEvents.BEACON_DEACTIVATE);
				}
			}
		}
	}

	private void updateBase(int i, int j, int k) {
		this.levels = 0;

		for (int l = 1; l <= 4; this.levels = l++) {
			int m = j - l;
			if (m < 0) {
				break;
			}

			boolean bl = true;

			for (int n = i - l; n <= i + l && bl; n++) {
				for (int o = k - l; o <= k + l; o++) {
					if (!this.level.getBlockState(new BlockPos(n, m, o)).is(BlockTags.BEACON_BASE_BLOCKS)) {
						bl = false;
						break;
					}
				}
			}

			if (!bl) {
				break;
			}
		}
	}

	@Override
	public void setRemoved() {
		this.playSound(SoundEvents.BEACON_DEACTIVATE);
		super.setRemoved();
	}

	private void applyEffects() {
		if (!this.level.isClientSide && this.primaryPower != null) {
			double d = (double)(this.levels * 10 + 10);
			int i = 0;
			if (this.levels >= 4 && this.primaryPower == this.secondaryPower) {
				i = 1;
			}

			int j = (9 + this.levels * 2) * 20;
			AABB aABB = new AABB(this.worldPosition).inflate(d).expandTowards(0.0, (double)this.level.getMaxBuildHeight(), 0.0);
			List<Player> list = this.level.getEntitiesOfClass(Player.class, aABB);

			for (Player player : list) {
				player.addEffect(new MobEffectInstance(this.primaryPower, j, i, true, true));
			}

			if (this.levels >= 4 && this.primaryPower != this.secondaryPower && this.secondaryPower != null) {
				for (Player player : list) {
					player.addEffect(new MobEffectInstance(this.secondaryPower, j, 0, true, true));
				}
			}
		}
	}

	public void playSound(SoundEvent soundEvent) {
		this.level.playSound(null, this.worldPosition, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	@Environment(EnvType.CLIENT)
	public List<BeaconBlockEntity.BeaconBeamSection> getBeamSections() {
		return (List<BeaconBlockEntity.BeaconBeamSection>)(this.levels == 0 ? ImmutableList.of() : this.beamSections);
	}

	public int getLevels() {
		return this.levels;
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

	@Environment(EnvType.CLIENT)
	@Override
	public double getViewDistance() {
		return 65536.0;
	}

	@Nullable
	private static MobEffect getValidEffectById(int i) {
		MobEffect mobEffect = MobEffect.byId(i);
		return VALID_EFFECTS.contains(mobEffect) ? mobEffect : null;
	}

	@Override
	public void load(BlockState blockState, CompoundTag compoundTag) {
		super.load(blockState, compoundTag);
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

	public static class BeaconBeamSection {
		private final float[] color;
		private int height;

		public BeaconBeamSection(float[] fs) {
			this.color = fs;
			this.height = 1;
		}

		protected void increaseHeight() {
			this.height++;
		}

		@Environment(EnvType.CLIENT)
		public float[] getColor() {
			return this.color;
		}

		@Environment(EnvType.CLIENT)
		public int getHeight() {
			return this.height;
		}
	}
}
