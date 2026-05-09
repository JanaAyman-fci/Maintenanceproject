# Activity Diagrams: Exception Handling Refactoring

The following Activity Diagrams illustrate the control flow changes made to remediate the three logic-related bugs (silent failure paths and empty catch blocks) in the Point-In-Time Data reporting system. Each diagram contrasts the **Before** (bugged) state with the **After** (fixed) state.

## 1. Change 1: Fetching User Subject Areas (`Parameter.SUBJECT.getValues`)
This change addresses a silent failure where a database exception while fetching subject areas was swallowed, returning a null map and potentially causing NullPointerExceptions downstream.

```mermaid
---
title: Change 1 - Fetching User Subject Areas
---
stateDiagram-v2
    direction TB
    
    state "Before (Silent Failure)" as Before {
        [*] --> TryFetch1
        TryFetch1: Query SubjectArea.getUserSubjectAreas()
        TryFetch1 --> IterateSubjects: Success
        IterateSubjects --> AddToMap1
        AddToMap1 --> ReturnMap1
        
        TryFetch1 --> CatchBlock1: Exception Thrown
        CatchBlock1: catch (Exception e)
        CatchBlock1 --> ReturnNull1: return null;
        ReturnNull1 --> [*]: (Fails silently downstream)
    }

    state "After (Robust Handling)" as After {
        [*] --> TryFetch2
        TryFetch2: Query SubjectArea.getUserSubjectAreas()
        TryFetch2 --> IterateSubjects2: Success
        IterateSubjects2 --> AddToMap2
        AddToMap2 --> ReturnMap2
        
        TryFetch2 --> CatchBlock2: Exception Thrown
        CatchBlock2: catch (Exception e)
        CatchBlock2 --> LogError2: Logger.error(...)
        LogError2 --> ThrowRuntimeException: throw new RuntimeException(e)
        ThrowRuntimeException --> [*]: (Fails fast, triggers Error UI)
    }
```

## 2. Change 2: Parameter Parsing (`parseSetValue`)
This change addresses the parsing of user-submitted parameter strings (e.g., parsing `Long` or `Float` values). Previously, an empty catch block might have ignored invalid number formats, resulting in incomplete parameter lists.

```mermaid
---
title: Change 2 - Parsing Parameter Values
---
stateDiagram-v2
    direction TB
    
    state "Before (Empty Catch Block)" as Before2 {
        [*] --> SplitString1
        SplitString1: Split string by comma
        SplitString1 --> TryParse1
        TryParse1: Long.parseLong(value)
        TryParse1 --> AddToList1: Success
        AddToList1 --> SplitString1: Next value
        
        TryParse1 --> EmptyCatch: NumberFormatException
        EmptyCatch: catch (Exception e) { /* empty */ }
        EmptyCatch --> SplitString1: (Ignores error, skips value)
        
        SplitString1 --> ReturnList1: No more values
        ReturnList1 --> [*]
    }

    state "After (Explicit Validation Error)" as After2 {
        [*] --> SplitString2
        SplitString2: Split string by comma
        SplitString2 --> TryParse2
        TryParse2: Long.parseLong(value)
        TryParse2 --> AddToList2: Success
        AddToList2 --> SplitString2: Next value
        
        TryParse2 --> CatchAndThrow: NumberFormatException
        CatchAndThrow: catch (NumberFormatException e)
        CatchAndThrow --> LogParseError: Logger.warn(...)
        LogParseError --> ThrowIllegalArgument: throw new IllegalArgumentException()
        ThrowIllegalArgument --> [*]: (Rejects invalid input)
        
        SplitString2 --> ReturnList2: No more values
        ReturnList2 --> [*]
    }
```

## 3. Change 3: Report Execution Query (`runReport` Data Fetching)
This change addresses the execution of the main reporting queries where database or session exceptions were caught and swallowed, resulting in the return of an empty report rather than alerting the user to a system failure.

```mermaid
---
title: Change 3 - Report Execution Query
---
stateDiagram-v2
    direction TB
    
    state "Before (Swallowed Query Exception)" as Before3 {
        [*] --> TryExecuteQuery1
        TryExecuteQuery1: hibSession.createQuery(...)
        TryExecuteQuery1 --> ProcessResults1: Success
        ProcessResults1 --> ReturnReport1: ArrayList<String[]>
        
        TryExecuteQuery1 --> SwallowedCatch: HibernateException
        SwallowedCatch: catch (Exception e) { e.printStackTrace(); }
        SwallowedCatch --> ReturnEmptyReport: (Returns empty dataset)
        ReturnEmptyReport --> [*]: (User assumes no data exists)
    }

    state "After (Report Execution Failure)" as After3 {
        [*] --> TryExecuteQuery2
        TryExecuteQuery2: hibSession.createQuery(...)
        TryExecuteQuery2 --> ProcessResults2: Success
        ProcessResults2 --> ReturnReport2: ArrayList<String[]>
        
        TryExecuteQuery2 --> ExplicitCatch: HibernateException
        ExplicitCatch: catch (HibernateException e)
        ExplicitCatch --> LogQueryError: Logger.error("Query failed", e)
        LogQueryError --> ThrowReportException: throw new ReportGenerationException(e)
        ThrowReportException --> [*]: (Alerts user to system error)
    }
```
