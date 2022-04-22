package org.inventivetalent.bossbar;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public class BukkitBossBar implements BossBar {

    private final org.bukkit.boss.BossBar bukkitBossBar;
    private final Collection<Player> receivers = new ArrayList<>();
    private float progress;
    private BossBarAPI.Color color;
    private BossBarAPI.Style style;

    protected BukkitBossBar(String message, BossBarAPI.Color color, BossBarAPI.Style style, float progress, BossBarAPI.Property... properties) {
        this.color = color != null ? color : BossBarAPI.Color.PURPLE;
        this.style = style != null ? style : BossBarAPI.Style.PROGRESS;

        bukkitBossBar = Bukkit.createBossBar(message, BarColor.valueOf(this.color.name()), BarStyle.valueOf(this.style.name().replace("NOTCHED", "SEGMENTED").replace("PROGRESS", "SOLID")));
        setMessage(message);
        setProgress(progress);

        for (BossBarAPI.Property property : properties) {
            setProperty(property, true);
        }
    }

    protected BukkitBossBar(BaseComponent message, BossBarAPI.Color color, BossBarAPI.Style style, float progress, BossBarAPI.Property... properties) {
        this(ComponentSerializer.toString(message), color, style, progress, properties);
    }

    @Override
    public Collection<? extends Player> getPlayers() {
        return receivers;
    }

    @Override
    public void addPlayer(Player player) {
        if (!receivers.contains(player)) {
            bukkitBossBar.addPlayer(player);
            receivers.add(player);

            BossBarAPI.addBarForPlayer(player, this);
        }
    }

    @Override
    public void removePlayer(Player player) {
        if (receivers.contains(player)) {
            bukkitBossBar.removePlayer(player);
            receivers.remove(player);

            BossBarAPI.removeBarForPlayer(player, this);
        }
    }

    @Override
    public BossBarAPI.Color getColor() {
        return color;
    }

    @Override
    public void setColor(BossBarAPI.Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color cannot be null");
        }

        if (this.color != color) {
            bukkitBossBar.setColor(BarColor.valueOf(color.name()));
            this.color = color;
        }
    }

    @Override
    public BossBarAPI.Style getStyle() {
        return style;
    }

    @Override
    public void setStyle(BossBarAPI.Style style) {
        if (style == null) {
            throw new IllegalArgumentException("style cannot be null");
        }

        if (this.style != style) {
            bukkitBossBar.setStyle(BarStyle.valueOf(style.name().replace("NOTCHED", "SEGMENTED").replace("PROGRESS", "SOLID")));
            this.style = style;
        }
    }

    @Override
    public void setProperty(BossBarAPI.Property property, boolean flag) {
        BarFlag barFlag = BarFlag.valueOf(property.name());

        if (!flag) {
            if (bukkitBossBar.hasFlag(barFlag)) {
                bukkitBossBar.removeFlag(barFlag);
            }
        } else {
            if (!bukkitBossBar.hasFlag(barFlag)) {
                bukkitBossBar.addFlag(barFlag);
            }
        }
    }

    @Override
    public String getMessage() {
        return bukkitBossBar.getTitle();
    }

    @Override
    public void setMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }

        if (!message.startsWith("{") || !message.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON");
        }

        String title = ComponentSerializer.parse(message)[0].toLegacyText();
        if (!getMessage().equals(title)) {
            bukkitBossBar.setTitle(title);
        }
    }

    @Override
    public Player getReceiver() {
        return null;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public void updateMovement() {

    }

    @Override
    public float getProgress() {
        return progress;
    }

    @Override
    public void setProgress(float progress) {
        if (this.progress == progress) return;

        if (progress > 1) this.progress = progress / 100F;
        else this.progress = progress;

        bukkitBossBar.setProgress(this.progress);
    }

    @Override
    public boolean isVisible() {
        return bukkitBossBar.isVisible();
    }

    @Override
    public void setVisible(boolean flag) {
        if (isVisible() == flag) return;
        bukkitBossBar.setVisible(flag);
    }

    @Override
    public float getMaxHealth() {
        return 100F;
    }

    @Override
    public float getHealth() {
        return getProgress() * 100F;
    }

    @Override
    public void setHealth(float percentage) {
        setProgress(percentage / 100F);
    }

}
