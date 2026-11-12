package org.nekodancer.modules.lua;

// Imports
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.nekodancer.GameRoom;
import org.nekodancer.modules.lua.debug.*;
import org.nekodancer.modules.lua.system.*;

public final class LuaNekoTable extends TwoArgFunction {
    private final GameRoom room;

    /**
     * Creates a new LuaTfmTable.
     * @param room The given room.
     */
    public LuaNekoTable(GameRoom room) {
        this.room = room;
    }

    @Override
    public LuaValue call(LuaValue name, LuaValue table) {
        table.set("debug", new LuaTable());
        table.get("debug").set("disableEventLog", new LuaDisableEventLog(this.room));
        table.get("debug").set("disableTimerLog", new LuaDisableTimerLog(this.room));
        table.get("debug").set("getCurrentThreadName", new LuaGetCurrentLuaThreadName());
        table.set("system", new LuaTable());
        table.get("system").set("exit", new LuaSystemExit(this.room));
        table.get("system").set("bindKeyboard", new LuaBindKeyboard(this.room));
        table.get("system").set("bindMouse", new LuaBindMouse(this.room));
        table.get("system").set("disableChatCommandDisplay", new LuaDisableChatCommandDisplay(this.room));
        return table;
    }
}