package me.kubbidev.cobbleworld.pokemon;

import me.kubbidev.cobbleworld.hologram.Hologram;
import me.kubbidev.cobbleworld.time.DurationFormatter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

@Environment(EnvType.SERVER)
public class CaughtPokemonBoard extends Hologram {
    private static final int SCORES_OFFSET = 2;
    private static final int SCORES_AMOUNT = 10;
    private static final int FOOTER_OFFSET = SCORES_OFFSET + SCORES_AMOUNT;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");

    private final Supplier<Duration> remainingDuration;

    public CaughtPokemonBoard(
            Supplier<Duration> remainingDuration, ServerWorld serverWorld
    ) {
        super(serverWorld);
        this.remainingDuration = remainingDuration;
        addLine(Text.literal("Most Caught Pokemon")
                .styled(s -> s
                        .withBold(true)
                        .withColor(Formatting.AQUA)
                ));

        addLine(Text.literal("Daily scores")
                .styled(s -> s.withColor(Formatting.GRAY)));

        forEachScores(i -> insertLine(i + SCORES_OFFSET, Text.literal("Line" + i)));
        insertLine(FOOTER_OFFSET, Text.literal("Refresh in : 0s"));
    }

    @Override
    public void baseTick() {
        setLine(FOOTER_OFFSET, Text.literal("Refresh in : " + getScoresRefreshFormattedDuration())
                .styled(s -> s.withColor(Formatting.GRAY)));
    }

    private void forEachScores(IntConsumer consumer) {
        for (int i = 0; i < SCORES_AMOUNT; i++) consumer.accept(i);
    }

    private Duration getScoresRefreshDuration() {
        return this.remainingDuration.get();
    }

    private String getScoresRefreshFormattedDuration() {
        return DurationFormatter.CONCISE_LOW_ACCURACY.format(getScoresRefreshDuration());
    }

    public void refreshScores(List<CaughtPokemonModule.Entry> entries) {
        entries.sort(Comparator.comparing(CaughtPokemonModule.Entry::caught).reversed());
        forEachScores(i -> {
            var entry = i < entries.size() ? entries.get(i) : null;

            MutableText score = Text.literal("." + (i + 1) + " ")
                    .styled(s -> s.withColor(Formatting.YELLOW));

            if (entry != null) {
                score.append(Text.literal(entry.username())
                        .styled(s -> s.withColor(Formatting.GOLD)));
            }

            score.append(Text.literal(" - ")
                    .styled(s -> s.withColor(Formatting.GRAY)));

            setLine(i + SCORES_OFFSET, score.append(
                    DECIMAL_FORMAT.format(entry != null ? entry.caught() : 0)
            ));
        });
    }
}
