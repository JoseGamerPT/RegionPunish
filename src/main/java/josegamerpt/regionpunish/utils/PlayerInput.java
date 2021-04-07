package josegamerpt.regionpunish.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import josegamerpt.regionpunish.RegionPunish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class PlayerInput implements Listener {

    private static Map<UUID, PlayerInput> inputs = new HashMap<>();
    private UUID uuid;

    private ArrayList<String> texts = Text
            .color(Arrays.asList("&l&9Type in chat your input", "&fType &4cancel &fto cancel"));

    private InputRunnable runGo;
    private InputRunnable runCancel;
    private BukkitTask taskId;
    private Boolean inputMode;

    public PlayerInput(Player p, InputRunnable correct, InputRunnable cancel) {
        this.uuid = p.getUniqueId();
        p.closeInventory();
        this.inputMode = true;
        this.runGo = correct;
        this.runCancel = cancel;
        this.taskId = new BukkitRunnable() {
            public void run() {
                p.sendTitle(texts.get(0), texts.get(1));
            }
        }.runTaskTimer(RegionPunish.pl, 0L, 20);

        this.register();
    }

    public PlayerInput(Player p, InputRunnable correct, InputRunnable cancel, String titl1, String titl2) {
        this.uuid = p.getUniqueId();
        p.closeInventory();
        this.inputMode = true;
        this.runGo = correct;
        this.runCancel = cancel;
        this.taskId = new BukkitRunnable() {
            public void run() {
                p.sendTitle(Text.color(titl1), Text.color(titl2));
            }
        }.runTaskTimer(RegionPunish.pl, 0L, 20);

        this.register();
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onPlayerChat(AsyncPlayerChatEvent event) {
                String input = event.getMessage();
                UUID uuid = event.getPlayer().getUniqueId();
                if (inputs.containsKey(uuid)) {
                    PlayerInput current = inputs.get(uuid);
                    if (current.inputMode) {
                        event.setCancelled(true);
                        try {
                            if (input.equalsIgnoreCase("cancel")) {
                                event.getPlayer().sendMessage("Input canceled.");
                                current.taskId.cancel();
                                event.getPlayer().sendTitle("", "");
                                Bukkit.getScheduler().scheduleSyncDelayedTask(RegionPunish.pl, () -> current.runCancel.run(input), 3);
                                current.unregister();
                                return;
                            }

                            current.taskId.cancel();
                            Bukkit.getScheduler().scheduleSyncDelayedTask(RegionPunish.pl, () -> current.runGo.run(input), 3);
                            event.getPlayer().sendTitle("", "");
                            current.unregister();
                        } catch (Exception e) {
                            event.getPlayer().sendMessage("§cAn error occurred. Contact JoseGamer_PT on spigotmc.org");
                            e.printStackTrace();
                        }
                    }
                }
            }

        };
    }

    private void register() {
        inputs.put(this.uuid, this);
    }

    private void unregister() {
        inputs.remove(this.uuid);
    }

    @FunctionalInterface
    public interface InputRunnable {
        void run(String input);
    }
}