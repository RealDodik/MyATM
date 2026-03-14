package com.glbank.myatm.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TerminalResponsePacket {

    public final boolean success;
    public final String message;

    public TerminalResponsePacket(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static void encode(TerminalResponsePacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.success);
        buf.writeUtf(pkt.message, 256);
    }

    public static TerminalResponsePacket decode(FriendlyByteBuf buf) {
        boolean success = buf.readBoolean();
        String message = buf.readUtf(256);
        return new TerminalResponsePacket(success, message);
    }

    public static void handle(TerminalResponsePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof com.glbank.myatm.screen.TerminalScreen terminalScreen) {
                terminalScreen.handleResponse(pkt);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
