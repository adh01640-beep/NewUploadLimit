package com.aliucord.plugins;

import android.content.Context;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;

import java.lang.reflect.Method;

public class NewUploadLimit extends Plugin {

    public static final long FREE_LIMIT_BYTES = 20971520L; // 20 MB

    @Override
    public void start(Context context) throws Throwable {
        patchUploadLimitResolver();
        patchStoreStreamFallback();
    }

    private void patchUploadLimitResolver() {
        try {
            Class<?> storeMediaClass = Class.forName("com.discord.stores.StoreMediaSettings");
            patcher.patch(
                storeMediaClass,
                "getMaxUploadSize",
                new Class<?>[] { boolean.class },
                new Hook(callFrame -> {
                    Object res = callFrame.getResult();
                    if (res instanceof Long) {
                        long originalLimit = (Long) res;
                        if (originalLimit <= 10485760L) {
                            callFrame.setResult(FREE_LIMIT_BYTES);
                        }
                    }
                })
            );
        } catch (Throwable ignored) {}
    }

    private void patchStoreStreamFallback() {
        try {
            Class<?> attachmentUtils = Class.forName("com.discord.utilities.attachments.AttachmentUtils");
            for (Method method : attachmentUtils.getDeclaredMethods()) {
                if (method.getReturnType().equals(boolean.class)
                        && method.getParameterTypes().length >= 2
                        && method.getParameterTypes()[0].equals(long.class)) {

                    patcher.patch(method, new Hook(callFrame -> {
                        long fileSize = (long) callFrame.args[0];
                        long allowedLimit = (long) callFrame.args[1];
                        long targetLimit = Math.max(allowedLimit, FREE_LIMIT_BYTES);
                        callFrame.setResult(fileSize > targetLimit);
                    }));
                }
            }
        } catch (Throwable e) {
            logger.error("Failed to patch upload limit check", e);
        }
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
