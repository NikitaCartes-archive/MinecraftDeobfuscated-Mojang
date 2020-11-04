/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Collection;
import net.minecraft.gametest.framework.GameTestInfo;

public class GameTestTicker {
    public static final GameTestTicker SINGLETON = new GameTestTicker();
    private final Collection<GameTestInfo> testInfos = Lists.newCopyOnWriteArrayList();

    public void add(GameTestInfo gameTestInfo) {
        this.testInfos.add(gameTestInfo);
    }

    public void clear() {
        this.testInfos.clear();
    }

    public void tick() {
        this.testInfos.forEach(GameTestInfo::tick);
        this.testInfos.removeIf(GameTestInfo::isDone);
    }
}

