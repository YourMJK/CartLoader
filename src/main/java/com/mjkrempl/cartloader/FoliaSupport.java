package com.mjkrempl.cartloader;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Small compatibility layer for Folia's region-based threading model.
 *
 * <p>Folia is not available as a compile-time dependency for plugins that also target Spigot,
 * so all Folia-specific API calls are made through reflection. On non-Folia servers every
 * method in this class falls back to the regular Bukkit behaviour.</p>
 */
public final class FoliaSupport {
	/**
	 * Minimal abstraction over either a BukkitTask or Folia's ScheduledTask.
	 */
	public interface ScheduledTask {
		boolean isCancelled();
		void cancel();
	}
	
	private static final boolean FOLIA = detectFolia();
	
	private static final Method GET_GLOBAL_REGION_SCHEDULER;
	private static final Method RUN_AT_FIXED_RATE;
	private static final Method SCHEDULED_TASK_CANCEL;
	private static final Method SCHEDULED_TASK_IS_CANCELLED;
	private static final Method ENTITY_GET_SCHEDULER;
	private static final Method ENTITY_SCHEDULER_EXECUTE;
	
	static {
		if (FOLIA) {
			try {
				GET_GLOBAL_REGION_SCHEDULER = Bukkit.class.getMethod("getGlobalRegionScheduler");
				
				Class<?> globalSchedulerClass = GET_GLOBAL_REGION_SCHEDULER.getReturnType();
				RUN_AT_FIXED_RATE = globalSchedulerClass.getMethod(
					"runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class
				);
				
				Class<?> scheduledTaskClass = RUN_AT_FIXED_RATE.getReturnType();
				SCHEDULED_TASK_CANCEL = scheduledTaskClass.getMethod("cancel");
				SCHEDULED_TASK_IS_CANCELLED = scheduledTaskClass.getMethod("isCancelled");
				
				ENTITY_GET_SCHEDULER = Entity.class.getMethod("getScheduler");
				Class<?> entitySchedulerClass = ENTITY_GET_SCHEDULER.getReturnType();
				ENTITY_SCHEDULER_EXECUTE = entitySchedulerClass.getMethod(
					"execute", Plugin.class, Runnable.class, Runnable.class, long.class
				);
			}
			catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Failed to initialise Folia compatibility layer", e);
			}
		}
		else {
			GET_GLOBAL_REGION_SCHEDULER = null;
			RUN_AT_FIXED_RATE = null;
			SCHEDULED_TASK_CANCEL = null;
			SCHEDULED_TASK_IS_CANCELLED = null;
			ENTITY_GET_SCHEDULER = null;
			ENTITY_SCHEDULER_EXECUTE = null;
		}
	}
	
	private FoliaSupport() {}
	
	
	/**
	 * @return whether the server is running Folia
	 */
	public static boolean isFolia() {
		return FOLIA;
	}
	
	/**
	 * Runs a task at a fixed rate on the global region scheduler, or on the Bukkit scheduler
	 * when the server is not running Folia.
	 */
	public static ScheduledTask runAtFixedRate(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
		if (FOLIA) {
			try {
				Object globalScheduler = GET_GLOBAL_REGION_SCHEDULER.invoke(null);
				Consumer<Object> consumer = task -> runnable.run();
				Object scheduledTask = RUN_AT_FIXED_RATE.invoke(
					globalScheduler, plugin, consumer, delayTicks, periodTicks
				);
				return new ReflectionScheduledTask(scheduledTask);
			}
			catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Failed to schedule task on Folia's global region scheduler", e);
			}
		}
		
		BukkitTask task = new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskTimer(plugin, delayTicks, periodTicks);
		return new BukkitScheduledTask(task);
	}
	
	/**
	 * Runs a task on the thread that owns the given entity, or immediately when the server
	 * is not running Folia.
	 *
	 * @param retiredTask task to run if the entity has already been removed
	 */
	public static void executeOnEntity(Plugin plugin, Entity entity, Runnable task, Runnable retiredTask) {
		if (!FOLIA) {
			task.run();
			return;
		}
		
		try {
			Object entityScheduler = ENTITY_GET_SCHEDULER.invoke(entity);
			ENTITY_SCHEDULER_EXECUTE.invoke(entityScheduler, plugin, task, retiredTask, 0L);
		}
		catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to schedule task on entity thread", e);
		}
	}
	
	
	private static boolean detectFolia() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer", false, FoliaSupport.class.getClassLoader());
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	private static Object invoke(Method method, Object target, Object... args) throws ReflectiveOperationException {
		try {
			return method.invoke(target, args);
		}
		catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException runtimeException) throw runtimeException;
			if (cause instanceof Error error) throw error;
			throw e;
		}
	}
	
	
	private record ReflectionScheduledTask(Object task) implements ScheduledTask {
		@Override
		public boolean isCancelled() {
			try {
				return (boolean) invoke(SCHEDULED_TASK_IS_CANCELLED, task);
			}
			catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Failed to query Folia task state", e);
			}
		}
		
		@Override
		public void cancel() {
			try {
				invoke(SCHEDULED_TASK_CANCEL, task);
			}
			catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Failed to cancel Folia task", e);
			}
		}
	}
	
	private record BukkitScheduledTask(BukkitTask task) implements ScheduledTask {
		@Override
		public boolean isCancelled() {
			return task.isCancelled();
		}
		
		@Override
		public void cancel() {
			task.cancel();
		}
	}
}
