package net.bereshitweb.stafftool.utils;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;

public interface StaffToolUtils {

    String CHECKINVKEY = "CHECKINV.KEY";

    static String getCurrencies(HabboInfo habboInfo) {
        String currencies = "";
        currencies += "<b>Currencies</b>\r";
        currencies += "<b>Credits: </b>" + habboInfo.getCredits() + "\r";
        currencies += "<b>Duckets: </b>" + habboInfo.getCurrencyAmount(0) + "\r";
        currencies += "<b>Diamonds: </b>" + habboInfo.getCurrencyAmount(5) + "\r\n";
        return currencies;
    }

    static TIntObjectHashMap<HabboItem> getOfflineHabboItems(int habboId) {
        TIntObjectHashMap<HabboItem> itemsList = new TIntObjectHashMap<>();

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM items WHERE room_id = ? AND user_id = ?")) {
            statement.setInt(1, 0);
            statement.setInt(2, habboId);
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    try {
                        HabboItem item = Emulator.getGameEnvironment().getItemManager().loadHabboItem(set);

                        if (item != null) {
                            itemsList.put(set.getInt("id"), item);
                        } else {
                            Emulator.getLogging().logErrorLine("[StaffTool] Can't load item with ID: " + set.getInt("id"));
                        }
                    } catch (SQLException e) {
                        Emulator.getLogging().logSQLException(e);
                    }
                }
            }
        } catch (SQLException e) {
            Emulator.getLogging().logSQLException(e);
        }

        return itemsList;
    }


    static int countOfflineHabboItem(int habboId, int itemId) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) as count FROM items WHERE room_id = 0 AND user_id = ? AND item_id = ?")) {
            statement.setInt(1, habboId);
            statement.setInt(2, itemId);
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    try {
                        return set.getInt("count");
                    } catch (SQLException e) {
                        Emulator.getLogging().logSQLException(e);
                    }
                }
            }
        } catch (SQLException e) {
            Emulator.getLogging().logSQLException(e);
        }
        return -1;
    }

    static Result getCount(int habboId, int itemId) {
        int count = countOfflineHabboItem(habboId, itemId);
        if(count != -1)
            return new Result(Emulator.getGameEnvironment().getItemManager().getItem(itemId).getFullName(), itemId, count);
        else
            return null;
    }

    static String getItems(int habboId) {
        ArrayList<Result> items = new ArrayList<>();
        for(String item : Emulator.getConfig().getValue("stafftool.cmd_wealth.item_ids").split(";")) {
            try {
                int itemId = Integer.parseInt(item);
                Result result = getCount(habboId, itemId);
                if(result != null && result.getCount() > 0) items.add(result);
            } catch (NumberFormatException ex) {
                Emulator.getLogging().logErrorLine("[StaffTool] This Item ID is not valid: " + item);
            }
        }
        items.sort((r1, r2) -> r2.getCount() - r1.getCount());
        StringBuilder itemsCount = new StringBuilder();
        itemsCount.append("<b>Furni Counts</b>\r");
        for(Result r : items) {
            itemsCount.append(r.getItemName().toUpperCase()).append(" (ID:").append(r.getItemId()).append(") = <b>").
                    append(r.getCount()).append("</b>x").append("\r");
        }
        itemsCount.append("\n");
        return itemsCount.toString();
    }


}
