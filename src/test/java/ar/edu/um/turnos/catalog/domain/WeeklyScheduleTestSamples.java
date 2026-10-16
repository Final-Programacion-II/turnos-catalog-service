package ar.edu.um.turnos.catalog.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class WeeklyScheduleTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static WeeklySchedule getWeeklyScheduleSample1() {
        return new WeeklySchedule().id(1L).externalId(1L).slotDurationMinutes(1);
    }

    public static WeeklySchedule getWeeklyScheduleSample2() {
        return new WeeklySchedule().id(2L).externalId(2L).slotDurationMinutes(2);
    }

    public static WeeklySchedule getWeeklyScheduleRandomSampleGenerator() {
        return new WeeklySchedule()
            .id(longCount.incrementAndGet())
            .externalId(longCount.incrementAndGet())
            .slotDurationMinutes(intCount.incrementAndGet());
    }
}
