# UML Diagram (Based on Reverse Engineering)
The following class diagram represents the **Point-In-Time Data Reporting Subsystem** (`org.unitime.timetable.reports.pointintimedata`), which is responsible for generating various scheduling and utilization reports.

```mermaid
classDiagram
    class ParameterImplementation {
        <<interface>>
        +getValues(UserContext) Map
        +getDefaultValue(UserContext) String
        +parseSetValue(String) ArrayList
    }

    class BasePointInTimeDataReports {
        <<abstract>>
        #pointInTimeDataUniqueId : Long
        #parameters : ArrayList~Parameter~
        +execute(HashMap, Session) ArrayList
        #runReport(Session)*
        #intializeHeader()*
        #parseParameters()
    }

    class AllWSCHByDepartment {
        +reportName() String
        +reportDescription() String
        #runReport(Session)
        #intializeHeader()
    }

    class RoomUtilization {
        +reportName() String
        +reportDescription() String
        #runReport(Session)
        #intializeHeader()
    }
    
    class WSCHByDepartment {
        +reportName() String
        +reportDescription() String
        #runReport(Session)
        #intializeHeader()
    }

    class Parameter {
        <<enumeration>>
        PITD
        SESSION
        DEPARTMENT
        SUBJECT
        BUILDING
        ROOM
        +values(UserContext) Map
        +defaultValue(UserContext) String
    }

    BasePointInTimeDataReports <|-- AllWSCHByDepartment
    BasePointInTimeDataReports <|-- RoomUtilization
    BasePointInTimeDataReports <|-- WSCHByDepartment
    ParameterImplementation <|-- Parameter
    BasePointInTimeDataReports --> Parameter : uses
```
