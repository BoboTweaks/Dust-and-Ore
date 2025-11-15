package com.bobotweaks.dustandore.gameplay;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

public class Messages {
    public static void SendErrorMessage(Player player, int code, String ingredientId, int value) {
        String text;
        switch (code) {
            case -1000:
                text = "No crush recipe for " + ingredientId;
                break;
            case -1001:
                text = "Required " + value + " Crushing Power Hammer.";
                break;
            case -1002:
                text = ""; // Hammer is cooling down
                break;
            default:
                text = "";
                break;
        }
        if (text.equals(""))
            return;
        Component msg = Component.literal(text);
        player.displayClientMessage(msg, true);
    }
}
