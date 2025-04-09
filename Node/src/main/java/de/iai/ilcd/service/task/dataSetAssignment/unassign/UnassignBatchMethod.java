package de.iai.ilcd.service.task.dataSetAssignment.unassign;

import de.iai.ilcd.model.dao.DataSetDaoType;
import de.iai.ilcd.model.datastock.DataStockMetaData;
import de.iai.ilcd.service.task.dataSetAssignment.AssignUnassignBatchMethod;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UnassignBatchMethod extends AssignUnassignBatchMethod {

    public UnassignBatchMethod(DataStockMetaData targetMeta) {
        super(targetMeta);
    }

    public UnassignBatchMethod(JdbcTemplate jdbcTemplate, DataStockMetaData targetMeta) {
        super(jdbcTemplate, targetMeta);
    }

    @Override
    protected String generateSingleQueryStringForType(DataSetDaoType daoType) {
        return "DELETE FROM " +
                getTableNameResolver().get(daoType) +
                "WHERE " +
                getDataSetIdColumnNameResolver().get(daoType) + "= ? AND " +
                getDataStockIdColumnNameResolver().get(daoType) + "= ?";
    }

    @Override
    protected BatchPreparedStatementSetter getBatchPreparedStatementSetter(List<Long> dataSetIds) {
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, dataSetIds.get(i)); // data set id column first
                ps.setLong(2, getTargetMeta().getId()); // set stock id column second
            }

            @Override
            public int getBatchSize() {
                return dataSetIds.size();
            }
        };
    }
}
