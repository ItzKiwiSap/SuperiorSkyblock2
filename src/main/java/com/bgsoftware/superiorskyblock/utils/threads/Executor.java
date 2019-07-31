package com.bgsoftware.superiorskyblock.utils.threads;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Executor {

    private static final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("SuperiorSkyblock Thread %d").build());
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void sync(Runnable runnable){
        sync(runnable, 0);
    }

    public static void sync(Runnable runnable, long delay){
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public static void async(Runnable runnable){
        if(Bukkit.isPrimaryThread()){
            executor.execute(runnable);
        }
        else{
            runnable.run();
        }
    }

}
