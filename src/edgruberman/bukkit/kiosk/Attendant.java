package edgruberman.bukkit.kiosk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class Attendant implements Listener {

    private final List<Kiosk> kiosks = new ArrayList<Kiosk>();
    private final Map<String, Function> functions = new TreeMap<String, Function>(String.CASE_INSENSITIVE_ORDER);

    public List<Kiosk> getKiosks() {
        return this.kiosks;
    }

    public void hire(final Kiosk kiosk) {
        this.kiosks.add(kiosk);
    }

    public void train(final Function function) {
        this.functions.put(function.getName(), function);
    }

    public Function matchFunction(final String name) {
        return this.functions.get(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent change) {
        if (!change.getPlayer().hasPermission("kiosk.create")) return;

        final Sign state = (Sign) change.getBlock().getState();

        for (int i = 0; i < change.getLines().length; i++)
            state.setLine(i, change.getLines()[i]);

        for (final Kiosk kiosk : this.kiosks)
            if (kiosk.hasTrigger(state)) {
                if (kiosk.create(change.getPlayer(), state))
                    for (int i = 0; i < state.getLines().length; i++)
                        change.setLine(i, state.getLines()[i]);

                return;
            }
    }

}
