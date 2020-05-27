package net.bereshitweb.stafftool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.CommandHandler;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.PacketManager;
import com.eu.habbo.messages.incoming.Incoming;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.HabboPlugin;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent;
import gnu.trove.map.hash.THashMap;
import net.bereshitweb.stafftool.commands.CheckInvCommand;
import net.bereshitweb.stafftool.commands.WealthCommand;
import net.bereshitweb.stafftool.events.OverrideCatalogBuyItemEvent;
import net.bereshitweb.stafftool.events.OverrideCatalogBuyItemAsGiftEvent;
import net.bereshitweb.stafftool.events.OverrideRoomPlaceItemEvent;
import net.bereshitweb.stafftool.events.OverrideTradeStartEvent;

import java.lang.reflect.Field;

public class StaffTool extends HabboPlugin implements EventListener {

    public void onEnable() throws Exception {
        Emulator.getPluginManager().registerEvents(this, this);
        Emulator.getLogging().logStart("[StaffTool] Enabling StaffTool Plugin...");
    }

    public void onDisable() throws Exception {
        Emulator.getLogging().logShutdownLine("[StaffTool] Stopped StaffTool Plugin!");
    }

    public boolean hasPermission(Habbo habbo, String s) {
        return false;
    }

    @EventHandler
    public static void onEmulatorLoaded(EmulatorLoadedEvent event) {
        initialize();
    }


    private static void initialize() {
        //CheckInventory related things
        Emulator.getTexts().register("commands.description.cmd_checkinventory", ":checkinventory <username>");
        Emulator.getTexts().register("commands.keys.cmd_checkinventory", "checkinventory");
        Emulator.getTexts().register("commands.error.cmd_checkinventory.user_not_found", "The user %user% doesnt exist.");
        Emulator.getTexts().register("commands.error.cmd_checkinventory.wrong_usage", "Wrong usage.");
        Emulator.getTexts().register("commands.error.cmd_checkinventory.already_set", "You have already set inventory to this user.");
        Emulator.getTexts().register("commands.error.cmd_checkinventory.already_set_yourself", "You have already set inventory to yourself.");
        Emulator.getTexts().register("commands.success.cmd_checkinventory.inventory_set.yourself", "Inventory set to: %yourself%");
        Emulator.getTexts().register("commands.success.cmd_checkinventory.inventory_set.other", "Inventory set to: %user%");
        Emulator.getTexts().register("commands.success.cmd_checkinventory.inventory_loading", "Inventory view loading...");
        Emulator.getTexts().register("checkinventory.error.cant_trade", "You cant trade this user.");
        Emulator.getTexts().register("checkinventory.error.trade_disabled", "Trade is disabled during inventory view.");
        Emulator.getTexts().register("checkinventory.error.placeitem_disabled", "Placing item is disabled during inventory view.");
        Emulator.getTexts().register("checkinventory.error.catalogbuy_disabled", "Buying item is disabled during inventory view.");
        CommandHandler.addCommand(new CheckInvCommand());
        hijackEvents();

        //WealthCommand related things
        Emulator.getTexts().register("commands.description.cmd_wealth", ":wealth <username>");
        Emulator.getTexts().register("commands.error.cmd_wealth.user_not_found", "The user %user% doesnt exist.");
        Emulator.getTexts().register("commands.error.cmd_wealth.wrong_usage", "Wrong usage.");
        Emulator.getTexts().register("commands.keys.cmd_wealth", "wealth");
        Emulator.getConfig().register("stafftool.cmd_wealth.item_ids", "1;2;3");
        CommandHandler.addCommand(new WealthCommand());

        Emulator.getLogging().logStart("[StaffTool] Started StaffTool Plugin!");
        Emulator.getLogging().logStart("[StaffTool] Plugin created by ItsGiuseppe#8237!");
    }

    public static void main(String[] args)
    {
        System.out.println("Don't run this seperately. Plugin created by ItsGiuseppe#8237!");
    }

    public static void hijackEvents() {
        PacketManager packetManager = Emulator.getGameServer().getPacketManager();
        Field f;
        THashMap<Integer, Class<? extends MessageHandler>> incoming;
        try {
            f = PacketManager.class.getDeclaredField("incoming");
            f.setAccessible(true);
            incoming = (THashMap<Integer, Class<? extends MessageHandler>>) f.get(packetManager);
            incoming.remove(Incoming.RoomPlaceItemEvent);
            incoming.remove(Incoming.TradeStartEvent);
            incoming.remove(Incoming.CatalogBuyItemEvent);
            incoming.remove(Incoming.CatalogBuyItemAsGiftEvent);
            Emulator.getGameServer().getPacketManager().registerHandler(Incoming.RoomPlaceItemEvent, OverrideRoomPlaceItemEvent.class);
            Emulator.getGameServer().getPacketManager().registerHandler(Incoming.TradeStartEvent, OverrideTradeStartEvent.class);
            Emulator.getGameServer().getPacketManager().registerHandler(Incoming.CatalogBuyItemEvent, OverrideCatalogBuyItemEvent.class);
            Emulator.getGameServer().getPacketManager().registerHandler(Incoming.CatalogBuyItemAsGiftEvent, OverrideCatalogBuyItemAsGiftEvent.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
