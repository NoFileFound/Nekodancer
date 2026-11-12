package org.nekodancer.modules;

// Imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.nekodancer.modules.lua.LuaNekoTable;
import org.psframework.utils.PSTimer;
import org.nekodancer.GamePlayer;
import org.nekodancer.GameRoom;
import org.nekodancer.GameServer;
import org.nekodancer.modules.impl.ModuleRoom;

// Packets
import org.nekodancer.modules.lua.debug.LuaTraceback;
import org.nekodancer.packets.send.lua.*;

public final class ModuleLua extends ModuleRoom {
    private final ExecutorService luaExecutor;
    private final Map<Integer, PSTimer> luaTimers;
    private final List<String> disableChatCommandListDisplay;
    private final List<Object[]> pendentEvents;
    private volatile Globals luaGlobal;
    private volatile LuaValue luaDebug;
    private volatile GamePlayer luaAdmin;
    private volatile boolean isRunningLua;
    private volatile boolean isInitializingLua;
    private volatile boolean isDestroyed;
    private volatile long luaGeneration;
    public boolean isDisabledEventLog;

    /**
     * Creates a lua module.
     * @param server The game server.
     * @param room The game room.
     */
    public ModuleLua(GameServer server, GameRoom room) {
        super(server, room);
        this.luaExecutor = Executors.newSingleThreadExecutor();
        this.luaTimers = new ConcurrentHashMap<>();
        this.disableChatCommandListDisplay = new ArrayList<>();
        this.pendentEvents = new ArrayList<>();
    }

    /**
     * Checks if a lua is running in this room.
     * @return True if a lua is running or else False.
     */
    public boolean getIsRunningLua() {
        return this.isRunningLua;
    }

    /**
     * Gets the lua's disableChatCommandDisplay commands.
     * @return The lua's disableChatCommandDisplay commands.
     */
    public List<String> getDisableChatCommandListDisplay() {
        return this.disableChatCommandListDisplay;
    }

    /**
     * Executes a lua script in the current room.
     * @param luaScript The lua script to execute.
     * @param player The person who executed the script.
     */
    public synchronized void executeScript(String luaScript, GamePlayer player) {
        if (this.isDestroyed)
            return;

        if (this.isRunningLua || this.isInitializingLua)
            this.sendRoomStopScript();

        long generation = ++this.luaGeneration;
        synchronized (this.pendentEvents) {
            this.pendentEvents.clear();
        }

        this.isRunningLua = false;
        this.isInitializingLua = true;
        this.luaAdmin = player;
        this.luaGlobal = JsePlatform.standardGlobals();
        this.luaGlobal.load(new DebugLib());
        this.luaDebug = this.luaGlobal.get("debug");
        this.luaGlobal.load(new LuaNekoTable(this.room));
        for (GamePlayer client : this.room.getPlayers().values())
            this.sendLuaUpdatePlayerInfo(client, true);

        this.luaGlobal.get("debug").set("traceback", new LuaTraceback(this.luaDebug));
        player.luaThread = new Thread(() -> {
            long startTime = System.nanoTime();
            LuaValue debug = this.luaDebug;

            try {
                LuaValue hook = new VarArgFunction() {
                    @Override
                    public Varargs invoke(Varargs args) {
                        if (generation != luaGeneration || isDestroyed)
                            throw new LuaError("Lua script stopped.");

                        if (System.nanoTime() - startTime > 4_000_000_000L)
                            throw new LuaError("Lua destroyed : Runtime too long!");

                        return LuaValue.NONE;
                    }
                };

                debug.get("sethook").invoke(LuaValue.varargsOf(new LuaValue[] {hook, LuaValue.valueOf(""), LuaValue.valueOf(1000)}));
                this.luaGlobal.load(luaScript).call();
                if (generation != this.luaGeneration || this.isDestroyed)
                    return;

                synchronized (this) {
                    if (generation != this.luaGeneration || this.isDestroyed)
                        return;

                    this.isRunningLua = true;
                    this.isInitializingLua = false;
                    synchronized (this.pendentEvents) {
                        for (Object[] object : this.pendentEvents)
                            this.callEvent((String) object[0], (Object[]) object[1]);

                        this.pendentEvents.clear();
                    }

                    if (generation != this.luaGeneration)
                        return;

                    this.room.broadcastPacket(new PacketSendInitializeLuaRoom());
                }

                long endTime = (System.nanoTime() - startTime) / 1_000_000L;
                this.sendLuaMessage("<V>[" + this.room.getRoomName() + "]</V> [" + player.getPlayerName() + "] Lua script loaded in " + endTime + " ms (4000 max)");
            } catch (Exception error) {
                if (generation == this.luaGeneration && !this.isDestroyed) {
                    this.sendLuaMessage("<V>[" + this.room.getRoomName() + "]</V> Init Error : " + player.getPlayerName() + ".lua : " + error.getMessage());
                    this.sendRoomStopScript();
                }
            } finally {
                debug.get("sethook").call();
            }
        });

        player.luaThread.start();
    }

    /**
     * Handles when the player joins a room.
     * @param player The player's instance.
     */
    public void handleEventNewPlayer(GamePlayer player) {
        if (this.isRunningLua) {
            player.broadcastPacket(new PacketSendInitializeLuaRoom());
            this.sendLuaUpdatePlayerInfo(player, true);
            this.callEvent("eventNewPlayer", player.getPlayerName());
        }
    }

    /**
     * Handles whe the player leaves the room.
     * @param player The player's instance.
     */
    public void handleEventLeavePlayer(GamePlayer player) {
        if (this.isRunningLua) {
            this.sendLuaUpdatePlayerInfo(player, false);
            this.callEvent("eventPlayerLeft", player.getPlayerName());
        }
    }

    /**
     * Handles when the player dies.
     * @param player The player's instance
     */
    public void handleEventPlayerDeath(GamePlayer player) {
        this.sendLuaUpdatePlayerInfo(player, true);
        this.callEvent("eventPlayerDied", player.getPlayerName());
    }

    /**
     * Handles when the player send a chat command.
     * @param player The player's name.
     * @param message The chat's command.
     */
    public void handleChatCommand(GamePlayer player, String message) {
        this.callEvent("eventChatCommand", player.getPlayerName(), message);
    }

    /**
     * Handles when the player send a chat message.
     * @param player The player's name.
     * @param message The chat's message.
     */
    public void handleChatMessage(GamePlayer player, String message) {
        this.callEvent("eventChatMessage", player.getPlayerName(), message);
    }

    /**
     * Handles when the player presses a key on the keyboard.
     * @param player The player's instance.
     * @param keyCode The code of the typed key.
     * @param down Whether it was the press event, or the release one.
     * @param xPos The horizontal coordinate of the player when he or she typed the key.
     * @param yPos The vertical coordinate of the player when he or she typed the key.
     */
    public void handleKeyboard(GamePlayer player, int keyCode, boolean down, int xPos, int yPos) {
        this.callEvent("eventKeyboard", player.getPlayerName(), keyCode, down, xPos, yPos);
    }

    /**
     * Handles when the player clicks with the mouse.
     * @param player The player's instance.
     * @param posX The X's position.
     * @param posY The Y's position.
     */
    public void handleMouse(GamePlayer player, int posX, int posY) {
        this.callEvent("eventMouse", player.getPlayerName(), posX, posY);
    }

    /**
     * Handles when the player answers a popup.
     * @param player The player's instance.
     * @param popupId The popup's id.
     * @param answer The answer.
     */
    public void handlePopupAnswer(GamePlayer player, int popupId, String answer) {
        this.callEvent("eventPopupAnswer", popupId, player.getPlayerName(), answer);
    }

    /**
     * Handles when the player clicks text event.
     * @param player The player's instance.
     * @param textAreaId The text area's id.
     * @param eventName The event's name.
     */
    public void handleTextAreaCallback(GamePlayer player, int textAreaId, String eventName) {
        this.callEvent("eventTextAreaCallback", textAreaId, player.getPlayerName(), eventName);
    }

    /**
     * Sends a #lua message.
     * @param message The message to send.
     */
    public void sendLuaMessage(String message) {
        if (this.luaAdmin == null)
            return;

        this.luaAdmin.broadcastPacket(new PacketSendLuaMessage(message));
    }

    /**
     * Handles when the script gets stopped in the room.
     */
    public synchronized void sendRoomStopScript() {
        this.luaGeneration++;
        this.isRunningLua = false;
        this.isInitializingLua = false;
        if (this.luaAdmin != null && this.luaAdmin.luaThread != null)
            this.luaAdmin.luaThread.interrupt();

        this.luaAdmin = null;
        this.room.maximumPlayers = 0;
        this.disableChatCommandListDisplay.clear();
        for (PSTimer timer : this.luaTimers.values())
            timer.cancelTask(false);

        this.luaTimers.clear();
        synchronized (this.pendentEvents) {
            this.pendentEvents.clear();
        }

        this.room.broadcastPacket(new PacketSendDestructLuaRoom());
        this.room.sendChangeMap();
    }

    /**
     * Handles when the room gets deleted.
     */
    public synchronized void sendRoomDestruction() {
        this.isDestroyed = true;
        this.luaGeneration++;
        this.isRunningLua = false;
        this.isInitializingLua = false;
        if (this.luaAdmin != null && this.luaAdmin.luaThread != null)
            this.luaAdmin.luaThread.interrupt();

        this.luaAdmin = null;
        for (PSTimer timer : this.luaTimers.values())
            timer.cancelTask(false);

        this.luaTimers.clear();
        synchronized (this.pendentEvents) {
            this.pendentEvents.clear();
        }

        this.luaExecutor.shutdownNow();
    }

    /**
     * Calls the given event.
     * @param eventName The event's name.
     * @param args The event's arguments.
     */
    private synchronized void callEvent(String eventName, Object... args) {
        if (this.isDestroyed)
            return;

        if (!this.isRunningLua) {
            if (this.isInitializingLua) {
                synchronized (this.pendentEvents) {
                    if (this.isInitializingLua && !this.isDestroyed)
                        this.pendentEvents.add(new Object[] {eventName, args});
                }
            }

            return;
        }

        long generation = this.luaGeneration;

        try {
            this.luaExecutor.execute(() -> {
                if (!this.isRunningLua || generation != this.luaGeneration || this.isDestroyed)
                    return;

                LuaValue debug = this.luaDebug;
                Globals globals = this.luaGlobal;
                if (debug == null || globals == null)
                    return;

                long startTime = System.nanoTime();

                try {
                    LuaValue event = globals.get(eventName);
                    if (!event.isfunction())
                        return;

                    LuaValue[] luaArgs = Arrays.stream(args).map(arg -> arg instanceof Integer ? LuaValue.valueOf((Integer) arg) : arg instanceof String ? LuaValue.valueOf((String) arg) : arg instanceof Boolean ? LuaValue.valueOf((Boolean) arg) : arg instanceof Long ? LuaInteger.valueOf((Long) arg) : (LuaValue) arg).toArray(LuaValue[]::new);
                    LuaValue hook = new VarArgFunction() {
                        private boolean warned = false;

                        @Override
                        public Varargs invoke(Varargs hookArgs) {
                            if (generation != luaGeneration || isDestroyed)
                                throw new LuaError("Lua script stopped.");

                            long elapsedTime = (System.nanoTime() - startTime) / 1_000_000L;
                            if (elapsedTime > 4000)
                                throw new LuaError("Lua destroyed : Runtime too long!");

                            if (elapsedTime > 60 && !warned) {
                                warned = true;
                                sendLuaMessage("<V>[" + room.getRoomName() + "]</V> Warning: " + "Runtime can't exceed 60 ms in 4 seconds! Currently: " + elapsedTime + " ms.");
                            }

                            return LuaValue.NONE;
                        }
                    };

                    debug.get("sethook").invoke(LuaValue.varargsOf(new LuaValue[] {hook, LuaValue.valueOf(""), LuaValue.valueOf(1000)}));
                    event.invoke(LuaValue.varargsOf(luaArgs));
                    if (generation != this.luaGeneration || this.isDestroyed)
                        return;

                    long endTime = (System.nanoTime() - startTime) / 1_000_000L;
                    if (!this.isDisabledEventLog)
                        this.sendLuaMessage("Event <V>" + eventName + "</V> : " + endTime + " ms");

                } catch (Exception error) {
                    if (generation == this.luaGeneration && !this.isDestroyed) {
                        this.sendLuaMessage("Runtime Error : " + error.getMessage());
                        this.sendRoomStopScript();
                    }
                } finally {
                    debug.get("sethook").call();
                }
            });
        } catch (RejectedExecutionException ignored) {}
    }

    /**
     * Updates the player's in the lua table.
     * @param player The player's instance.
     */
    private void sendLuaUpdatePlayerInfo(GamePlayer player, boolean toAdd) {
        try {
            if (this.luaGlobal == null)
                return;

            LuaValue playerList = this.luaGlobal.get("tfm").get("get").get("room").get("playerList");
            if (!playerList.istable())
                return;

            String playerName = player.getPlayerName();
            if (!toAdd) {
                playerList.set(playerName, LuaValue.NIL);
                return;
            }

            LuaTable table = new LuaTable();
            table.set("y", LuaValue.valueOf(player.positionY));
            table.set("x", LuaValue.valueOf(player.positionX));
            table.set("id", LuaValue.valueOf(player.getSessionId()));
            table.set("isDead", LuaValue.valueOf(player.isDead));
            table.set("score", LuaValue.valueOf(player.playerScore));
            table.set("registrationDate", LuaValue.valueOf(player.getPlayerData().getCreatedDate()));
            table.set("playerName", LuaValue.valueOf(playerName));
            table.set("community", LuaValue.valueOf(player.getPlayerCommunity()));
            table.set("tribeName", LuaValue.valueOf(player.getPlayerData().tribeName));
            playerList.set(playerName, table);
        } catch (LuaError ignored) {}
    }
}