import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linkuei.HoursData;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HoursDataTest {

    private final HoursData hoursData = HoursData.getInstance();

    @BeforeEach
    void reset() {
        hoursData.resetCurrentTime();
        hoursData.resetHours();
    }

    @Test
    void shouldAdd1HourAnd1MinuteToTotalHours() {
        // given
        LocalTime toAdd = LocalTime.of(1, 1);
        // when
        hoursData.addHours(toAdd);
        //then
        assertEquals("1h 1m", hoursData.getHoursString());
    }

    @Test
    void shouldRemove1HourFromTotalHours() {
        // given
        LocalTime toAdd = LocalTime.of(1, 1, 0);
        hoursData.addHours(toAdd);
        LocalTime toRemove = LocalTime.of(1, 0, 0);
        // when
        hoursData.removeHours(toRemove);
        //then
        assertEquals("0h 1m", hoursData.getHoursString());
    }

    @Test
    void shouldResetCurrentTimeToWorkHoursValue() {
        // given
        // when
        //then
        assertEquals(hoursData.getWorkHours(), hoursData.getCurrentTime().getHour());
    }

    @Test
    void shouldDecreaseCurrentTimeByOneSecond() {
        // given
        hoursData.setWorkHours(8);
        hoursData.setStartTime(LocalTime.now());
        hoursData.recalculate();
        // when
        hoursData.minusTime();
        //then
        assertEquals("07:59:59", hoursData.getCurrentTimeString());
    }

    @Test
    void shouldHaveOneMinuteOvertime() {
        // given
        hoursData.setWorkHours(0);
        hoursData.setStartTime(LocalTime.now());
        // when
        for (int i = 70; i > 0; i--) {
            hoursData.minusTime();
        }
        hoursData.appendOvertime();
        // then
        assertEquals("0h 1m", hoursData.getHoursString());
    }

    @Test
    void shouldHaveOneHourUndertime() {
        // given
        hoursData.setWorkHours(1);
        hoursData.resetCurrentTime();
        // when
        hoursData.appendUndertime();
        // then
        assertEquals("-1h 0m", hoursData.getHoursString());
    }

    @Test
    void shouldSetEndTimeAsStartTimePlus8Hours() {
        // given
        hoursData.resetHours();
        hoursData.resetCurrentTime();
        final int START_HOUR = 7;
        // when
        hoursData.setStartTime(LocalTime.of(START_HOUR, 0));
        // then
        assertEquals(LocalTime.of(START_HOUR + hoursData.getWorkHours(), 0), hoursData.getEndTime());
    }
}
