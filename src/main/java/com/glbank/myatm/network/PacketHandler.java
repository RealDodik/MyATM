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
        CHANNEL.registerMessage(id++, CardLinkerRequestPacket.class,
                CardLinkerRequestPacket::encode, CardLinkerRequestPacket::decode, CardLinkerRequestPacket::handle);

        CHANNEL.registerMessage(id++, TerminalRequestPacket.class,
                TerminalRequestPacket::encode, TerminalRequestPacket::decode, TerminalRequestPacket::handle);

        CHANNEL.registerMessage(id++, CardLinkerResponsePacket.class,
                CardLinkerResponsePacket::encode, CardLinkerResponsePacket::decode, CardLinkerResponsePacket::handle);

        CHANNEL.registerMessage(id++, TerminalResponsePacket.class,
                TerminalResponsePacket::encode, TerminalResponsePacket::decode, TerminalResponsePacket::handle);
    }
}
