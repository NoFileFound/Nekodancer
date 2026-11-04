package org.nekodancer.packets;

public final class PacketOpcodes {
    public static final int SEND_HANDSHAKE = 6659;
    public static final int RECV_CREATE_ACCOUNT = 6663;
    public static final int RECV_LOGIN_PLAYER = 6664;
    public static final int RECV_CREATE_ACCOUNT_CAPTCHA_GEN = 6676;
    public static final int SEND_CREATE_ACCOUNT_CAPTCHA_GEN = 6676;
    public static final int RECV_HEARTBEAT = 6682;
    public static final int RECV_HANDSHAKE = 7169;
    public static final int RECV_GAME_ERROR_LOG = 7172;
    public static final int RECV_SYSTEM_INFORMATION = 7185;
    public static final int RECV_SET_LANGUAGE = 45057;
    public static final int RECV_LANGUAGE_LIST = 45058;
    public static final int SEND_SET_LANGUAGE = 45061;
    public static final int SEND_LANGUAGE_LIST = 45062;
    public static final int SEND_CLIENT_VERIFICATION = 45063;
    public static final int RECV_CLIENT_VERIFICATION = 45103;
}