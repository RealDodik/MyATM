package com.glbank.myatm.network;

import com.glbank.myatm.MyATMMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MyATMMod.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id = 0;

    public static void register() {
        // Client → Server
        CHANNEL.registerMessage(id++, ATMRequestPacket.class,
                ATMRequestPacket::encode, ATMRequestPacket::decode, ATMRequestPacket::handle);

        CHANNEL.registerMessage(id++, TerminalRequestPacket.class,
                TerminalRequestPacket::encode, TerminalRequestPacket::decode, TerminalRequestPacket::handle);

        // Server → Client
        CHANNEL.registerMessage(id++, ATMResponsePacket.class,
                ATMResponsePacket::encode, ATMResponsePacket::decode, ATMResponsePacket::handle);

        CHANNEL.registerMessage(id++, TerminalResponsePacket.class,
                TerminalResponsePacket::encode, TerminalResponsePacket::decode, TerminalResponsePacket::handle);
    }
}
