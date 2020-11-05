import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linkuei.HoursDataHandler;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HoursDataTest {

    private final HoursDataHandler hoursDataHandler = HoursDataHandler.INSTANCE;

    @BeforeEach
    void reset() {
        hoursDataHandler.resetCurrentTime();
        // hoursData.resetHours();
    }

    @Test
    void shouldAdd1HourAnd1MinuteToTotalHours() {
        // given
        LocalTime toAdd = LocalTime.of(1, 1);
        // when
        hoursDataHandler.addHours(toAdd);
        //then
        assertEquals("1h 1m", hoursDataHandler.getHoursString(hoursDataHandler.getHoursData()));
    }

    @Test
    void shouldRemove1HourFromTotalHours() {
        // given
        LocalTime toAdd = LocalTime.of(1, 1, 0);
        hoursDataHandler.addHours(toAdd);
        LocalTime toRemove = LocalTime.of(1, 0, 0);
        // when
        hoursDataHandler.removeHours(toRemove);
        //then
        assertEquals("0h 1m", hoursDataHandler.getHoursString(hoursDataHandler.getHoursData()));
    }

    @Test
    void shouldResetCurrentTimeToWorkHoursValue() {
        // given
        // when
        //then
        assertEquals(hoursDataHandler.getHoursData().getWorkHours(), hoursDataHandler.getHoursData().getCurrentTime().getHour());
    }

    @Test
    void shouldDecreaseCurrentTimeByOneSecond() {
        // given
        hoursDataHandler.getHoursData().setWorkHours(8);
        hoursDataHandler.setStartTime(LocalTime.now());
        hoursDataHandler.recalculate();
        // when
        hoursDataHandler.minusTime();
        //then
        assertEquals("07:59:59", hoursDataHandler.getCurrentTimeString(hoursDataHandler.getHoursData()));
    }

    @Test
    void shouldHaveOneMinuteOvertime() {
        // given
        hoursDataHandler.getHoursData().setWorkHours(0);
        hoursDataHandler.setStartTime(LocalTime.now());
        // when
        for (int i = 70; i > 0; i--) {
            hoursDataHandler.minusTime();
        }
        hoursDataHandler.appendOvertime();
        // then
        assertEquals("0h 1m", hoursDataHandler.getHoursString(hoursDataHandler.getHoursData()));
    }

    @Test
    void shouldHaveOneHourUndertime() {
        // given
        hoursDataHandler.getHoursData().setWorkHours(1);
        hoursDataHandler.resetCurrentTime();
        // when
        hoursDataHandler.appendUndertime();
        // then
        assertEquals("-1h 0m", hoursDataHandler.getHoursString(hoursDataHandler.getHoursData()));
    }

    @Test
    void shouldSetEndTimeAsStartTimePlus8Hours() {
        // given
        // hoursDataHandler.resetHours();
        hoursDataHandler.resetCurrentTime();
        final int START_HOUR = 7;
        // when
        hoursDataHandler.setStartTime(LocalTime.of(START_HOUR, 0));
        // then
        assertEquals(LocalTime.of(START_HOUR + hoursDataHandler.getHoursData().getWorkHours(), 0), hoursDataHandler.getHoursData().getEndTime());
    }
}
