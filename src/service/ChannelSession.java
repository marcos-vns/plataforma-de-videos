package service;

import model.Channel; // Supondo que sua classe se chame Channel

public class ChannelSession {

    private static Channel currentChannel;

    // Define qual canal está sendo gerenciado agora
    public static void setChannel(Channel channel) {
        currentChannel = channel;
    }

    // Retorna o canal atual para usar nos DAOs/Services
    public static Channel getChannel() {
        return currentChannel;
    }

    // Limpa a seleção (ex: quando volta para a Home ou sai do "Studio")
    public static void close() {
        currentChannel = null;
    }

    // Útil para habilitar/desabilitar botões na interface
    public static boolean hasSelectedChannel() {
        return currentChannel != null;
    }
}