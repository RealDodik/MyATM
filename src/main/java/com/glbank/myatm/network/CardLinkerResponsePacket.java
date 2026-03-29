package com.glbank.myatm.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CardLinkerResponsePacket {

    public final boolean success;
    public final String cardNumber;
    public final String cvv;
    public final String message;

    public CardLinkerResponsePacket(boolean success, String cardNumber, String cvv, String message) {
        this.success = success;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.message = message;
    }

    public static void encode(CardLinkerResponsePacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.success);
        buf.writeUtf(pkt.cardNumber, 32);
        buf.writeUtf(pkt.cvv, 8);
        buf.writeUtf(pkt.message, 256);
    }

    public static CardLinkerResponsePacket decode(FriendlyByteBuf buf) {
        return new CardLinkerResponsePacket(buf.readBoolean(), buf.readUtf(32),
                buf.readUtf(8), buf.readUtf(256));
    }

    public static void handle(CardLinkerResponsePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof com.glbank.myatm.screen.CardLinkerScreen cls) {
                cls.handleResponse(pkt);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
