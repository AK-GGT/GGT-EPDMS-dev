package com.ggt.epdm.service;

import com.ggt.epdm.model.Contact;
import com.ggt.epdm.model.DataSet;
import com.ggt.epdm.model.FlowCommon;
import com.ggt.epdm.model.Flowproperty;
import com.ggt.epdm.model.Lciamethod;
import com.ggt.epdm.model.PortalExportedXml;
import com.ggt.epdm.model.Process;
import com.ggt.epdm.model.Source;
import com.ggt.epdm.model.Unitgroup;
import com.ggt.epdm.model.Xmlfile;
import com.ggt.epdm.repository.*;
import com.ggt.epdm.utils.EcoPortalApiService;
import com.ggt.epdm.utils.SodaApiClientService;
import feign.Response;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.FileItem;

/**
 * @See DataExportService is responsible for executing service tasks to  push
 * the data to Eco Portal
 *
 *
 * @author Akshay Benny GGT
 * @since 1.0
 * 
 */

@Service
//@RequiredArgsConstructor
public class DataExportService {
	
    @Value("${ecoportal.node.username}")
    private String PORTAL_USERNAME;
    
    @Value("${ecoportal.node.password}")
    private String PORTAL_PASSWORD;

	private String SODA_NODE_RESPONSE_FORMAT = "xml";
	private String TEMP_FILE_LOCATION = "temp/response.xml";
	private String DATA_STOCK_UUID = "6f0072c0-4788-4701-bfc6-de3709cbd553";
	private static final Logger logger = LoggerFactory.getLogger(DataExportService.class);
	
	@Autowired
	private SodaApiClientService sodaApiClientService;

	@Autowired
	private EcoPortalApiService ecoPortalApiService;

	@Autowired
	private PortalExportedXmlRepository portalExportedXmlRepository;

	@Autowired
	private XmlfileRepository xmlfileRepository;

	@Autowired
	ProcessRepository processRepository;

	@Autowired
	FlowCommonRepository flowCommonRepository;

	@Autowired
	FlowpropertyRepository flowpropertyRepository;

	@Autowired
	UnitgroupRepository unitgroupRepository;

	@Autowired
	LciamethodRepository lciamethodRepository;

	@Autowired
	SourceRepository sourceRepository;

	@Autowired
	ContactRepository contactRepository;
	
	public String getEcoportalBearerToken() {

		String BEARER_TOKEN = "Bearer ";
		return BEARER_TOKEN+ecoPortalApiService.getAuthenticatedToken(PORTAL_USERNAME, PORTAL_PASSWORD);
	}

	public Object getAllPendingXmlFiles() throws FileNotFoundException, IOException {

		List<Xmlfile> pendingXmlFiles = xmlfileRepository.findXmlfileNotInPortalExportedXml();

		List<Long> ids = pendingXmlFiles.stream().map(Xmlfile::getId).collect(Collectors.toList());

		List<Process> pendingProcesses = processRepository.findAllByXmlfileIdIn(ids);

		List<FlowCommon> pendingFlows = flowCommonRepository.findAllByXmlfileIdIn(ids);

		List<Flowproperty> pendingFlowproperty = flowpropertyRepository.findAllByXmlfileIdIn(ids);

		List<Unitgroup> pendingUnitgroup = unitgroupRepository.findAllByXmlfileIdIn(ids);

		List<Lciamethod> pendingLciamethod = lciamethodRepository.findAllByXmlfileIdIn(ids);

		List<Source> pendingSource = sourceRepository.findAllByXmlfileIdIn(ids);

		List<Contact> pendingContact = contactRepository.findAllByXmlfileIdIn(ids);
		
		uploadDataSets(pendingProcesses);
		uploadDataSets(pendingFlows);
		uploadDataSets(pendingFlowproperty);
		uploadDataSets(pendingUnitgroup);
		uploadDataSets(pendingLciamethod);
		uploadDataSets(pendingSource);
		uploadDataSets(pendingContact);

		return "200";

	}
	
	private <T extends DataSet> void uploadDataSets(List<T> dataSets) throws FileNotFoundException, IOException {
		
		for (T dataSet : dataSets) 
		{
			String dataSetType = "";

			if (dataSet instanceof Lciamethod) {
			    dataSetType = "lciamethods";
			} else if (dataSet instanceof Source) {
			    dataSetType = "sources";
			} else if (dataSet instanceof FlowCommon) {
			    dataSetType = "flows";
			} else if (dataSet instanceof Unitgroup) {
			    dataSetType = "unitgroups";
			} else if (dataSet instanceof Flowproperty) {
			    dataSetType = "flowproperties";
			} else if (dataSet instanceof Contact) {
			    dataSetType = "contacts";
			} else if (dataSet instanceof Process) {
			    dataSetType = "processes";
			} else {
			    dataSetType = "";
			}

			Response response = (Response) sodaApiClientService.getDatasetById(dataSetType, dataSet.getUuid(), SODA_NODE_RESPONSE_FORMAT);
			
			if (response.status() == 200) 
			{
				// Save the XML to a file
				InputStream inputStream = response.body().asInputStream();
				File file = new File(TEMP_FILE_LOCATION);
				try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
					byte[] buffer = new byte[1024];
					int bytesRead;
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						fileOutputStream.write(buffer, 0, bytesRead);
					}
					logger.info("XML file received successfully.");

					FileItem fileItem = new DiskFileItem("mainFile", Files.probeContentType(file.toPath()), false,
							file.getName(), (int) file.length(), file.getParentFile());

					try {
						InputStream input = new FileInputStream(file);
						OutputStream os = fileItem.getOutputStream();
						IOUtils.copy(input, os);
					} catch (IOException ex) {
						logger.error(ex.getMessage());
					}

					MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
					Response portalResponse = null;
					String token = getEcoportalBearerToken();
					if(dataSet instanceof Lciamethod)
						portalResponse = ecoPortalApiService.saveLciamethodDataset(token, DATA_STOCK_UUID, multipartFile);
					else if(dataSet instanceof Source)
						portalResponse = ecoPortalApiService.saveSourceDataset(token, DATA_STOCK_UUID, multipartFile);
					else if(dataSet instanceof FlowCommon)
						portalResponse = ecoPortalApiService.saveFlowDataset(token, DATA_STOCK_UUID, multipartFile);
					else if(dataSet instanceof Unitgroup)
						portalResponse = ecoPortalApiService.saveUnitGroupDataset(token, DATA_STOCK_UUID, multipartFile);
					else if(dataSet instanceof Flowproperty)
						portalResponse = ecoPortalApiService.saveFlowPropertyDataset(token, DATA_STOCK_UUID, multipartFile);
					else if(dataSet instanceof Contact)
						portalResponse = ecoPortalApiService.saveContactDataset(token, DATA_STOCK_UUID, multipartFile);
					else if(dataSet instanceof Process)
						portalResponse = ecoPortalApiService.saveProcessesDataset(token, DATA_STOCK_UUID, multipartFile);
					
					if (portalResponse.status() == 200) {
						PortalExportedXml portalExportedXml = new PortalExportedXml();
						portalExportedXml.setXmlfile(dataSet.getXmlfile());
						portalExportedXml.setIsExported(true);
						portalExportedXml.setUuid(dataSet.getUuid());
						portalExportedXml.setDataSetType(dataSetType);
						portalExportedXmlRepository.save(portalExportedXml);
						logger.info("XML uploaded successfully.");
					}
					else {
						String responseBody = new String(portalResponse.body().asInputStream().readAllBytes(),
								StandardCharsets.UTF_8);
						logger.error(responseBody);
					}

				}

				catch (Exception e) {
					logger.error(e.getMessage());
				} finally {
					inputStream.close();
				}
				file.delete();
			} else {
				logger.error("Failed to get XML. HTTP Status: " + response.status());
			}
		}
		
	}
}