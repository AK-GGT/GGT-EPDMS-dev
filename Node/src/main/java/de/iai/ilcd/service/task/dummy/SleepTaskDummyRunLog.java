package de.iai.ilcd.service.task.dummy;

import de.iai.ilcd.service.task.abstractTask.runLogImplementations.DefaultRunLog;

public class SleepTaskDummyRunLog extends DefaultRunLog {

    SleepTaskDummyRunLog() {
        prepend("Good night world!");
        append("I always love a good nap!.");
    }

}
