package org.inventivetalent.bossbar;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BossBarTimer extends BukkitRunnable {

	private BukkitBossBar bossBar;
	final   float         progressMinus;

	public BossBarTimer(BukkitBossBar bukkitBossBar, float progress, int timeout) {
		this.bossBar = bukkitBossBar;
		this.progressMinus = progress / timeout;
	}

	@Override
	public void run() {
		float newProgress = bossBar.getProgress() - progressMinus;
		if (newProgress <= 0) {
			for (Player player : bossBar.getPlayers()) {
				bossBar.removePlayer(player);
			}
		} else {
			bossBar.setProgress(newProgress);
		}
	}
}
