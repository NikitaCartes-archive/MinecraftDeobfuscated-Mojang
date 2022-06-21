/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.authlib.minecraft.BanDetails;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.time.Duration;
import java.time.Instant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;

@Environment(value=EnvType.CLIENT)
public class BanNoticeScreen {
    public static final String URL_MODERATION = "https://aka.ms/mcjavamoderation";
    private static final Component TEMPORARY_BAN_TITLE = Component.translatable("gui.banned.title.temporary").withStyle(ChatFormatting.BOLD);
    private static final Component PERMANENT_BAN_TITLE = Component.translatable("gui.banned.title.permanent").withStyle(ChatFormatting.BOLD);

    public static ConfirmLinkScreen create(BooleanConsumer booleanConsumer, BanDetails banDetails) {
        return new ConfirmLinkScreen(booleanConsumer, BanNoticeScreen.getBannedTitle(banDetails), BanNoticeScreen.getBannedScreenText(banDetails), URL_MODERATION, CommonComponents.GUI_ACKNOWLEDGE, true);
    }

    private static Component getBannedTitle(BanDetails banDetails) {
        return BanNoticeScreen.isTemporaryBan(banDetails) ? TEMPORARY_BAN_TITLE : PERMANENT_BAN_TITLE;
    }

    private static Component getBannedScreenText(BanDetails banDetails) {
        return Component.translatable("gui.banned.description", BanNoticeScreen.getBanReasonText(banDetails), BanNoticeScreen.getBanStatusText(banDetails), ConfirmLinkScreen.confirmMessage(true, URL_MODERATION));
    }

    private static Component getBanReasonText(BanDetails banDetails) {
        Component component = null;
        String string = banDetails.reason();
        if (StringUtils.isNumeric(string)) {
            component = ReportReason.getTranslationById(Integer.parseInt(string));
        }
        if (component != null) {
            return Component.translatable("gui.banned.description.reason", ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withBold(true)));
        }
        return Component.translatable("gui.banned.description.unknownreason");
    }

    private static Component getBanStatusText(BanDetails banDetails) {
        if (BanNoticeScreen.isTemporaryBan(banDetails)) {
            Component component = BanNoticeScreen.getBanDurationText(banDetails);
            return Component.translatable("gui.banned.description.temporary", Component.translatable("gui.banned.description.temporary.duration", component).withStyle(ChatFormatting.BOLD));
        }
        return Component.translatable("gui.banned.description.permanent").withStyle(ChatFormatting.BOLD);
    }

    private static Component getBanDurationText(BanDetails banDetails) {
        Duration duration = Duration.between(Instant.now(), banDetails.expires());
        long l = duration.toHours();
        if (l > 72L) {
            return CommonComponents.days(duration.toDays());
        }
        if (l < 1L) {
            return CommonComponents.minutes(duration.toMinutes());
        }
        return CommonComponents.hours(duration.toHours());
    }

    private static boolean isTemporaryBan(BanDetails banDetails) {
        return banDetails.expires() != null;
    }
}

