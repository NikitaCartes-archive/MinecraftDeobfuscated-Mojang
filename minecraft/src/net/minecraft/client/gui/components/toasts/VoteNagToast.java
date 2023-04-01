package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class VoteNagToast implements Toast {
	private static final int MAX_LINE_SIZE = 200;
	private static final int LINE_SPACING = 12;
	private static final int MARGIN = 10;
	private static final Component TITLE = Component.literal("New proposal received!").withStyle(ChatFormatting.LIGHT_PURPLE);
	private final VoteNagToast.Urgency urgency;
	private final Component title;
	private final List<FormattedCharSequence> messageLines;
	private final int width;
	public static final Component VOTE_KEY = Component.keybind("key.voting").withStyle(ChatFormatting.BOLD);
	private static final Map<VoteNagToast.Urgency, List<Component>> NAGS = Map.of(
		VoteNagToast.Urgency.LOW,
		List.of(
			Component.translatable("Press %s to open voting screen", VOTE_KEY),
			Component.translatable("To open voting screen, press %s", VOTE_KEY),
			Component.translatable("New vote started, press %s to cast your vote", VOTE_KEY)
		),
		VoteNagToast.Urgency.MEDIUM,
		List.of(
			Component.translatable("A new proposal is waiting for your vote, press %s", VOTE_KEY),
			Component.translatable("Others are having fun while you are not pressing %s", VOTE_KEY),
			Component.translatable("Time to change some rules, press %s", VOTE_KEY),
			Component.translatable("You have new vote proposals to review, press %s to access!", VOTE_KEY)
		),
		VoteNagToast.Urgency.CONCERNING,
		List.of(
			Component.translatable("Ok, so the whole idea of this release is to vote, so press %s", VOTE_KEY),
			Component.translatable("Somebody wants to tell you what you can and can not do, press %s to prevent that", VOTE_KEY),
			Component.translatable("At this point you are probably just waiting to see what happens next. Fine! But you can avoid that by pressing %s.", VOTE_KEY),
			Component.translatable("If you can't find %s, it's probably on your keyboard", VOTE_KEY),
			Component.translatable("Not pressing %s has been proven less fun that pressing %s", VOTE_KEY, VOTE_KEY),
			Component.translatable("Do you want more phantoms? That's how you get more phantoms! Press %s", VOTE_KEY),
			Component.translatable("Please, just press %s and be done with it!", VOTE_KEY),
			Component.translatable("Hot votes in your area! Press %s", VOTE_KEY)
		),
		VoteNagToast.Urgency.WHY_ARE_YOU_NOT_DOING_IT,
		List.of(
			Component.translatable("PRESS %s ", VOTE_KEY)
				.append(Component.translatable("PRESS %s ", VOTE_KEY))
				.append(Component.translatable("PRESS %s ", VOTE_KEY))
				.append(Component.translatable("PRESS %s ", VOTE_KEY))
				.append(Component.translatable("PRESS %s ", VOTE_KEY))
				.append(Component.translatable("PRESS %s ", VOTE_KEY))
				.append(Component.translatable("PRESS %s ", VOTE_KEY))
				.append(Component.translatable("PRESS %s ", VOTE_KEY))
				.append(Component.translatable("PRESS %s ", VOTE_KEY))
				.append(Component.translatable("PRESS %s ", VOTE_KEY)),
			Component.translatable("WHYYYYYYYYYYYYYYYYYYYYYYYYY NO %s", VOTE_KEY),
			Component.translatable("DO YOU HAVE NO IDEA WHERE %s IS!?", VOTE_KEY),
			Component.translatable("gfdgh bbtvsvtfgfsgb a %sjhrst ujs  t 452423 r", VOTE_KEY),
			Component.translatable("Press %s to open voting screen", VOTE_KEY),
			Component.translatable(
				"%s %s %s",
				Component.literal("AAAAAAA").withStyle(ChatFormatting.RED, ChatFormatting.OBFUSCATED),
				VOTE_KEY,
				Component.literal("AAAAAAA!").withStyle(ChatFormatting.BLUE, ChatFormatting.OBFUSCATED)
			)
		)
	);

	public static Optional<VoteNagToast> create(Minecraft minecraft, RandomSource randomSource, VoteNagToast.Urgency urgency) {
		Font font = minecraft.font;
		return Util.getRandomSafe((List)NAGS.getOrDefault(urgency, List.of()), randomSource).map(component -> {
			List<FormattedCharSequence> list = font.split(component, 200);
			int i = Math.max(200, list.stream().mapToInt(font::width).max().orElse(200));
			return new VoteNagToast(urgency, TITLE, list, i + 30);
		});
	}

	private VoteNagToast(VoteNagToast.Urgency urgency, Component component, List<FormattedCharSequence> list, int i) {
		this.urgency = urgency;
		this.title = component;
		this.messageLines = list;
		this.width = i;
	}

	private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component component) {
		return component == null ? ImmutableList.of() : ImmutableList.of(component.getVisualOrderText());
	}

	@Override
	public int width() {
		return this.width;
	}

	@Override
	public int height() {
		return 20 + Math.max(this.messageLines.size(), 1) * 12;
	}

	@Override
	public Toast.Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
		RenderSystem.setShaderTexture(0, TEXTURE);
		int i = this.width();
		if (i == 160 && this.messageLines.size() <= 1) {
			GuiComponent.blit(poseStack, 0, 0, 0, 64, i, this.height());
		} else {
			int j = this.height();
			int k = 28;
			int m = Math.min(4, j - 28);
			this.renderBackgroundRow(poseStack, toastComponent, i, 0, 0, 28);

			for (int n = 28; n < j - m; n += 10) {
				this.renderBackgroundRow(poseStack, toastComponent, i, 16, n, Math.min(16, j - n - m));
			}

			this.renderBackgroundRow(poseStack, toastComponent, i, 32 - m, j - m, m);
		}

		toastComponent.getMinecraft().font.draw(poseStack, this.title, 18.0F, 7.0F, -256);

		for (int j = 0; j < this.messageLines.size(); j++) {
			toastComponent.getMinecraft().font.draw(poseStack, (FormattedCharSequence)this.messageLines.get(j), 18.0F, (float)(18 + j * 12), -1);
		}

		return l > (long)(50 * this.urgency.toastsDuration) ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
	}

	private void renderBackgroundRow(PoseStack poseStack, ToastComponent toastComponent, int i, int j, int k, int l) {
		int m = j == 0 ? 20 : 5;
		int n = Math.min(60, i - m);
		GuiComponent.blit(poseStack, 0, k, 0, 64 + j, m, l);

		for (int o = m; o < i - n; o += 64) {
			GuiComponent.blit(poseStack, o, k, 32, 64 + j, Math.min(64, i - o - n), l);
		}

		GuiComponent.blit(poseStack, i - n, k, 160 - n, 64 + j, n, l);
	}

	public VoteNagToast.Urgency getToken() {
		return this.urgency;
	}

	@Environment(EnvType.CLIENT)
	public static enum Urgency {
		LOW(300, 2400, 100),
		MEDIUM(200, 2400, 60),
		CONCERNING(100, 1200, 40),
		WHY_ARE_YOU_NOT_DOING_IT(10, 99999, 20);

		private final int period;
		private final int phaseDuration;
		final int toastsDuration;

		private Urgency(int j, int k, int l) {
			this.period = j;
			this.phaseDuration = k;
			this.toastsDuration = l;
		}

		public static Optional<VoteNagToast.Urgency> fromTicks(int i) {
			VoteNagToast.Urgency urgency = selectUrgency(i);
			return i % urgency.period == 0 ? Optional.of(urgency) : Optional.empty();
		}

		private static VoteNagToast.Urgency selectUrgency(int i) {
			for (VoteNagToast.Urgency urgency : values()) {
				i -= urgency.phaseDuration;
				if (i < 0) {
					return urgency;
				}
			}

			return WHY_ARE_YOU_NOT_DOING_IT;
		}
	}
}
