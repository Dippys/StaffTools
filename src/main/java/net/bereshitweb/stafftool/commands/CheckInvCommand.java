package net.bereshitweb.stafftool.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.outgoing.inventory.InventoryItemsComposer;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.bereshitweb.stafftool.utils.StaffToolUtils;

import java.util.NoSuchElementException;

public class CheckInvCommand extends Command {
    public CheckInvCommand() {
        super("cmd_checkinventory", Emulator.getTexts().getValue("commands.keys.cmd_checkinventory").split(";"));
    }

    public boolean handle(GameClient gameClient, String[] strings) throws Exception {
        if(strings.length == 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_checkinventory.wrong_usage") +
                    Emulator.getTexts().getValue("commands.description.cmd_checkinventory"));
            return true;
        }
        int habboId;
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strings[1]);
        if(habbo == null) {
            HabboInfo habboInfo = HabboManager.getOfflineHabboInfo(strings[1]);
            if(habboInfo == null) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_checkinventory.user_not_found").replace("%user%", strings[1]));
                return true;
            } else
                habboId = habboInfo.getId();
        } else
            habboId = habbo.getHabboInfo().getId();
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.success.cmd_checkinventory.inventory_loading"));
        if(gameClient.getHabbo().getHabboStats().cache.containsKey(StaffToolUtils.CHECKINVKEY)) {
            String actual = (String) gameClient.getHabbo().getHabboStats().cache.get(StaffToolUtils.CHECKINVKEY);
            if(gameClient.getHabbo().getHabboInfo().getUsername().equals(strings[1])) {
                gameClient.getHabbo().getHabboStats().cache.remove(StaffToolUtils.CHECKINVKEY);
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.success.cmd_checkinventory.inventory_set.yourself")
                        .replace("%yourself%", gameClient.getHabbo().getHabboInfo().getUsername()));
                Emulator.getThreading().run(() -> setInventory(gameClient), 500);
            } else if(!strings[1].equals(actual)) {
                gameClient.getHabbo().getHabboStats().cache.put(StaffToolUtils.CHECKINVKEY, strings[1]);
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.success.cmd_checkinventory.inventory_set.other").replace("%user%", strings[1]));
                Emulator.getThreading().run(() -> setInventory(gameClient, habboId), 500);
            } else {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_checkinventory.already_set").replace("%user%", strings[1]));
            }
        } else {
            if(gameClient.getHabbo().getHabboInfo().getUsername().equals(strings[1])) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_checkinventory.already_set_yourself"));
            } else {
                gameClient.getHabbo().getHabboStats().cache.put(StaffToolUtils.CHECKINVKEY, strings[1]);
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.success.cmd_checkinventory.inventory_set.other").replace("%user%", strings[1]));
                Emulator.getThreading().run(() -> setInventory(gameClient, habboId), 500);
            }
        }
        return true;
    }

    public static void setInventory(GameClient gameClient) {
        setInventory(gameClient, gameClient.getHabbo().getHabboInfo().getId());
    }

    public static void setInventory(GameClient gameClient, int habboId) {
        TIntObjectHashMap<HabboItem> offlineItems = StaffToolUtils.getOfflineHabboItems(habboId);
        int pages = (int) Math.ceil((double) StaffToolUtils.getOfflineHabboItems(habboId).size() / 1000.0);

        if (pages == 0) {
            pages = 1;
        }
            TIntObjectMap<HabboItem> items = new TIntObjectHashMap<>();
            TIntObjectIterator<HabboItem> iterator = offlineItems.iterator();
            int count = 0;
            int page = 0;
            for (int i = offlineItems.size(); i-- > 0; ) {
                if (count == 0) {
                    page++;
                }

                try {
                    iterator.advance();
                    items.put(iterator.key(), iterator.value());
                    count++;
                } catch (NoSuchElementException e) {
                    Emulator.getLogging().logErrorLine(e);
                    break;
                }

                if (count == 1000) {
                    gameClient.sendResponse(new InventoryItemsComposer(page, pages, items));
                    count = 0;
                    items.clear();
                }
            }
            gameClient.sendResponse(new InventoryItemsComposer(page, pages, items));
    }
}
