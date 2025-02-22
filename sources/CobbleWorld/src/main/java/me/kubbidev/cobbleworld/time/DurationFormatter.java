package me.kubbidev.cobbleworld.time;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public enum DurationFormatter {
    LONG {
        @Override
        protected String formatUnitPlural(ChronoUnit unit) {
            return ' ' + unit.name().toLowerCase();
        }

        @Override
        protected String formatUnitSingle(ChronoUnit unit) {
            String s = unit.name().toLowerCase();
            return ' ' + s.substring(0, s.length() - 1);
        }
    },
    CONCISE_LOW_ACCURACY(3),
    CONCISE;

    private final Unit[] units = new Unit[]{
            new Unit(ChronoUnit.YEARS),
            new Unit(ChronoUnit.MONTHS),
            new Unit(ChronoUnit.WEEKS),
            new Unit(ChronoUnit.DAYS),
            new Unit(ChronoUnit.HOURS),
            new Unit(ChronoUnit.MINUTES),
            new Unit(ChronoUnit.SECONDS)
    };

    private final int accuracy;

    DurationFormatter() {
        this(Integer.MAX_VALUE);
    }

    DurationFormatter(int accuracy) {
        this.accuracy = accuracy;
    }

    public String format(Duration duration) {
        long seconds = duration.getSeconds();
        StringBuilder output = new StringBuilder();
        int outputSize = 0;

        for (Unit unit : this.units) {
            long n = seconds / unit.seconds;
            if (n > 0) {
                seconds -= unit.seconds * n;
                output.append(' ').append(n).append(unit.toString(n));
                outputSize++;
            }
            if (seconds <= 0 || outputSize >= this.accuracy) {
                break;
            }
        }

        if (output.isEmpty()) {
            return '0' + this.units[this.units.length - 1].plural;
        }
        return output.substring(1);
    }

    protected String formatUnitPlural(ChronoUnit unit) { // concise
        return String.valueOf(Character.toLowerCase(unit.name().charAt(0)));
    }

    protected String formatUnitSingle(ChronoUnit unit) {
        return formatUnitPlural(unit);
    }

    private final class Unit {
        private final long seconds;
        private final String plural;
        private final String single;

        Unit(ChronoUnit unit) {
            this.seconds = unit.getDuration().getSeconds();
            this.plural = formatUnitPlural(unit);
            this.single = formatUnitSingle(unit);
        }

        public String toString(long n) {
            return n == 1 ? this.single : this.plural;
        }
    }

}