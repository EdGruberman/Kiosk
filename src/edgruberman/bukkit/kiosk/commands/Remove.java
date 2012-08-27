package edgruberman.bukkit.kiosk.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.kiosk.Attendant;
import edgruberman.bukkit.kiosk.Kiosk;
import edgruberman.bukkit.kiosk.KioskRemove;
import edgruberman.bukkit.kiosk.Main;

public final class Remove implements CommandExecutor {

    private final Attendant attendant;

    public Remove(final Attendant attendant) {
        this.attendant = attendant;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "messages.requiresPlayer", label);
            return false;
        }

        final Player player = (Player) sender;
        final Block target = player.getTargetBlock(null, 16);

        if (!this.isKiosk(target)) {
            Main.courier.send(sender, "messages.unknownKiosk");
            return true;
        }

        final KioskRemove removal = new KioskRemove(target, player);
        Bukkit.getPluginManager().callEvent(removal);
        if (removal.isCancelled()) {
            Main.courier.send(sender, "messages.removeCancel");
            return true;
        }

        target.setType(Material.AIR);
        target.getWorld().dropItemNaturally(target.getLocation(), new ItemStack(Material.SIGN));
        return true;
    }

    private boolean isKiosk(final Block block) {
        if (!Kiosk.MATERIALS.contains(block.getTypeId())) return false;

        final Sign state = (Sign) block.getState();
        for (final Kiosk kiosk : this.attendant.getKiosks())
            if (kiosk.hasTitle(state)) return true;

        return false;
    }

}
