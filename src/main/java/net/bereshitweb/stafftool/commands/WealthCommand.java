package net.bereshitweb.stafftool.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import net.bereshitweb.stafftool.utils.StaffToolUtils;

public class WealthCommand extends Command {
    public WealthCommand() {
        super("cmd_wealth", Emulator.getTexts().getValue("commands.keys.cmd_wealth").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] strings) throws Exception {
        if(strings.length == 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_wealth.wrong_usage") +
                    Emulator.getTexts().getValue("commands.description.cmd_wealth"));
            return true;
        }
        HabboInfo habboInfo;
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(strings[1]);
        if(habbo == null) {
            habboInfo = HabboManager.getOfflineHabboInfo(strings[1]);
            if(habboInfo == null) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_wealth.user_not_found").replace("%user%", strings[1]));
                return true;
            }
        } else habboInfo = habbo.getHabboInfo();

        String wealthMessage = "";
        wealthMessage += "<b>" + habboInfo.getUsername() + "'s Wealth:</b>\r\n";
        wealthMessage += StaffToolUtils.getCurrencies(habboInfo);
        wealthMessage += StaffToolUtils.getItems(habboInfo.getId());

        gameClient.getHabbo().alert(wealthMessage);
        return true;
    }
}
