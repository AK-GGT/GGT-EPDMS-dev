package de.iai.ilcd.service.task.dataSetAssignment;


import de.iai.ilcd.model.datastock.DataStockMetaData;
import de.iai.ilcd.service.task.dataSetAssignment.assign.AssignBatchMethod;
import de.iai.ilcd.service.task.dataSetAssignment.unassign.UnassignBatchMethod;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.function.BiFunction;

public enum AssignUnassignTaskType {

    ATTACH("Attaching", "to", AssignBatchMethod::new),
    DETACH("Detaching", "from", UnassignBatchMethod::new);

    String verb;

    String preposition;

    BiFunction<JdbcTemplate, DataStockMetaData, AssignUnassignBatchMethod> batchMethodSupplier;

    AssignUnassignTaskType(String verb, String preposition,
                           BiFunction<JdbcTemplate, DataStockMetaData, AssignUnassignBatchMethod> batchMethodSupplier) {
        this.verb = verb;
        this.preposition = preposition;
        this.batchMethodSupplier = batchMethodSupplier;
    }

    public String getVerb() {
        return verb;
    }

    public String getPreposition() {
        return preposition;
    }

    public AssignUnassignBatchMethod getBatchMethod(JdbcTemplate jdbcTemplate, DataStockMetaData targetMeta) {
        return batchMethodSupplier.apply(jdbcTemplate, targetMeta);
    }

}
