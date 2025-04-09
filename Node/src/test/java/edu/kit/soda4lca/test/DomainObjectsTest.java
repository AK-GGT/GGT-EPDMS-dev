package edu.kit.soda4lca.test;

import de.fzk.iai.ilcd.service.client.impl.vo.epd.ProcessSubType;
import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.fzk.iai.ilcd.service.model.enums.TypeOfProcessValue;
import de.fzk.iai.ilcd.service.model.enums.TypeOfReviewValue;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.process.*;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;

public class DomainObjectsTest {

    public void createProcess() {
        Process p = new Process();
        p.setName(new MultiLangStringMapAdapter("LAVALKALI SULFATBESTANDIG cement", "en"));
        p.setType(TypeOfProcessValue.EPD);
        p.setSubType(ProcessSubType.SPECIFIC_DATASET);

        TimeInformation timeInformation = new TimeInformation();
        timeInformation.setReferenceYear(2017);
        timeInformation.setValidUntil(2022);
        timeInformation.setDescription(new MultiLangStringMapAdapter("06.10.2017 - 06.10.2022", "en"));
        p.setTimeInformation(timeInformation);

        Geography geography = new Geography();
        geography.setLocation("DK");
        geography.setDescription(new MultiLangStringMapAdapter("Markets: Norway / Europe", "en"));
        p.setGeography(geography);

        p.setTechnicalPurpose(new MultiLangStringMapAdapter("Grey Portland cement (CAS-Nr. 65997-15-1)&#xD;LOW-ALKALI cement (LAV-ALKALI SULFATBESTANDIG in Danish) is a grey Portland cement, with a strength class of 42.5 N with high sulphate-resistance and low heat generation. The LOW-ALKALI cement can be used for all purposes and in all environmental classes. The LOW-ALKALI cement is specially designed for concrete used for plant constructions and other structures prone to alkali reactions, bridges or structures in contact with sulphate-rich groundwater.", "en"));

        // here we're referencing a source dataset that is created together with and only referenced from this process dataset
        GlobalReference ds1 = new GlobalReference();
        ds1.setRefObjectId("7b19a4b7-c369-4c6a-966a-51956e80d9ad");
        ds1.setVersion("00.00.001");
        ds1.setType(GlobalReferenceTypeValue.SOURCE_DATA_SET);
        ds1.setShortDescription(new MultiLangStringMapAdapter("NEPD-1418-467-PDF", "en"));

        // here we're referencing a source dataset that is used for multiple process datasets
        GlobalReference ds2 = new GlobalReference();
        ds2.setRefObjectId("d82a6cdc-1930-4a8a-9b07-2f7fda03fcbd");
        ds2.setVersion("00.00.000");
        ds2.setType(GlobalReferenceTypeValue.SOURCE_DATA_SET);
        ds2.setShortDescription(new MultiLangStringMapAdapter("ecoinvent 3.2 database", "en"));

        // here we're referencing a source dataset that is used for multiple process datasets
        GlobalReference ds3 = new GlobalReference();
        ds3.setRefObjectId("b497a91f-e14b-4b69-8f28-f50eb1576766");
        ds3.setVersion("00.03.001");
        ds3.setType(GlobalReferenceTypeValue.SOURCE_DATA_SET);
        ds3.setShortDescription(new MultiLangStringMapAdapter("ecoinvent database (all versions)", "en"));

        p.getDataSources().add(ds1);
        p.getDataSources().add(ds2);
        p.getDataSources().add(ds3);

        Review review = new Review();
        review.setType(TypeOfReviewValue.INDEPENDENT_EXTERNAL_REVIEW);

        // here we're referencing a contact dataset that is used for multiple process datasets
        GlobalReference reviewer = new GlobalReference();
        reviewer.setRefObjectId("18498e14-c50d-437d-aab4-cc96c2a68aae");
        reviewer.setVersion("00.00.001");
        reviewer.setType(GlobalReferenceTypeValue.CONTACT_DATA_SET);
        reviewer.setShortDescription(new MultiLangStringMapAdapter("Linda Høibye", "en"));
        review.getReferencesToReviewers().add(reviewer);
        p.getReviews().add(review);

        p.setUseAdvice(new MultiLangStringMapAdapter("The declaration only considers cradle-to-gate environmental impacts, including modules A1-A3 as required in EN 15804.", "en"));

        // here we're referencing a contact dataset that is used for multiple process datasets
        GlobalReference ownerOfDataSet = new GlobalReference();
        ownerOfDataSet.setRefObjectId("6c3b4e4f-8b76-4fe4-b0a0-e2016a403da2");
        ownerOfDataSet.setVersion("00.00.003");
        ownerOfDataSet.setType(GlobalReferenceTypeValue.CONTACT_DATA_SET);
        ownerOfDataSet.setShortDescription(new MultiLangStringMapAdapter("Aalborg Portland A/S", "en"));
        p.setOwnerReference(ownerOfDataSet);

        Exchange e1 = new Exchange();

        // here we're referencing a flow dataset that is created together with and only referenced from this process dataset
        GlobalReference referenceFlow = new GlobalReference();
        referenceFlow.setRefObjectId("c94249b5-b440-4c1e-8d24-f1fc541009a9");
        referenceFlow.setVersion("00.00.002");
        referenceFlow.setType(GlobalReferenceTypeValue.FLOW_DATA_SET);
        referenceFlow.setShortDescription(new MultiLangStringMapAdapter("LAVALKALI SULFATBESTANDIG cement", "en"));
        e1.setFlowReference(referenceFlow);
        e1.setMeanAmount(Double.valueOf(1000));
        e1.setResultingAmount(Double.valueOf(1000));
        p.getExchanges().add(e1);
    }
}
