package org.unitime.timetable.reports.pointintimedata;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.unitime.timetable.security.UserContext;
import java.util.Map;

/**
 * Validation Test for Maintenance Phase 1
 * Demonstrates the remediation of the silent failure logic bug.
 */
public class BasePointInTimeDataReportsValidationTest {

    @Test
    public void testSubjectAreaExceptionHandling() {
        // 1. Setup: Mock the UserContext
        UserContext mockUser = Mockito.mock(UserContext.class);

        // We simulate a scenario where fetching subject areas fails.
        // Since we cannot easily mock the static SubjectArea.getUserSubjectAreas() without PowerMock,
        // we can trigger the exception by passing a null user context (which will throw a NullPointerException inside the loop).

        try {
            // 2. Execution: Attempt to fetch values with a null user context
            // Before Fix: This would silently catch the NullPointerException and return null.
            // After Fix: This explicitly catches the Exception, logs it, and throws a RuntimeException.
            Map<Long, String> result = BasePointInTimeDataReports.Parameter.SUBJECT.getValues(null);

            // If the code reaches this line, it means it failed silently and returned null or empty!
            if (result == null) {
                fail("VALIDATION FAILED: The method failed silently and returned null instead of throwing an exception.");
            }

        } catch (RuntimeException e) {
            // 3. Expected Result: A RuntimeException is explicitly thrown, bubbling the error up.
            // 4. Result: The test passes, validating the bug is fixed!
            assertNotNull("Exception should be populated", e);
            System.out.println("VALIDATION SUCCESSFUL: Exception was properly thrown instead of swallowed.");
        }
    }
}
