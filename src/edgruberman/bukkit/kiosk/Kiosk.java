package edgruberman.bukkit.kiosk;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import edgruberman.bukkit.kiosk.messaging.Sender;

public abstract class Kiosk {

    public static final List<Integer> MATERIALS = Arrays.asList(Material.SIGN_POST.getId(), Material.WALL_SIGN.getId());

    protected final Attendant attendant;
    protected final String title;
    protected final Pattern trigger;

    protected Kiosk(final Attendant attendant, final String title, final String trigger) {
        this.attendant = attendant;
        this.title = title;
        this.trigger = Pattern.compile(trigger);
    }

    public boolean create(final Player player, final Sign state) {
        final Function function = this.getFunction(state);
        if (function == null) {
            Main.courier.send(player, "messages.unknownFunction", state.getLine(1));
            return false;
        }

        if (!function.canApply(player)) {
            Main.courier.send(player, "messages.applyDenied", function.getName());
            return false;
        }

        state.setLine(0, this.title);
        state.setLine(1, function.getName());
        if (!state.update()) {
            Main.courier.send(player, "messages.createFailed");
            return false;
        }

        Main.courier.send(player, "messages.createSuccess");
        return true;
    }

    public void describe(final Player player, final Sign state) {
        Main.courier.submit(new Sender(player), this.getFunction(state).draftDescription(this.getArguments(player, state)));
    }

    public void dispatch(final Player player, final Sign state) {
        Main.courier.getPlugin().getLogger().log(Level.FINER, "Kiosk used by {0} at {1}", new Object[] { player.getName(), state.getBlock() });
        this.getFunction(state).dispatch(this.getArguments(player, state));
    }

    public boolean hasTrigger(final Sign state) {
        return this.trigger.matcher(state.getLine(0)).find();
    }

    public boolean hasTitle(final Sign state) {
        return state.getLine(0).equals(this.title);
    }

    public Function getFunction(final Sign state) {
        return this.attendant.matchFunction(state.getLine(1));
    }

    protected void onPlayerInteract(final PlayerInteractEvent interaction) {
        if (interaction.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!Kiosk.MATERIALS.contains(interaction.getClickedBlock().getTypeId())) return;

        final Sign state = (Sign) interaction.getClickedBlock().getState();
        if (!this.hasTitle(state)) return;

        this.describe(interaction.getPlayer(), state);
    }

    /** # 1 = Player Name, 2 = Player Display Name, 3 = 3rd Sign Line, 4 = 4th Sign Line, 5 = World, 6 = Block X, 7 = Block Y, 8 = Block Z */
    public Object[] getArguments(final Player player, final Sign state) {
        final Object[] arguments = new Object[8];
        arguments[0] = player.getName();
        arguments[1] = player.getDisplayName();
        arguments[2] = state.getLine(2);
        arguments[3] = state.getLine(3);
        arguments[4] = player.getWorld().getName();
        arguments[5] = state.getX();
        arguments[6] = state.getY();
        arguments[7] = state.getZ();
        return arguments;
    }

}
