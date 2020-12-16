package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class PlayDead extends Behavior<Axolotl> {
	public PlayDead() {
		super(ImmutableMap.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryStatus.VALUE_PRESENT, MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_PRESENT), 200);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Axolotl axolotl) {
		return axolotl.isInWaterOrBubble();
	}

	protected boolean canStillUse(ServerLevel serverLevel, Axolotl axolotl, long l) {
		return axolotl.isInWaterOrBubble() && axolotl.getBrain().hasMemoryValue(MemoryModuleType.PLAY_DEAD_TICKS);
	}

	protected void start(ServerLevel serverLevel, Axolotl axolotl, long l) {
		Brain<Axolotl> brain = axolotl.getBrain();
		brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
		axolotl.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
	}
}
