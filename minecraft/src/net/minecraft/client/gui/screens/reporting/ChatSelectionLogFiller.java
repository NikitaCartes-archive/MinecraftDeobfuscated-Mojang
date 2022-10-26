package net.minecraft.client.gui.screens.reporting;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReportContextBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageLink;

@Environment(EnvType.CLIENT)
public class ChatSelectionLogFiller {
	private final ChatLog log;
	private final ChatReportContextBuilder contextBuilder;
	private final Predicate<LoggedChatMessage.Player> canReport;
	@Nullable
	private SignedMessageLink previousLink = null;
	private int eventId;
	private int missedCount;
	@Nullable
	private PlayerChatMessage lastMessage;

	public ChatSelectionLogFiller(ReportingContext reportingContext, Predicate<LoggedChatMessage.Player> predicate) {
		this.log = reportingContext.chatLog();
		this.contextBuilder = new ChatReportContextBuilder(reportingContext.sender().reportLimits().leadingContextMessageCount());
		this.canReport = predicate;
		this.eventId = this.log.end();
	}

	public void fillNextPage(int i, ChatSelectionLogFiller.Output output) {
		int j = 0;

		while (j < i) {
			LoggedChatEvent loggedChatEvent = this.log.lookup(this.eventId);
			if (loggedChatEvent == null) {
				break;
			}

			int k = this.eventId--;
			if (loggedChatEvent instanceof LoggedChatMessage.Player player && !player.message().equals(this.lastMessage)) {
				if (this.acceptMessage(output, player)) {
					if (this.missedCount > 0) {
						output.acceptDivider(Component.translatable("gui.chatSelection.fold", this.missedCount));
						this.missedCount = 0;
					}

					output.acceptMessage(k, player);
					j++;
				} else {
					this.missedCount++;
				}

				this.lastMessage = player.message();
			}
		}
	}

	private boolean acceptMessage(ChatSelectionLogFiller.Output output, LoggedChatMessage.Player player) {
		PlayerChatMessage playerChatMessage = player.message();
		boolean bl = this.contextBuilder.acceptContext(playerChatMessage);
		if (this.canReport.test(player)) {
			this.contextBuilder.trackContext(playerChatMessage);
			if (this.previousLink != null && !this.previousLink.isDescendantOf(playerChatMessage.link())) {
				output.acceptDivider(Component.translatable("gui.chatSelection.join", player.profile().getName()).withStyle(ChatFormatting.YELLOW));
			}

			this.previousLink = playerChatMessage.link();
			return true;
		} else {
			return bl;
		}
	}

	@Environment(EnvType.CLIENT)
	public interface Output {
		void acceptMessage(int i, LoggedChatMessage.Player player);

		void acceptDivider(Component component);
	}
}
