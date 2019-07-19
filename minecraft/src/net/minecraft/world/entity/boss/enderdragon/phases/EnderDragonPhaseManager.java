package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnderDragonPhaseManager {
	private static final Logger LOGGER = LogManager.getLogger();
	private final EnderDragon dragon;
	private final DragonPhaseInstance[] phases = new DragonPhaseInstance[EnderDragonPhase.getCount()];
	private DragonPhaseInstance currentPhase;

	public EnderDragonPhaseManager(EnderDragon enderDragon) {
		this.dragon = enderDragon;
		this.setPhase(EnderDragonPhase.HOVERING);
	}

	public void setPhase(EnderDragonPhase<?> enderDragonPhase) {
		if (this.currentPhase == null || enderDragonPhase != this.currentPhase.getPhase()) {
			if (this.currentPhase != null) {
				this.currentPhase.end();
			}

			this.currentPhase = this.getPhase((EnderDragonPhase<DragonPhaseInstance>)enderDragonPhase);
			if (!this.dragon.level.isClientSide) {
				this.dragon.getEntityData().set(EnderDragon.DATA_PHASE, enderDragonPhase.getId());
			}

			LOGGER.debug("Dragon is now in phase {} on the {}", enderDragonPhase, this.dragon.level.isClientSide ? "client" : "server");
			this.currentPhase.begin();
		}
	}

	public DragonPhaseInstance getCurrentPhase() {
		return this.currentPhase;
	}

	public <T extends DragonPhaseInstance> T getPhase(EnderDragonPhase<T> enderDragonPhase) {
		int i = enderDragonPhase.getId();
		if (this.phases[i] == null) {
			this.phases[i] = enderDragonPhase.createInstance(this.dragon);
		}

		return (T)this.phases[i];
	}
}
