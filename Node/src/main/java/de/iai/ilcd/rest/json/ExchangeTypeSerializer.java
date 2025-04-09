package de.iai.ilcd.rest.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.TypeOfFlowValue;
import de.iai.ilcd.model.common.Classification;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.dao.FlowDao;
import de.iai.ilcd.model.flow.*;
import it.jrc.lca.ilcd.process.ExchangeType;
import it.jrc.lca.ilcd.process.ProcessDataSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A custom serializer for the Exchanges that conditionally add a few additional computed field to the extended JSON.
 * <p>
 * Most of <code>serialize</code> is trying to mimick the default behavior of Jackson when dealing with an ordinary ExchangeType <b>WITHOUT</b>
 * injecting this module. the conditional behavior start from <code>writeFlowProperty</code>
 *
 * @author MK
 * @since soda4LCA 6.8.1
 */
public class ExchangeTypeSerializer extends JsonSerializer<ExchangeType> {

    private final Logger logger = LogManager.getLogger(ExchangeTypeSerializer.class);

    private final ProcessDataSet process;

    private final Function<Supplier<Object>, Object> peek = objectSupplier -> {
        try {
            return objectSupplier.get();

        } catch (Exception e) {
            if (logger.isTraceEnabled())
                logger.trace("Exception thrown when trying to read property", e);
            return null;
        }
    };

    /**
     * When serializing an Exchange, we need to ask the process itself a few questions.
     * Mainly, is this exchange has a flow that is a marked in the QuantitativeReference? (aka ReferenceToReferenceFlows).
     * <p>
     * It's possible to replace the whole <code>ProcessDataset</code> with List of BigInteger for the referenceToReferenceFlows.
     * But a decision has been made to keep the whole process for possible further modifications.
     *
     * @param process process data set
     */
    public ExchangeTypeSerializer(ProcessDataSet process) {
        this.process = process;
    }

    @Override
    public void serialize(ExchangeType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        gen.writeStartObject();

        writeNumber(gen, "dataSetInternalID", value.getDataSetInternalID());
        writeObject(gen, "referenceToFlowDataSet", value.getReferenceToFlowDataSet());
        writeString(gen, "location", value.getLocation());

        writeString(gen, "exchange direction", value.getExchangeDirection());
        writeString(gen, "referenceToVariable", value.getReferenceToVariable());

        writeNumber(gen, "meanAmount", value.getMeanAmount());
        writeNumber(gen, "resultingAmount", value.getResultingAmount());
        writeNumber(gen, "minimumAmount", value.getMinimumAmount());
        writeNumber(gen, "maximumAmount", value.getMaximumAmount());

        // TODO: use value instead of toString for enums
        writeString(gen, "uncertaintyDistributionType", value.getUncertaintyDistributionType());
        writeString(gen, "relativeStandardDeviation95In", value.getRelativeStandardDeviation95In());

        writeObject(gen, "allocations", value.getAllocations());
        writeString(gen, "dataSourceType", value.getDataSourceType());
        writeString(gen, "dataDerivationTypeStatus", value.getDataDerivationTypeStatus());
        writeObject(gen, "referencesToDataSource", value.getReferencesToDataSource());

        if (value.getGeneralComments() != null && !value.getGeneralComments().isEmpty())
            writeObject(gen, "generalComments", value.getGeneralComments());
        writeObject(gen, "other", value.getOther());

        // TODO: is this null safe? if the reference doesn't exist, then you have bigger problems
        // for each...

        if (process.getProcessInformation().getQuantitativeReference().getReferenceToReferenceFlows()
                .contains(value.getDataSetInternalID())) {
            writeBoolean(gen, "referenceFlow", true);
        }

        String flowUUID = (String) peek.apply(() -> value.getReferenceToFlowDataSet().getRefObjectId());
        if (flowUUID == null) {
            logger.warn(String.format("Failed to retrieve reference flow uuid from exchange with internal id: %s", value.getDataSetInternalID()));
        } else {

            DataSetVersion version = null;
            try {
                version = DataSetVersion.parse(value.getReferenceToFlowDataSet().getVersion());
            } catch (FormatException fe) {
                logger.debug("Exchange flow {} has no, or broken version, proceeding without", flowUUID, fe);
            }

            Flow f;
            if (version != null) {
                f = FlowDao.lookupFlow(flowUUID, version);
                if (f == null) {
                    logger.debug("Looking up *latest* flow {} ", flowUUID);
                    f = FlowDao.lookupFlow(flowUUID);
                }
            } else {
                logger.debug("Looking up *latest* flow {} ", flowUUID);
                f = FlowDao.lookupFlow(flowUUID);
            }

            if (f == null) {
                logger.debug("Flow {} not included (Couldn't be found in db).", flowUUID);
            } else {
                if (f.getVersion() != null && !f.getVersion().equals(version)) {
                    writeString(gen, "resolvedFlowVersion", f.getDataSetVersion());
                }

                FlowPropertyDescription rfp = f.getReferenceFlowProperty();
                if (rfp != null)
                    writeNumber(gen, "resultingflowAmount", value.getMeanAmount() * rfp.getMeanValue());

                writeFlowProperties(gen, f);

                if (f instanceof ProductFlow)
                    writeMaterialProperties(gen, (ProductFlow) f);

                TypeOfFlowValue typeOfFlowValue = f.getType();
                writeString(gen, "typeOfFlow", (typeOfFlowValue != null) ? typeOfFlowValue.getValue() : null);

                if (f instanceof ElementaryFlow)
                    writeClassification(gen, ((ElementaryFlow) f).getCategorization());
                else
                    writeClassification(gen, f.getClassification());

                IMultiLangString location = f.getLocationOfSupply();
                writeString(gen, "locationOfSupply", (location != null) ? location.getValue() : null);
            }
        }

        gen.writeEndObject();


    }

    private void writeMaterialProperties(@Nonnull JsonGenerator gen, @Nonnull ProductFlow pf) throws IOException {
        Set<MaterialProperty> mps = pf.getMaterialProperties();
        if (mps == null || mps.isEmpty())
            return;

        try {
            gen.writeArrayFieldStart("materialProperties");
        } catch (IOException e) {
            logger.error("Failed to write material properties", e);
            return;
        }

        try {
            for (MaterialProperty mp : mps) {
                gen.writeStartObject();
                MaterialPropertyDefinition mpDef = mp.getDefinition();
                writeString(gen, "name", peek.apply(mpDef::getName));
                writeString(gen, "value", peek.apply(mp::getValue));
                writeString(gen, "unit", peek.apply(mpDef::getUnit));
                writeString(gen, "unitDescription", peek.apply(mpDef::getUnitDescription));
                gen.writeEndObject();
            }
        } finally {
            gen.writeEndArray();
        }
    }


    /**
     * @param gen  json generator
     * @param flow flow
     */
    private void writeFlowProperties(@Nonnull JsonGenerator gen, @Nonnull Flow flow) throws IOException {
        try {
            gen.writeArrayFieldStart("flowProperties");

            if (flow.getPropertyDescriptions() != null)
                for (FlowPropertyDescription fp : flow.getPropertyDescriptions()) {
                    String fp_uuid = (String) peek.apply(() -> fp.getFlowPropertyRef().getUuid().getUuid());
                    try {
                        gen.writeStartObject();

                        writeObject(gen, "name", peek.apply(() -> fp.getName().getLStrings()));
                        writeString(gen, "uuid", fp_uuid);
                        if (fp_uuid.equalsIgnoreCase((String) peek.apply(() -> flow.getReferenceFlowProperty().getReference().getUuid().getUuid())))
                            writeBoolean(gen, "referenceFlowProperty", true);

                        writeNumber(gen, "meanValue", fp.getMeanValue());
                        writeString(gen, "referenceUnit", fp.getFlowPropertyUnit());
                        writeString(gen, "unitGroupUUID", peek.apply(() -> fp.getFlowProperty().getReferenceToUnitGroup().getUuid().getUuid()));

                        gen.writeEndObject();
                    } catch (Exception e) {
                        logger.error("Error writing flow property description with uuid: " + fp_uuid, e);
                    }
                }

        } catch (Exception e) { // NPE fallback
            logger.debug("exception rendering flow properties, partial stack trace: " + e.getStackTrace()[0].toString());
            gen.writeEndObject();
        } finally {
            gen.writeEndArray();
        }
    }

    private void writeString(@Nonnull JsonGenerator gen, String key, Object value) {
        if (value == null) return;
        try {
            gen.writeStringField(key, value.toString());
        } catch (Exception e) {
            // ignore
        }
    }

    private void writeNumber(@Nonnull JsonGenerator gen, String key, BigInteger value) {
        if (value == null) return;
        try {
            gen.writeNumberField(key, value);
        } catch (Exception e) {
            // ignore
        }
    }

    private void writeNumber(@Nonnull JsonGenerator gen, String key, Double value) {
        if (value == null) return;
        try {
            gen.writeNumberField(key, value);
        } catch (Exception e) {
            // ignore
        }
    }

    private void writeObject(@Nonnull JsonGenerator gen, String key, Object value) {
        if (value == null) return;
        try {
            gen.writeObjectField(key, value);
        } catch (Exception e) {
            // ignore
        }
    }

    private void writeBoolean(@Nonnull JsonGenerator gen, String key, Boolean value) {
        if (value == null) return;
        try {
            gen.writeBooleanField(key, value);
        } catch (Exception e) {
            // ignore
        }
    }

    private void writeClassification(@Nonnull JsonGenerator gen, Classification classification) throws IOException {
        try {
            if (classification != null) {
                Map<String, String> m = new HashMap<>();
                m.put("name", classification.getName());
                m.put("classHierarchy", classification.getClassHierarchyAsString());
                writeObject(gen, "classification", m);
            }
        } catch (Exception e) {
            logger.error("partial stack trace: " + e.getStackTrace()[0].toString());
            gen.writeEndObject();
        }
    }

}
