package com.bobotweaks.dustandore.events;

import com.bobotweaks.dustandore.ForgeMod;
import com.bobotweaks.dustandore.events.RightClickBlockEvent;
import com.bobotweaks.dustandore.core.CrushingInteraction;

import java.lang.reflect.Method;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ForgeMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeRightClickBlock {

    private ForgeRightClickBlock() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
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
            invokeEnumResult(event, "setUseItem", "DENY");
            invokeEnumResult(event, "setUseBlock", "DENY");
            invokeBoolean(event, "setCanceled", true);
            invokeNoArg(event, "cancel");

            if (!event.getLevel().isClientSide()) {
                CrushingInteraction.execute(event.getLevel(), player.getX(), player.getY(), player.getZ(), player);
            }
        }
    }

    private static void invokeEnumResult(Object target, String methodName, String constantName) {
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

    private static void invokeBoolean(Object target, String methodName, boolean value) {
        Method method = findMethod(target.getClass(), methodName, 1, boolean.class);
        if (method == null)
            return;
        try {
            method.invoke(target, value);
        } catch (Throwable ignored) {
        }
    }

    private static void invokeNoArg(Object target, String methodName) {
        Method method = findMethod(target.getClass(), methodName, 0);
        if (method == null)
            return;
        try {
            method.invoke(target);
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
