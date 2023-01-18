package net.minecraft.server.advancements;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;

public class AdvancementVisibilityEvaluator {
	private static final int VISIBILITY_DEPTH = 2;

	private static AdvancementVisibilityEvaluator.VisibilityRule evaluateVisibilityRule(Advancement advancement, boolean bl) {
		DisplayInfo displayInfo = advancement.getDisplay();
		if (displayInfo == null) {
			return AdvancementVisibilityEvaluator.VisibilityRule.HIDE;
		} else if (bl) {
			return AdvancementVisibilityEvaluator.VisibilityRule.SHOW;
		} else {
			return displayInfo.isHidden() ? AdvancementVisibilityEvaluator.VisibilityRule.HIDE : AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE;
		}
	}

	private static boolean evaluateVisiblityForUnfinishedNode(Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack) {
		for (int i = 0; i <= 2; i++) {
			AdvancementVisibilityEvaluator.VisibilityRule visibilityRule = stack.peek(i);
			if (visibilityRule == AdvancementVisibilityEvaluator.VisibilityRule.SHOW) {
				return true;
			}

			if (visibilityRule == AdvancementVisibilityEvaluator.VisibilityRule.HIDE) {
				return false;
			}
		}

		return false;
	}

	private static boolean evaluateVisibility(
		Advancement advancement,
		Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack,
		Predicate<Advancement> predicate,
		AdvancementVisibilityEvaluator.Output output
	) {
		boolean bl = predicate.test(advancement);
		AdvancementVisibilityEvaluator.VisibilityRule visibilityRule = evaluateVisibilityRule(advancement, bl);
		boolean bl2 = bl;
		stack.push(visibilityRule);

		for (Advancement advancement2 : advancement.getChildren()) {
			bl2 |= evaluateVisibility(advancement2, stack, predicate, output);
		}

		boolean bl3 = bl2 || evaluateVisiblityForUnfinishedNode(stack);
		stack.pop();
		output.accept(advancement, bl3);
		return bl2;
	}

	public static void evaluateVisibility(Advancement advancement, Predicate<Advancement> predicate, AdvancementVisibilityEvaluator.Output output) {
		Advancement advancement2 = advancement.getRoot();
		Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack = new ObjectArrayList<>();

		for (int i = 0; i <= 2; i++) {
			stack.push(AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE);
		}

		evaluateVisibility(advancement2, stack, predicate, output);
	}

	@FunctionalInterface
	public interface Output {
		void accept(Advancement advancement, boolean bl);
	}

	static enum VisibilityRule {
		SHOW,
		HIDE,
		NO_CHANGE;
	}
}
