/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.exceptions.MinecraftClientHttpException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.mojang.datafixers.util.Unit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ThrowingComponent;

@Environment(value=EnvType.CLIENT)
public interface AbuseReportSender {
    public static AbuseReportSender create(ReportEnvironment reportEnvironment, UserApiService userApiService) {
        return new Services(reportEnvironment, userApiService);
    }

    public CompletableFuture<Unit> send(UUID var1, AbuseReport var2);

    public boolean isEnabled();

    default public AbuseReportLimits reportLimits() {
        return AbuseReportLimits.DEFAULTS;
    }

    @Environment(value=EnvType.CLIENT)
    public record Services(ReportEnvironment environment, UserApiService userApiService) implements AbuseReportSender
    {
        private static final Component SERVICE_UNAVAILABLE_TEXT = Component.translatable("gui.abuseReport.send.service_unavailable");
        private static final Component HTTP_ERROR_TEXT = Component.translatable("gui.abuseReport.send.http_error");
        private static final Component JSON_ERROR_TEXT = Component.translatable("gui.abuseReport.send.json_error");

        @Override
        public CompletableFuture<Unit> send(UUID uUID, AbuseReport abuseReport) {
            return CompletableFuture.supplyAsync(() -> {
                AbuseReportRequest abuseReportRequest = new AbuseReportRequest(1, uUID, abuseReport, this.environment.clientInfo(), this.environment.thirdPartyServerInfo(), this.environment.realmInfo());
                try {
                    this.userApiService.reportAbuse(abuseReportRequest);
                    return Unit.INSTANCE;
                } catch (MinecraftClientHttpException minecraftClientHttpException) {
                    Component component = this.getHttpErrorDescription(minecraftClientHttpException);
                    throw new CompletionException(new SendException(component, (Throwable)minecraftClientHttpException));
                } catch (MinecraftClientException minecraftClientException) {
                    Component component = this.getErrorDescription(minecraftClientException);
                    throw new CompletionException(new SendException(component, (Throwable)minecraftClientException));
                }
            }, Util.ioPool());
        }

        @Override
        public boolean isEnabled() {
            return this.userApiService.canSendReports();
        }

        private Component getHttpErrorDescription(MinecraftClientHttpException minecraftClientHttpException) {
            return Component.translatable("gui.abuseReport.send.error_message", minecraftClientHttpException.getMessage());
        }

        private Component getErrorDescription(MinecraftClientException minecraftClientException) {
            return switch (minecraftClientException.getType()) {
                default -> throw new IncompatibleClassChangeError();
                case MinecraftClientException.ErrorType.SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE_TEXT;
                case MinecraftClientException.ErrorType.HTTP_ERROR -> HTTP_ERROR_TEXT;
                case MinecraftClientException.ErrorType.JSON_ERROR -> JSON_ERROR_TEXT;
            };
        }

        @Override
        public AbuseReportLimits reportLimits() {
            return this.userApiService.getAbuseReportLimits();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SendException
    extends ThrowingComponent {
        public SendException(Component component, Throwable throwable) {
            super(component, throwable);
        }
    }
}

