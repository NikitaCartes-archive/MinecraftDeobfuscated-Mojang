/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.authlib.yggdrasil.ServicesKeyInfo;
import com.mojang.logging.LogUtils;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import net.minecraft.util.SignatureUpdater;
import org.slf4j.Logger;

public interface SignatureValidator {
    public static final SignatureValidator NO_VALIDATION = (signatureUpdater, bs) -> true;
    public static final Logger LOGGER = LogUtils.getLogger();

    public boolean validate(SignatureUpdater var1, byte[] var2);

    default public boolean validate(byte[] bs, byte[] cs) {
        return this.validate(output -> output.update(bs), cs);
    }

    private static boolean verifySignature(SignatureUpdater signatureUpdater, byte[] bs, Signature signature) throws SignatureException {
        signatureUpdater.update(signature::update);
        return signature.verify(bs);
    }

    public static SignatureValidator from(PublicKey publicKey, String string) {
        return (signatureUpdater, bs) -> {
            try {
                Signature signature = Signature.getInstance(string);
                signature.initVerify(publicKey);
                return SignatureValidator.verifySignature(signatureUpdater, bs, signature);
            } catch (Exception exception) {
                LOGGER.error("Failed to verify signature", exception);
                return false;
            }
        };
    }

    public static SignatureValidator from(ServicesKeyInfo servicesKeyInfo) {
        return (signatureUpdater, bs) -> {
            Signature signature = servicesKeyInfo.signature();
            try {
                return SignatureValidator.verifySignature(signatureUpdater, bs, signature);
            } catch (SignatureException signatureException) {
                LOGGER.error("Failed to verify Services signature", signatureException);
                return false;
            }
        };
    }
}

