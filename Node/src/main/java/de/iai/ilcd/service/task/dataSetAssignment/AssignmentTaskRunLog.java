package de.iai.ilcd.service.task.dataSetAssignment;

import de.iai.ilcd.service.task.LogLevel;
import de.iai.ilcd.service.task.abstractTask.runLogImplementations.DataSetReferencingRunLog;
import de.iai.ilcd.util.result.ResultType;

public class AssignmentTaskRunLog extends DataSetReferencingRunLog {

    public AssignmentTaskRunLog(LogLevel... logLevelsToIgnore) {
        super(logLevelsToIgnore);
    }

    @Override
    protected void determineLogLevelToResultTypeTranslations() {
        super.determineLogLevelToResultTypeTranslations();
        logLeveLInterpreter.put(LogLevel.WARNING, ResultType.PARTIALLY_SUCCESSFUL); // Override default (QUIET_ERROR)
    }
}
