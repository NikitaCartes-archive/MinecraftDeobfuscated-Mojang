/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

public class LookAtTradingPlayerGoal
extends LookAtPlayerGoal {
    private final AbstractVillager villager;

    public LookAtTradingPlayerGoal(AbstractVillager abstractVillager) {
        super(abstractVillager, Player.class, 8.0f);
        this.villager = abstractVillager;
    }

    @Override
    public boolean canUse() {
        if (this.villager.isTrading()) {
            this.lookAt = this.villager.getTradingPlayer();
            return true;
        }
        return false;
    }
}

