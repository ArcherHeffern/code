package frog.dptb.client.listeners;

import com.mojang.authlib.GameProfile;
import frog.dptb.client.context.DPTBContext;
import frog.dptb.client.context.DPTBContextProvider;
import frog.dptb.client.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class ChatMessageListener implements ClientReceiveMessageEvents.Chat {

    private static DPTBContext CONTEXT = DPTBContextProvider.get();

    private boolean isFromGod(GameProfile sender) {
        return false;
    }

    @Override
    public void onReceiveChatMessage(Component message, @Nullable PlayerChatMessage playerChatMessage, @Nullable GameProfile sender, ChatType.Bound boundChatType, Instant timeStamp) {
        if (playerChatMessage != null) {
            CONTEXT.getLogger().info(Utils.stringish(sender != null ? sender.name() : "(null)") + " " + sender.id() + " " + playerChatMessage.toString());
        } else {
            CONTEXT.getLogger().info("PlayerChatMessage was null");
        }
    }
}
