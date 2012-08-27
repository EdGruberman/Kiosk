package edgruberman.bukkit.kiosk;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;

import edgruberman.bukkit.kiosk.messaging.Message;

// TODO remove created permissions on plugin disable
public class Function {

    private final String name;
    private final Permission use;
    private final Permission apply;
    private final List<String> dispatch = new ArrayList<String>();

    public Function(final ConfigurationSection definition) {
        this.name = definition.getName();
        this.use = this.addPermission(definition.getString("use"));
        this.apply = this.addPermission(definition.getString("apply"));
        if (this.dispatch != null) this.dispatch.addAll(this.getStringList(definition, "dispatch"));
    }

    public String getName() {
        return this.name;
    }

    public List<Message> draftDenied(final Object... arguments) {
        return Main.courier.draft("functions." + this.name + ".denied", arguments);
    }

    public boolean canUse(final CommandSender sender) {
        return (this.use != null ? sender.hasPermission(this.use) || sender.hasPermission("kiosk.override.use") : true);
    }

    public boolean canApply(final CommandSender sender) {
        return (this.apply != null ? sender.hasPermission(this.apply) || sender.hasPermission("kiosk.override.apply") : true);
    }

    public List<Message> draftDescription(final Object... arguments) {
        return Main.courier.draft("functions." + this.name + ".description", arguments);
    }

    public void dispatch(final Object... arguments) {
        // prepend null argument to emulate argument index with description
        final Object[] shifted = new Object[arguments.length + 1];
        shifted[0] = null;
        System.arraycopy(arguments, 0, shifted, 1, arguments.length);

        for (final String command : this.format(this.dispatch, shifted)) {
            Main.courier.getPlugin().getLogger().log(Level.FINEST, "Function dispatch for {0}: {1}", new Object[] { this.name, command });
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    private Permission addPermission(final String name) {
        if (name == null) return null;

        Permission permission = Bukkit.getPluginManager().getPermission(name);
        if (permission == null) {
            permission = new Permission(name);
            Bukkit.getPluginManager().addPermission(permission);

        }
        return permission;
    }

    private List<String> getStringList(final ConfigurationSection section, final String path) {
        if (section.isString(path)) return Arrays.asList(section.getString(path));
        if (!section.isList(path)) return Collections.emptyList();
        return section.getStringList(path);
    }

    private List<String> format(final List<String> list, final Object... arguments) {
        final List<String> formatted = new ArrayList<String>();
        for (final String line : list) formatted.add(MessageFormat.format(line, arguments));
        return formatted;
    }

}
