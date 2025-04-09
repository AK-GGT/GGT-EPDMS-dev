package de.iai.ilcd.service.glad;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.rest.util.InvalidGLADUrlException;
import eu.europa.ec.jrc.lca.commons.dao.SearchParameters;
import eu.europa.ec.jrc.lca.commons.rest.dto.DataSetRegistrationResult;
import eu.europa.ec.jrc.lca.commons.service.exceptions.AuthenticationException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.NodeIllegalStatusException;
import eu.europa.ec.jrc.lca.commons.service.exceptions.RestWSUnknownException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the GLAD registration service object.
 *
 * @author sarai
 */
@Service("gladRegistrationService")
public class GLADRegistrationServiceImpl implements GLADRegistrationService {

    public static final int LARGE_WORKLOAD = 30; // If exceeded a background job is started.
    private final Logger log = LogManager.getLogger(GLADRegistrationServiceImpl.class);
    @Autowired
    private GLADRegistrationDataDao dao;


    public GLADRegistrationServiceImpl() throws SchedulerException {
        super();
    }

    public static DataSetRegistrationResult doRegister(DataSet dataSet, GLADRegistrationDataDao dao)
            throws NodeIllegalStatusException, AuthenticationException, RestWSUnknownException,
            InvalidGLADUrlException {
        DataSetRegistrationResult result;
        GLADRegistrationData grd = fetchGLADRegistrationData(dataSet, dao);

        RegistrationJobType jobType = determineJobType(dataSet, grd);
        if (jobType == RegistrationJobType.REJECTED) {
            result = DataSetRegistrationResult.REJECTED_NO_DIFFERENCE;
        } else {
            boolean isUpdate = (jobType == RegistrationJobType.UPDATE);
            result = GLADRestServiceBD.getInstance().registerDataSet(dataSet, isUpdate);

            if (result == DataSetRegistrationResult.ACCEPTED_PENDING)
                logRegistrationInformation(grd, dataSet, dao);
        }

        return result;
    }

    private static void logRegistrationInformation(GLADRegistrationData grd, DataSet dataSet,
                                                   GLADRegistrationDataDao dao) {
        if (grd == null)
            grd = new GLADRegistrationData();

        grd.setUuid(dataSet.getUuidAsString());
        grd.setVersion(dataSet.getVersion());
        dao.saveOrUpdate(grd);
    }

    private static RegistrationJobType determineJobType(DataSet dataSet, GLADRegistrationData dataSetGLAD) {
        RegistrationJobType jobType;

        try {
            if (dataSetGLAD == null)
                jobType = RegistrationJobType.NEW;
            else if (dataSetGLAD.getVersion().compareTo(dataSet.getVersion()) >= 0)
                jobType = RegistrationJobType.REJECTED;
            else
                jobType = RegistrationJobType.UPDATE;

        } catch (Exception e) {
            jobType = RegistrationJobType.REJECTED;
        }

        return jobType;
    }

    private static GLADRegistrationData fetchGLADRegistrationData(DataSet dataSet, GLADRegistrationDataDao dao) {
        GLADRegistrationData grd;
        try {
            grd = dao.findByUUID(dataSet.getUuidAsString());
        } catch (NoResultException nre) {
            grd = null;
        }
        return grd;
    }

    public static void doDeregister(DataSet dataSet, GLADRegistrationDataDao dao) throws RestWSUnknownException, AuthenticationException {
        GLADRegistrationData regData = null;
        try {
            regData = dao.findByUUID(dataSet.getUuidAsString());
            if (regData != null) {
                GLADRestServiceBD.getInstance().deregisterDataSet(dataSet.getUuidAsString());
                dao.remove(regData.getId());
            }
        } catch (RestWSUnknownException re) {
            GLADRestServiceBD.getInstance().deregisterDataSet(dataSet.getUuidAsString());
            dao.remove(regData.getId());

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GLADRegistrationData> getRegistered() {
        return dao.getRegistered();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSetRegistrationResult> register(List<Process> processes) throws RestWSUnknownException,
            AuthenticationException, NodeIllegalStatusException, InvalidGLADUrlException {
        List<DataSetRegistrationResult> resultList = new ArrayList<>();

        for (Process process : processes) {
            DataSetRegistrationResult result = doRegister(process, dao);
            resultList.add(result);
        }
        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deregister(List<Process> processes) {
        for (Process process : processes) {
            try {
                doDeregister(process, dao);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GLADRegistrationData> getListOfRegistrations(Process process) {
        return dao.getListOfRegistrations(process);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GLADRegistrationData> loadLazy(SearchParameters sp) {
        return dao.find(sp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long count(SearchParameters sp) {
        return dao.count(sp);
    }

    private enum RegistrationJobType {
        UPDATE, NEW, REJECTED
    }

}
