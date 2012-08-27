package edgruberman.bukkit.kiosk;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class KioskRemove extends BlockBreakEvent {

    public KioskRemove(final Block theBlock, final Player player) {
        super(theBlock, player);
    }

}
