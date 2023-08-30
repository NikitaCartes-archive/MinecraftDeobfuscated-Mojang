package net.minecraft.server.advancements;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;

public class AdvancementVisibilityEvaluator {
	private static final int VISIBILITY_DEPTH = 2;

	private static AdvancementVisibilityEvaluator.VisibilityRule evaluateVisibilityRule(Advancement advancement, boolean bl) {
		Optional<DisplayInfo> optional = advancement.display();
		if (optional.isEmpty()) {
			return AdvancementVisibilityEvaluator.VisibilityRule.HIDE;
		} else if (bl) {
			return AdvancementVisibilityEvaluator.VisibilityRule.SHOW;
		} else {
			return ((DisplayInfo)optional.get()).isHidden()
				? AdvancementVisibilityEvaluator.VisibilityRule.HIDE
				: AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE;
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
		AdvancementNode advancementNode,
		Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack,
		Predicate<AdvancementNode> predicate,
		AdvancementVisibilityEvaluator.Output output
	) {
		boolean bl = predicate.test(advancementNode);
		AdvancementVisibilityEvaluator.VisibilityRule visibilityRule = evaluateVisibilityRule(advancementNode.advancement(), bl);
		boolean bl2 = bl;
		stack.push(visibilityRule);

		for (AdvancementNode advancementNode2 : advancementNode.children()) {
			bl2 |= evaluateVisibility(advancementNode2, stack, predicate, output);
		}

		boolean bl3 = bl2 || evaluateVisiblityForUnfinishedNode(stack);
		stack.pop();
		output.accept(advancementNode, bl3);
		return bl2;
	}

	public static void evaluateVisibility(AdvancementNode advancementNode, Predicate<AdvancementNode> predicate, AdvancementVisibilityEvaluator.Output output) {
		AdvancementNode advancementNode2 = advancementNode.root();
		Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack = new ObjectArrayList<>();

		for (int i = 0; i <= 2; i++) {
			stack.push(AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE);
		}

		evaluateVisibility(advancementNode2, stack, predicate, output);
	}

	@FunctionalInterface
	public interface Output {
		void accept(AdvancementNode advancementNode, boolean bl);
	}

	static enum VisibilityRule {
		SHOW,
		HIDE,
		NO_CHANGE;
	}
}
