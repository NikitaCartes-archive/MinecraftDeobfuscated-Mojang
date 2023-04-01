package net.minecraft.world.damagesource;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CombatTracker {
	public static final int RESET_DAMAGE_STATUS_TIME = 100;
	public static final int RESET_COMBAT_STATUS_TIME = 300;
	private static final Style INTENTIONAL_GAME_DESIGN_STYLE = Style.EMPTY
		.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MCPE-28723"))
		.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("MCPE-28723")));
	private final List<CombatEntry> entries = Lists.<CombatEntry>newArrayList();
	private final LivingEntity mob;
	private int lastDamageTime;
	private int combatStartTime;
	private int combatEndTime;
	private boolean inCombat;
	private boolean takingDamage;
	@Nullable
	private String nextLocation;

	public CombatTracker(LivingEntity livingEntity) {
		this.mob = livingEntity;
	}

	public void prepareForDamage() {
		this.resetPreparedStatus();
		Optional<BlockPos> optional = this.mob.getLastClimbablePos();
		if (optional.isPresent()) {
			BlockState blockState = this.mob.level.getBlockState((BlockPos)optional.get());
			if (blockState.is(Blocks.LADDER) || blockState.is(BlockTags.TRAPDOORS)) {
				this.nextLocation = "ladder";
			} else if (blockState.is(Blocks.VINE)) {
				this.nextLocation = "vines";
			} else if (blockState.is(Blocks.WEEPING_VINES) || blockState.is(Blocks.WEEPING_VINES_PLANT)) {
				this.nextLocation = "weeping_vines";
			} else if (blockState.is(Blocks.TWISTING_VINES) || blockState.is(Blocks.TWISTING_VINES_PLANT)) {
				this.nextLocation = "twisting_vines";
			} else if (blockState.is(Blocks.SCAFFOLDING)) {
				this.nextLocation = "scaffolding";
			} else {
				this.nextLocation = "other_climbable";
			}
		} else if (this.mob.isInWater()) {
			this.nextLocation = "water";
		}
	}

	public void recordDamage(DamageSource damageSource, float f, float g) {
		this.recheckStatus();
		this.prepareForDamage();
		CombatEntry combatEntry = new CombatEntry(damageSource, this.mob.tickCount, f, g, this.nextLocation, this.mob.fallDistance);
		this.entries.add(combatEntry);
		this.lastDamageTime = this.mob.tickCount;
		this.takingDamage = true;
		if (combatEntry.isCombatRelated() && !this.inCombat && this.mob.isAlive()) {
			this.inCombat = true;
			this.combatStartTime = this.mob.tickCount;
			this.combatEndTime = this.combatStartTime;
			this.mob.onEnterCombat();
		}
	}

	public Component getDeathMessage() {
		if (this.entries.isEmpty()) {
			return Component.translatable("death.attack.generic", this.mob.getDisplayName());
		} else {
			CombatEntry combatEntry = this.getMostSignificantFall();
			CombatEntry combatEntry2 = (CombatEntry)this.entries.get(this.entries.size() - 1);
			Component component = combatEntry2.getAttackerName();
			DamageSource damageSource = combatEntry2.getSource();
			Entity entity = damageSource.getEntity();
			DeathMessageType deathMessageType = damageSource.type().deathMessageType();
			Component component3;
			if (combatEntry != null && deathMessageType == DeathMessageType.FALL_VARIANTS) {
				Component component2 = combatEntry.getAttackerName();
				DamageSource damageSource2 = combatEntry.getSource();
				if (damageSource2.is(DamageTypeTags.IS_FALL) || damageSource2.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL)) {
					component3 = Component.translatable("death.fell.accident." + this.getFallLocation(combatEntry), this.mob.getDisplayName());
				} else if (component2 != null && !component2.equals(component)) {
					ItemStack itemStack = damageSource2.getEntity() instanceof LivingEntity livingEntity ? livingEntity.getMainHandItem() : ItemStack.EMPTY;
					if (!itemStack.isEmpty() && itemStack.hasCustomHoverName()) {
						component3 = Component.translatable("death.fell.assist.item", this.mob.getDisplayName(), component2, itemStack.getDisplayName());
					} else {
						component3 = Component.translatable("death.fell.assist", this.mob.getDisplayName(), component2);
					}
				} else if (component != null) {
					ItemStack itemStack2 = entity instanceof LivingEntity livingEntity2 ? livingEntity2.getMainHandItem() : ItemStack.EMPTY;
					if (!itemStack2.isEmpty() && itemStack2.hasCustomHoverName()) {
						component3 = Component.translatable("death.fell.finish.item", this.mob.getDisplayName(), component, itemStack2.getDisplayName());
					} else {
						component3 = Component.translatable("death.fell.finish", this.mob.getDisplayName(), component);
					}
				} else {
					component3 = Component.translatable("death.fell.killer", this.mob.getDisplayName());
				}
			} else {
				if (deathMessageType == DeathMessageType.INTENTIONAL_GAME_DESIGN) {
					String string = "death.attack." + damageSource.getMsgId();
					Component component4 = ComponentUtils.wrapInSquareBrackets(Component.translatable(string + ".link")).withStyle(INTENTIONAL_GAME_DESIGN_STYLE);
					return Component.translatable(string + ".message", this.mob.getDisplayName(), component4);
				}

				if (deathMessageType == DeathMessageType.MIDAS_CURSE) {
					return Component.translatable("death.midas.turned_into_gold", this.mob.getDisplayName());
				}

				component3 = damageSource.getLocalizedDeathMessage(this.mob);
			}

			return component3;
		}
	}

	@Nullable
	public LivingEntity getKiller() {
		LivingEntity livingEntity = null;
		Player player = null;
		float f = 0.0F;
		float g = 0.0F;

		for (CombatEntry combatEntry : this.entries) {
			if (combatEntry.getSource().getEntity() instanceof Player player2 && (player == null || combatEntry.getDamage() > g)) {
				g = combatEntry.getDamage();
				player = player2;
			}

			if (combatEntry.getSource().getEntity() instanceof LivingEntity livingEntity2 && (livingEntity == null || combatEntry.getDamage() > f)) {
				f = combatEntry.getDamage();
				livingEntity = livingEntity2;
			}
		}

		return (LivingEntity)(player != null && g >= f / 3.0F ? player : livingEntity);
	}

	@Nullable
	private CombatEntry getMostSignificantFall() {
		CombatEntry combatEntry = null;
		CombatEntry combatEntry2 = null;
		float f = 0.0F;
		float g = 0.0F;

		for (int i = 0; i < this.entries.size(); i++) {
			CombatEntry combatEntry3 = (CombatEntry)this.entries.get(i);
			CombatEntry combatEntry4 = i > 0 ? (CombatEntry)this.entries.get(i - 1) : null;
			DamageSource damageSource = combatEntry3.getSource();
			boolean bl = damageSource.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL);
			float h = bl ? Float.MAX_VALUE : combatEntry3.getFallDistance();
			if ((damageSource.is(DamageTypeTags.IS_FALL) || bl) && h > 0.0F && (combatEntry == null || h > g)) {
				if (i > 0) {
					combatEntry = combatEntry4;
				} else {
					combatEntry = combatEntry3;
				}

				g = h;
			}

			if (combatEntry3.getLocation() != null && (combatEntry2 == null || combatEntry3.getDamage() > f)) {
				combatEntry2 = combatEntry3;
				f = combatEntry3.getDamage();
			}
		}

		if (g > 5.0F && combatEntry != null) {
			return combatEntry;
		} else {
			return f > 5.0F && combatEntry2 != null ? combatEntry2 : null;
		}
	}

	private String getFallLocation(CombatEntry combatEntry) {
		return combatEntry.getLocation() == null ? "generic" : combatEntry.getLocation();
	}

	public boolean isTakingDamage() {
		this.recheckStatus();
		return this.takingDamage;
	}

	public boolean isInCombat() {
		this.recheckStatus();
		return this.inCombat;
	}

	public int getCombatDuration() {
		return this.inCombat ? this.mob.tickCount - this.combatStartTime : this.combatEndTime - this.combatStartTime;
	}

	private void resetPreparedStatus() {
		this.nextLocation = null;
	}

	public void recheckStatus() {
		int i = this.inCombat ? 300 : 100;
		if (this.takingDamage && (!this.mob.isAlive() || this.mob.tickCount - this.lastDamageTime > i)) {
			boolean bl = this.inCombat;
			this.takingDamage = false;
			this.inCombat = false;
			this.combatEndTime = this.mob.tickCount;
			if (bl) {
				this.mob.onLeaveCombat();
			}

			this.entries.clear();
		}
	}

	public LivingEntity getMob() {
		return this.mob;
	}

	@Nullable
	public CombatEntry getLastEntry() {
		return this.entries.isEmpty() ? null : (CombatEntry)this.entries.get(this.entries.size() - 1);
	}

	public int getKillerId() {
		LivingEntity livingEntity = this.getKiller();
		return livingEntity == null ? -1 : livingEntity.getId();
	}
}
