/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.advancements;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;

public class AdvancementVisibilityEvaluator {
    private static final int VISIBILITY_DEPTH = 2;

    private static VisibilityRule evaluateVisibilityRule(Advancement advancement, boolean bl) {
        DisplayInfo displayInfo = advancement.getDisplay();
        if (displayInfo == null) {
            return VisibilityRule.HIDE;
        }
        if (bl) {
            return VisibilityRule.SHOW;
        }
        if (displayInfo.isHidden()) {
            return VisibilityRule.HIDE;
        }
        return VisibilityRule.NO_CHANGE;
    }

    private static boolean evaluateVisiblityForUnfinishedNode(Stack<VisibilityRule> stack) {
        for (int i = 0; i <= 2; ++i) {
            VisibilityRule visibilityRule = stack.peek(i);
            if (visibilityRule == VisibilityRule.SHOW) {
                return true;
            }
            if (visibilityRule != VisibilityRule.HIDE) continue;
            return false;
        }
        return false;
    }

    private static boolean evaluateVisibility(Advancement advancement, Stack<VisibilityRule> stack, Predicate<Advancement> predicate, Output output) {
        boolean bl = predicate.test(advancement);
        VisibilityRule visibilityRule = AdvancementVisibilityEvaluator.evaluateVisibilityRule(advancement, bl);
        boolean bl2 = bl;
        stack.push(visibilityRule);
        for (Advancement advancement2 : advancement.getChildren()) {
            bl2 |= AdvancementVisibilityEvaluator.evaluateVisibility(advancement2, stack, predicate, output);
        }
        boolean bl3 = bl2 || AdvancementVisibilityEvaluator.evaluateVisiblityForUnfinishedNode(stack);
        stack.pop();
        output.accept(advancement, bl3);
        return bl2;
    }

    public static void evaluateVisibility(Advancement advancement, Predicate<Advancement> predicate, Output output) {
        Advancement advancement2 = advancement.getRoot();
        ObjectArrayList<VisibilityRule> stack = new ObjectArrayList<VisibilityRule>();
        for (int i = 0; i <= 2; ++i) {
            stack.push(VisibilityRule.NO_CHANGE);
        }
        AdvancementVisibilityEvaluator.evaluateVisibility(advancement2, stack, predicate, output);
    }

    static enum VisibilityRule {
        SHOW,
        HIDE,
        NO_CHANGE;

    }

    @FunctionalInterface
    public static interface Output {
        public void accept(Advancement var1, boolean var2);
    }
}

