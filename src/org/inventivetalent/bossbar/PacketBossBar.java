package org.inventivetalent.bossbar;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class PacketBossBar implements BossBar {

    static NMSClassResolver nmsClassResolver = new NMSClassResolver();

    static Class<?> PacketPlayOutBoss = nmsClassResolver.resolveSilent("PacketPlayOutBoss");
    static FieldResolver PacketPlayOutBossFieldResolver = new FieldResolver(PacketPlayOutBoss);
    static Class<?> PacketPlayOutBossAction = nmsClassResolver.resolveSilent("PacketPlayOutBoss$Action");
    static Class<?> ChatSerializer = nmsClassResolver.resolveSilent("ChatSerializer", "IChatBaseComponent$ChatSerializer");
    static MethodResolver ChatSerializerMethodResolver = new MethodResolver(ChatSerializer);
    static Class<?> BossBattleBarColor = nmsClassResolver.resolveSilent("BossBattle$BarColor");
    static Class<?> BossBattleBarStyle = nmsClassResolver.resolveSilent("BossBattle$BarStyle");
    private final UUID uuid;
    private final org.bukkit.boss.BossBar bukkitBossBar;
    private final Collection<Player> receivers = new ArrayList<>();
    private float progress;
    private String message;
    private BossBarAPI.Color color;
    private BossBarAPI.Style style;
    private boolean visible;

    private boolean darkenSky;
    private boolean playMusic;
    private boolean createFog;

    protected PacketBossBar(String message, BossBarAPI.Color color, BossBarAPI.Style style, float progress, BossBarAPI.Property... properties) {
        uuid = UUID.randomUUID();
        this.color = color != null ? color : BossBarAPI.Color.PURPLE;
        this.style = style != null ? style : BossBarAPI.Style.PROGRESS;

        bukkitBossBar = Bukkit.createBossBar(message, BarColor.valueOf(this.color.name()), BarStyle.valueOf(this.style.name().replace("NOTCHED", "SEGMENTED").replace("PROGRESS", "SOLID")));
        setMessage(message);
        setProgress(progress);

        for (BossBarAPI.Property property : properties) {
            setProperty(property, true);
        }
    }

    protected PacketBossBar(BaseComponent message, BossBarAPI.Color color, BossBarAPI.Style style, float progress, BossBarAPI.Property... properties) {
        this(ComponentSerializer.toString(message), color, style, progress, properties);
    }

    static Object serialize(String json) throws ReflectiveOperationException {
        return ChatSerializerMethodResolver.resolve(new ResolverQuery("a", String.class)).invoke(null, json);
    }

    @Override
    public Collection<? extends Player> getPlayers() {
        return new ArrayList<>(receivers);
    }

    @Override
    public void addPlayer(Player player) {
        if (!receivers.contains(player)) {
            bukkitBossBar.addPlayer(player);
            receivers.add(player);
            sendPacket(0, player);

            BossBarAPI.addBarForPlayer(player, this);
        }
    }

    @Override
    public void removePlayer(Player player) {
        if (receivers.contains(player)) {
            bukkitBossBar.removePlayer(player);
            receivers.remove(player);

            sendPacket(1, player);
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
            sendPacket(4, null);
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
            sendPacket(4, null);
        }
    }

    @Override
    public void setProperty(BossBarAPI.Property property, boolean flag) {
        switch (property) {
            case DARKEN_SKY:
                BarFlag barFlag = BarFlag.valueOf(property.name());

                if (!flag) bukkitBossBar.removeFlag(barFlag);
                else bukkitBossBar.addFlag(barFlag);

                darkenSky = flag;
                break;
            case PLAY_MUSIC:
                BarFlag barFlag1 = BarFlag.valueOf(property.name());

                if (!flag) bukkitBossBar.removeFlag(barFlag1);
                else bukkitBossBar.addFlag(barFlag1);

                playMusic = flag;
                break;
            case CREATE_FOG:
                BarFlag barFlag2 = BarFlag.valueOf(property.name());

                if (!flag) bukkitBossBar.removeFlag(barFlag2);
                else bukkitBossBar.addFlag(barFlag2);

                createFog = flag;
                break;
            default:
                break;
        }

        sendPacket(5, null);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }

        if (!message.startsWith("{") || !message.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON");
        }

        if (!this.message.equals(message)) {
            bukkitBossBar.setTitle(ComponentSerializer.parse(message)[0].toLegacyText());

            this.message = message;
            sendPacket(3, null);
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
        if (progress > 1) {
            this.progress = progress / 100F;
        }

        if (this.progress != progress) {
            bukkitBossBar.setProgress(progress);
            this.progress = progress;
            sendPacket(2, null);
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean flag) {
        if (flag != visible) {
            bukkitBossBar.setVisible(flag);
            visible = flag;
            sendPacket(flag ? 0 : 1, null);
        }
    }

    //Deprecated methods

    void sendPacket(int action, Player player) {
        try {
            Object packet = PacketPlayOutBoss.newInstance();
            PacketPlayOutBossFieldResolver.resolve("a").set(packet, uuid);
            PacketPlayOutBossFieldResolver.resolve("b").set(packet, PacketPlayOutBossAction.getEnumConstants()[action]);
            PacketPlayOutBossFieldResolver.resolve("c").set(packet, serialize(message));
            PacketPlayOutBossFieldResolver.resolve("d").set(packet, progress);
            PacketPlayOutBossFieldResolver.resolve("e").set(packet, BossBattleBarColor.getEnumConstants()[color.ordinal()]);
            PacketPlayOutBossFieldResolver.resolve("f").set(packet, BossBattleBarStyle.getEnumConstants()[style.ordinal()]);
            PacketPlayOutBossFieldResolver.resolve("g").set(packet, darkenSky);
            PacketPlayOutBossFieldResolver.resolve("h").set(packet, playMusic);
            PacketPlayOutBossFieldResolver.resolve("i").set(packet, createFog);

            if (player != null) {
                BossBarAPI.sendPacket(player, packet);
            } else {
                for (Player player1 : getPlayers()) {
                    BossBarAPI.sendPacket(player1, packet);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
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
