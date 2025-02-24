package me.kubbidev.cobbleworld.application.command;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.kubbidev.cobbleworld.CobbleWorldMod;
import me.kubbidev.cobbleworld.ImmutableCollectors;
import me.kubbidev.cobbleworld.application.command.sender.CommandSender;
import me.kubbidev.cobbleworld.application.command.sender.MessageChannelSender;
import me.kubbidev.cobbleworld.application.commands.PingCommand;
import me.kubbidev.cobbleworld.scheduler.SchedulerAdapter;
import me.kubbidev.cobbleworld.scheduler.SchedulerTask;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InteractionManager extends ListenerAdapter {

    private final CobbleWorldMod mod;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("cobbleworld-interaction-executor")
            .build()
    );
    private final AtomicBoolean executingCommand = new AtomicBoolean(false);
    private final Map<String, Interaction> interactions;

    public InteractionManager(CobbleWorldMod mod) {
        this.mod = mod;
        this.interactions = ImmutableList.<Interaction>builder()
                .add(new PingCommand())
                .build()
                .stream()
                .collect(ImmutableCollectors.toMap(c -> c.getMetadata().getName().toLowerCase(Locale.ROOT), Function.identity()));
    }

    public CobbleWorldMod getMod() {
        return this.mod;
    }

    @VisibleForTesting
    public Map<String, Interaction> getInteractions() {
        return this.interactions;
    }

    /**
     * Registers the {@link Interaction}s with the specified JDA (Java Discord API) shard.
     * <p>
     * This method updates and queues the commands associated with the interactions currently
     * available in the manager.
     *
     * @param shard The JDA instance representing a shard.
     */
    public void registerInteraction(@NotNull JDA shard) {
        var commands = shard.updateCommands();
        for (Interaction i : this.interactions.values()) {
            commands = commands.addCommands(i.getMetadata());
        }

        commands.queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        executeCommand(MessageChannelSender.wrap(e.getChannel()), e.getName(), new InteractionContext(e));
    }

    public CompletableFuture<Void> executeCommand(CommandSender sender, String label, InteractionContext context) {
        SchedulerAdapter scheduler = this.mod.getScheduler();

        // if the executingCommand flag is set, there is another command executing at the moment
        if (this.executingCommand.get()) {
            context.setDeferred(true);
            context.sendMessage("Another command is being executed, waiting for it to finish...");
            return CompletableFuture.completedFuture(null);
        }

        // a reference to the thread being used to execute the command
        AtomicReference<Thread> executorThread = new AtomicReference<>();
        // a reference to the timeout task scheduled to catch if this command takes too long to execute
        AtomicReference<SchedulerTask> timeoutTask = new AtomicReference<>();

        // schedule the actual execution of the command using the command executor service
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // set flags
            executorThread.set(Thread.currentThread());
            this.executingCommand.set(true);

            // actually try to execute the command
            try {
                execute(sender, label, context);
            } catch (Throwable e) {
                handleStackTrace(context, e);

                // catch any exception
                CobbleWorldMod.LOGGER.error("Exception whilst executing command: {}", label, e);
            } finally {
                // unset flags
                this.executingCommand.set(false);
                executorThread.set(null);

                // cancel the timeout task
                SchedulerTask timeout;
                if ((timeout = timeoutTask.get()) != null) {
                    timeout.cancel();
                }
            }
        }, this.executor);

        // schedule another task to catch if the command doesn't complete after 10 seconds
        timeoutTask.set(scheduler.asyncLater(() -> {
            if (!future.isDone()) {
                handleCommandTimeout(executorThread, label);
            }
        }, 10, TimeUnit.SECONDS));

        return future;
    }

    private void handleStackTrace(InteractionContext context, Throwable e) {
        var output = new StringWriter();
        var writer = new PrintWriter(output);
        e.printStackTrace(writer);

        List<String> trace = new ArrayList<>();
        trace.add(":warning:Exception whilst executing command:");
        trace.add("```");
        trace.add(output.toString());

        trace.add("```");
        String stackedTrace = String.join("\n", trace);

        context.setDeferred(false);
        context.sendMessage(stackedTrace);
    }

    private void handleCommandTimeout(AtomicReference<Thread> thread, String label) {
        Thread executorThread = thread.get();
        if (executorThread == null) {
            CobbleWorldMod.LOGGER.warn("Interaction execution {} has not completed - is another interaction execution blocking it?", label);
        } else {
            String stackTrace = Arrays.stream(executorThread.getStackTrace())
                    .map(s -> "  " + s)
                    .collect(Collectors.joining("\n"));
            CobbleWorldMod.LOGGER.warn("Interaction execution {} has not completed. Trace: \n{}", label, stackTrace);
        }
    }

    private void execute(CommandSender sender, String label, InteractionContext context) {
        Interaction main = this.interactions.get(label.toLowerCase(Locale.ROOT));

        // Try to execute the interaction.
        if (main != null) {
            main.execute(this.mod, sender, context);
        } else {
            context.setDeferred(true);
            context.sendMessage("Command not recognised.");
        }
    }
}