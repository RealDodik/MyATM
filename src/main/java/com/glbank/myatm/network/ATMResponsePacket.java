package com.glbank.myatm.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ATMResponsePacket {

    public final boolean success;
    public final String cardNumber;
    public final String cvv;
    public final String message;

    public ATMResponsePacket(boolean success, String cardNumber, String cvv, String message) {
        this.success = success;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.message = message;
    }

    public static void encode(ATMResponsePacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.success);
        buf.writeUtf(pkt.cardNumber, 32);
        buf.writeUtf(pkt.cvv, 8);
        buf.writeUtf(pkt.message, 256);
    }

    public static ATMResponsePacket decode(FriendlyByteBuf buf) {
        boolean success = buf.readBoolean();
        String cardNumber = buf.readUtf(32);
        String cvv = buf.readUtf(8);
        String message = buf.readUtf(256);
        return new ATMResponsePacket(success, cardNumber, cvv, message);
    }

    public static void handle(ATMResponsePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof com.glbank.myatm.screen.ATMScreen atmScreen) {
                atmScreen.handleResponse(pkt);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
