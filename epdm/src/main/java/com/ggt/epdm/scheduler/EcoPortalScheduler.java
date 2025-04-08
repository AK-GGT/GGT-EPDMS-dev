package com.ggt.epdm.scheduler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ggt.epdm.service.DataExportService;

/**
 * SchedulerService is responsible for executing periodic tasks such as 
 * data cleanup, reporting, or syncing with external systems.
 *
 * This service uses Spring's @Scheduled annotation to automate task execution 
 * at fixed intervals based on CRON expressions.
 *
 * <p>Scheduling intervals:</p>
 * <ul>
 *   <li>Every 15 minutes</li>
 *   <li>Every 1 hour</li>
 *   <li>Every 12 hours</li>
 *   <li>Every 1 day (midnight)</li>
 *   <li>Every 1st of the month</li>
 * </ul>
 *
 *
 * @author Akshay Benny GGT
 * @since 1.0
 * 
 */

@Component
public class EcoPortalScheduler {
	
	private static final Logger logger = LoggerFactory.getLogger(EcoPortalScheduler.class);

	@Autowired
	private DataExportService dataExportService;

	// @Scheduled(cron = "0 0 * * * *") // runs every 1 hour
	// @Scheduled(cron = "0 0 0,12 * * *") // runs every 12 hour
	// @Scheduled(cron = "0 0 0 * * *") // runs every day midnight
	// @Scheduled(cron = "0 0 0 1 * *") // runs every month
	@Scheduled(cron = "0 */15 * * * *") // runs every 15 minutes
	public void runTask() throws FileNotFoundException, IOException {
		logger.info(
				"****Started Scheduled Task to push Eco Portal Data at " + Instant.now() + "****");
		
		dataExportService.getAllPendingXmlFiles();
		
		logger.info(
				"****Finished Scheduled Task to push Eco Portal Data at " + Instant.now() + "****");
	}
}