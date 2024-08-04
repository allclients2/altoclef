package adris.altoclef.multiversion;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

public class ServerInfoVer {
    private final ClientPlayNetworkHandler networkHandler;

    public ServerInfoVer(ClientPlayNetworkHandler networkHandler) {
        this.networkHandler = networkHandler;
    }

    public String getAddress() {
        final var inetSocketAddress = (InetSocketAddress) networkHandler.getConnection().getAddress();
        return inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
    }
}
