import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linkuei.HoursData;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HoursDataTest {

    private final HoursData hoursData = HoursData.getInstance();

    @BeforeEach
    void setUp() {
        hoursData.setMaxTime(LocalTime.of(8, 0));
        hoursData.resetHours();
        hoursData.resetCurrentTime();
    }

    @Test
    void addHoursTest() {
        // given
        LocalTime toAdd = LocalTime.of(1, 1, 0);
        // when
        hoursData.addHours(toAdd);
        //then
        assertEquals("1h 1m", hoursData.getHoursString());
    }

    @Test
    void removeHoursTest() {
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
    void resetCurrentTimeTest() {
        // given
        hoursData.minusTime();
        // when
        hoursData.resetCurrentTime();
        //then
        assertEquals(hoursData.getMaxTimeString(), hoursData.getCurrentTimeString());
    }

    @Test
    void minusTimeTest() {
        // given
        // when
        hoursData.minusTime();
        //then
        assertEquals("07:59:59", hoursData.getCurrentTimeString());
    }

    @Test
    void appendOvertimeTest() {
        // given
        hoursData.setMaxTime(LocalTime.of(0, 0, 5));
        hoursData.resetCurrentTime();
        // when
        for (int i = 70; i > 0; i--) {
            hoursData.minusTime();
        }
        hoursData.appendOvertime();
        // then
        assertEquals("0h 1m", hoursData.getHoursString());
    }

    @Test
    void appendUndertimeTest() {
        // given
        hoursData.setMaxTime(LocalTime.of(0, 1, 0));
        hoursData.resetCurrentTime();
        // when
        hoursData.appendUndertime();
        // then
        assertEquals("-0h 1m", hoursData.getHoursString());
    }
}
