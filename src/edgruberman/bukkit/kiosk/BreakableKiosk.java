package edgruberman.bukkit.kiosk;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.kiosk.messaging.Sender;

public class BreakableKiosk extends Kiosk implements Listener {

    public BreakableKiosk(final Attendant attendant, final String title, final String trigger) {
        super(attendant, title, trigger);
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent interaction) {
        super.onPlayerInteract(interaction);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW) // override protection plugins
    public void onBlockBreak(final BlockBreakEvent broken) {
        if (broken instanceof KioskRemove) return;
        if (!Kiosk.SIGN_BLOCKS.contains(broken.getBlock().getTypeId())) return;

        final Sign state = (Sign) broken.getBlock().getState();
        if (!this.hasTitle(state)) return;

        final Function function = this.getFunction(state);
        if (function == null) {
            Main.courier.send(broken.getPlayer(), "unknownFunction", state.getLine(1));
            broken.setCancelled(true);
            state.update();
            return;
        }

        if (!function.canUse(broken.getPlayer())) {
            Main.courier.submit(new Sender(broken.getPlayer()), function.draftDenied());
            broken.setCancelled(true);
            state.update();
            return;
        }

        this.dispatch(broken.getPlayer(), state);

        // manually process block break to avoid other plugins responding to event
        broken.setCancelled(true);
        state.setType(Material.AIR);
        state.update(true);
        broken.getBlock().getWorld().dropItemNaturally(broken.getBlock().getLocation(), new ItemStack(Material.SIGN));

    }

}
