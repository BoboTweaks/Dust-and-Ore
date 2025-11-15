package com.bobotweaks.dustandore.events;

import com.bobotweaks.dustandore.events.RightClickBlockEvent;
import com.bobotweaks.dustandore.core.CrushingInteraction;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import java.lang.reflect.Method;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class NeoforgeRightClickBlock {

    private NeoforgeRightClickBlock() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(NeoforgeRightClickBlock::onRightClickBlock);
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player == null)
            return;

        InteractionResult result = RightClickBlockEvent.onRightClickBlock(
            player, 
            event.getHand(), 
            event.getPos(),
            event.getHitVec()
        );

        if (result == InteractionResult.FAIL) {
            event.setCancellationResult(InteractionResult.FAIL);
            invokeTriState(event, "setUseItem", "FALSE");
            invokeTriState(event, "setUseBlock", "FALSE");
            event.setCanceled(true);

            if (!event.getLevel().isClientSide()) {
                CrushingInteraction.execute(event.getLevel(), player.getX(), player.getY(), player.getZ(), player);
            }
        }
    }

    private static void invokeTriState(Object target, String methodName, String constantName) {
        Method method = findMethod(target.getClass(), methodName, 1);
        if (method == null)
            return;
        Class<?> paramType = method.getParameterTypes()[0];
        Object value = resolveEnumConstant(paramType, constantName);
        if (value == null)
            return;
        try {
            method.invoke(target, value);
        } catch (Throwable ignored) {
        }
    }

    private static Method findMethod(Class<?> type, String name, int paramCount, Class<?>... exactTypes) {
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(name))
                    continue;
                if (method.getParameterCount() != paramCount)
                    continue;
                if (exactTypes != null && exactTypes.length == paramCount) {
                    Class<?>[] params = method.getParameterTypes();
                    boolean matches = true;
                    for (int i = 0; i < paramCount; i++) {
                        if (!params[i].equals(exactTypes[i])) {
                            matches = false;
                            break;
                        }
                    }
                    if (!matches)
                        continue;
                }
                method.setAccessible(true);
                return method;
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static Method findMethod(Class<?> type, String name, int paramCount) {
        return findMethod(type, name, paramCount, (Class<?>[]) null);
    }

    private static Object resolveEnumConstant(Class<?> type, String name) {
        if (type == null)
            return null;
        try {
            if (type.isEnum()) {
                Object[] constants = type.getEnumConstants();
                if (constants != null) {
                    for (Object constant : constants) {
                        if (constant != null && name.equals(((Enum<?>) constant).name()))
                            return constant;
                    }
                }
            } else {
                Method accessor = findMethod(type, "valueOf", 1, String.class);
                if (accessor != null)
                    return accessor.invoke(null, name);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}