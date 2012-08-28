package edgruberman.bukkit.kiosk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import edgruberman.bukkit.kiosk.commands.Reload;
import edgruberman.bukkit.kiosk.commands.Remove;
import edgruberman.bukkit.kiosk.messaging.ConfigurationCourier;
import edgruberman.bukkit.kiosk.messaging.Courier;
import edgruberman.bukkit.kiosk.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static Courier courier;

    final List<Permission> permissions = new ArrayList<Permission>();

    @Override
    public void onLoad() { this.putConfigMinimum(CustomPlugin.CONFIGURATION_FILE, "1.0.2"); }

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = new ConfigurationCourier(this);

        this.loadPermissions(this.getConfig().getConfigurationSection("permissions"));

        final Attendant attendant = new Attendant();

        final ConfigurationSection breakable = this.getConfig().getConfigurationSection("kiosks.breakable");
        final BreakableKiosk breakableKiosk = new BreakableKiosk(attendant, breakable.getString("title"), breakable.getString("trigger"));
        attendant.hire(breakableKiosk);

        final ConfigurationSection punchable = this.getConfig().getConfigurationSection("kiosks.punchable");
        final PunchableKiosk punchableKiosk = new PunchableKiosk(attendant, punchable.getString("title"), punchable.getString("trigger"));
        attendant.hire(punchableKiosk);

        final ConfigurationSection functions = this.getConfig().getConfigurationSection("functions");
        for (final String name : functions.getKeys(false)) {
            final Function function = new Function(functions.getConfigurationSection(name));
            attendant.train(function);
        }

        Bukkit.getPluginManager().registerEvents(attendant, this);
        Bukkit.getPluginManager().registerEvents(breakableKiosk, this);
        Bukkit.getPluginManager().registerEvents(punchableKiosk, this);

        this.getCommand("kiosk:remove").setExecutor(new Remove(attendant));
        this.getCommand("kiosk:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        for (final Permission permission : this.permissions)
            Bukkit.getPluginManager().removePermission(permission);

        Main.courier = null;
    }

    private void loadPermissions(final ConfigurationSection permissions) {
        if (permissions == null) return;

        for (final String name : permissions.getKeys(false)) {
            final ConfigurationSection permission = permissions.getConfigurationSection(name);
            final String description = permission.getString("description");
            final PermissionDefault permDefault = PermissionDefault.getByName(permission.getString("default", Permission.DEFAULT_PERMISSION.name()));

            final Map<String, Boolean> children = new HashMap<String, Boolean>();
            final ConfigurationSection c = permission.getConfigurationSection("children");
            if (c != null)
                for (final String child : c.getKeys(false))
                    children.put(child, permission.getConfigurationSection("children").getBoolean(child));

            final Permission created = new Permission(name, description, permDefault, children);
            this.permissions.add(created);
            Bukkit.getPluginManager().addPermission(created);
        }
    }

}
