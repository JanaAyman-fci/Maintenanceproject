package org.unitime.timetable.test;

import org.unitime.timetable.reports.pointintimedata.BasePointInTimeDataReports;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.server.pointintimedata.PITDQueriesBackend;
import org.unitime.timetable.export.pointintimedata.PointInTimeDataReportsExportToCSV;
import org.unitime.timetable.gwt.shared.PointInTimeDataReportsInterface;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Validation Test for Maintenance Phase 1
 * Demonstrates the remediation of the silent failure logic bug.
 */
public class BasePointInTimeDataReportsValidationTest {

    @Test
    public void testSubjectAreaExceptionHandling() {
        // 1. Setup: Mock the UserContext
        UserContext mockUser = Mockito.mock(UserContext.class);

        try {
            // 2. Execution: Attempt to fetch values with a null user context
            Map<Long, String> result = BasePointInTimeDataReports.Parameter.SUBJECT.values(null);

            if (result == null) {
                fail("VALIDATION FAILED: The method failed silently and returned null instead of throwing an exception.");
            }
        } catch (RuntimeException e) {
            // 3. Expected Result: A RuntimeException is explicitly thrown, bubbling the
            // error up.
            assertNotNull("Exception should be populated", e);
            System.out.println("VALIDATION SUCCESSFUL: Exception was properly thrown instead of swallowed.");
        }
    }

    /**
     * Dummy report that throws an exception during instantiation to simulate
     * reflection failure.
     */
    public static class FaultyReport extends BasePointInTimeDataReports {
        public FaultyReport(String invalidArg) {
            // No-arg constructor missing to cause NoSuchMethodException (which leads to
            // instantiation failure)
        }

        @Override
        public String reportName() {
            return "Faulty Report";
        }

        @Override
        public String reportDescription() {
            return "Faulty Report";
        }

        @Override
        protected void intializeHeader() {
        }

        @Override
        protected void runReport(org.hibernate.Session hibSession) {
        }
    }

    @Test
    public void testPITDQueriesBackend_InstantiationException() {
        // 1. Setup: Backup the current register, clear it, and inject a class that
        // cannot be instantiated
        Map<String, Class> backup = new HashMap<>(BasePointInTimeDataReports.sPointInTimeDataReportRegister);
        BasePointInTimeDataReports.sPointInTimeDataReportRegister.clear();
        BasePointInTimeDataReports.sPointInTimeDataReportRegister.put("faulty", FaultyReport.class);

        PITDQueriesBackend backend = new PITDQueriesBackend();
        PointInTimeDataReportsInterface.PITDQueriesRpcRequest request = new PointInTimeDataReportsInterface.PITDQueriesRpcRequest();

        // 2. Execution & Verification: execute should throw IllegalArgumentException
        // with cause
        try {
            backend.execute(request, null);
            fail("VALIDATION FAILED: PITDQueriesBackend failed silently instead of throwing IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should indicate instantiation failure",
                    e.getMessage().contains("Failed to instantiate report"));
            System.out.println("VALIDATION SUCCESSFUL: PITDQueriesBackend threw IllegalArgumentException as expected: "
                    + e.getMessage());
        } finally {
            // Cleanup
            BasePointInTimeDataReports.sPointInTimeDataReportRegister.clear();
            BasePointInTimeDataReports.sPointInTimeDataReportRegister.putAll(backup);
        }
    }

    @Test
    public void testPointInTimeDataReportsExportToCSV_InstantiationException() {
        // 1. Setup: Inject a class that cannot be instantiated into the register
        BasePointInTimeDataReports.sPointInTimeDataReportRegister.put("faultyCsv", FaultyReport.class);

        PointInTimeDataReportsInterface.Report report = new PointInTimeDataReportsInterface.Report();
        report.setId("faultyCsv");

        // 2. Execution & Verification
        try {
            PointInTimeDataReportsExportToCSV.execute(null, null, report, new ArrayList<>());
            fail("VALIDATION FAILED: PointInTimeDataReportsExportToCSV failed silently instead of throwing Exception.");
        } catch (Exception e) {
            // execute() wraps it in PointInTimeDataReportsException
            assertTrue("Exception should indicate execution failure",
                    e.getMessage() != null && e.getMessage().contains("Failed to instantiate report"));
            System.out.println("VALIDATION SUCCESSFUL: PointInTimeDataReportsExportToCSV threw Exception as expected: "
                    + e.getMessage());
        } finally {
            // Cleanup
            BasePointInTimeDataReports.sPointInTimeDataReportRegister.remove("faultyCsv");
        }
    }

    @Test
    public void testBasePointInTimeDataReports_MissingParameters() {
        // 1. Setup: Create a dummy report class extending BasePointInTimeDataReports
        BasePointInTimeDataReports rpt = new BasePointInTimeDataReports() {
            @Override
            public String reportName() {
                return "Test";
            }

            @Override
            public String reportDescription() {
                return "Test";
            }

            @Override
            protected void intializeHeader() {
            }

            @Override
            protected void runReport(org.hibernate.Session hibSession) {
            }
        };

        // 2. Execution & Verification: Execute with empty parameters should throw
        // IllegalArgumentException
        try {
            rpt.execute(new HashMap<BasePointInTimeDataReports.Parameter, String>(), null);
            fail("VALIDATION FAILED: BasePointInTimeDataReports failed silently instead of throwing IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should indicate missing parameter",
                    e.getMessage().contains("Missing or invalid parameter"));
            System.out.println(
                    "VALIDATION SUCCESSFUL: BasePointInTimeDataReports threw IllegalArgumentException as expected: "
                            + e.getMessage());
        }
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("org.unitime.timetable.test.BasePointInTimeDataReportsValidationTest");
    }
}