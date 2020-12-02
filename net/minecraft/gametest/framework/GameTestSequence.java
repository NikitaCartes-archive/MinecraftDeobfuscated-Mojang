/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import java.util.Iterator;
import java.util.List;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestEvent;
import net.minecraft.gametest.framework.GameTestInfo;

public class GameTestSequence {
    private final GameTestInfo parent;
    private final List<GameTestEvent> events;
    private long lastTick;

    public void tickAndContinue(long l) {
        try {
            this.tick(l);
        } catch (GameTestAssertException gameTestAssertException) {
            // empty catch block
        }
    }

    public void tickAndFailIfNotComplete(long l) {
        try {
            this.tick(l);
        } catch (GameTestAssertException gameTestAssertException) {
            this.parent.fail(gameTestAssertException);
        }
    }

    private void tick(long l) {
        Iterator<GameTestEvent> iterator = this.events.iterator();
        while (iterator.hasNext()) {
            GameTestEvent gameTestEvent = iterator.next();
            gameTestEvent.assertion.run();
            iterator.remove();
            long m = l - this.lastTick;
            long n = this.lastTick;
            this.lastTick = l;
            if (gameTestEvent.expectedDelay == null || gameTestEvent.expectedDelay == m) continue;
            this.parent.fail(new GameTestAssertException("Succeeded in invalid tick: expected " + (n + gameTestEvent.expectedDelay) + ", but current tick is " + l));
            break;
        }
    }
}

