package de.iai.ilcd.rest.util;

import com.google.gson.*;
import de.fzk.iai.ilcd.api.binding.generated.categories.DataSetType;
import de.fzk.iai.ilcd.service.model.*;
import de.fzk.iai.ilcd.service.model.common.IClassification;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.fzk.iai.ilcd.service.model.enums.PublicationTypeValue;
import de.fzk.iai.ilcd.service.model.enums.TypeOfProcessValue;
import de.fzk.iai.ilcd.service.model.flowproperty.IUnitGroupType;
import de.fzk.iai.ilcd.service.model.process.IComplianceSystem;
import de.fzk.iai.ilcd.service.model.process.IReferenceFlow;
import de.fzk.iai.ilcd.service.model.process.ITimeInformation;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.flow.FlowPropertyDescription;
import de.iai.ilcd.model.unitgroup.Unit;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import de.iai.ilcd.model.utils.DatasourcesFilterUtil;
import de.iai.ilcd.util.SodaUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * <p>
 * Serializer for PrimeUI lists. Register in a {@link GsonBuilder} via {@link #register(GsonBuilder, String, boolean)}.
 * </p>
 * <p>
 * The generated JSON has the fields: <code>startIndex, pageSize, totalCount, data</code>, where <code>data</code> is an
 * array of data sets (ready for PrimeUI data table).
 * </p>
 * <p>
 * JSON fields set for the various data set types are:
 * <ul>
 * <li>Process: <code>dsType, name, classific, geo, refYear, validUntil</code></li>
 * <li>Flow: <code>dsType, name, classific, refProp, refPropUnit</code></li>
 * <li>Flow Property: <code>dsType, name, classific, defUnitGrp, defUnit</code></li>
 * <li>LCIA method: <code>dsType, name, type, refYear, dur</code></li>
 * <li>Unit group: <code>dsType, name, classific, defUnit</code></li>
 * <li>Source: <code>dsType, name, classific, pubType</code></li>
 * <li>Contact: <code>dsType, name, classific, email, www</code></li>
 * </ul>
 * </p>
 * <p>
 * Example for a Flow:
 * <pre>
 * { "startIndex":0,
 *   "pageSize":1,
 *   "totalCount":210,
 *   "data":[
 *    {"name":"Demo Name 1","type":"Product flow","classific":"My / Classification / Path","refProp":"Mass","refPropUnit":"kg","dsType":"Flow"},
 *      {"name":"Demo Name 2","type":"Product flow","classific":"My / Classification / Path","refProp":"Mass","refPropUnit":"kg","dsType":"Flow"}
 *   ]
 * }
 * </pre>
 * <p>
 */
public class JSONDatasetListSerializer implements JsonSerializer<JSONDatasetList> {

    private boolean langFallback = false;

    private String lang = null;

    /**
     * Private constructor, use {@link #register(GsonBuilder, String, boolean)}
     */
    private JSONDatasetListSerializer() {
    }

    /**
     * Register serializer in provided builder. Serialization of a list
     * must be performed by passing an instance of the wrapper class {@link JSONDatasetList} to {@link Gson}.
     *
     * @param gsonBuilder GSON builder
     */
    public static void register(GsonBuilder gsonBuilder, String language, boolean langFallback) {
        JSONDatasetListSerializer s = new JSONDatasetListSerializer();
        s.setLang(language);
        s.setLangFallback(langFallback);
        gsonBuilder.registerTypeAdapter(JSONDatasetList.class, s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonElement serialize(JSONDatasetList src, Type type, JsonSerializationContext context) {
        JsonObject list = new JsonObject();

        list.addProperty("startIndex", src.getStartIndex());
        list.addProperty("pageSize", src.getPageSize());
        list.addProperty("totalCount", src.getTotalCount());

        JsonArray data = new JsonArray();
        for (IDataSetListVO listvo : src.getDataSets()) {
            JsonObject obj = new JsonObject();
            DataSetType dsType;
            if (listvo instanceof IProcessListVO) {
                dsType = this.serialize(obj, (IProcessListVO) listvo);
            } else if (listvo instanceof IFlowListVO) {
                dsType = this.serialize(obj, (IFlowListVO) listvo);
            } else if (listvo instanceof IFlowPropertyListVO) {
                dsType = this.serialize(obj, (IFlowPropertyListVO) listvo);
            } else if (listvo instanceof ILCIAMethodListVO) {
                dsType = this.serialize(obj, (ILCIAMethodListVO) listvo);
            } else if (listvo instanceof IUnitGroupListVO) {
                dsType = this.serialize(obj, (IUnitGroupListVO) listvo);
            } else if (listvo instanceof ISourceListVO) {
                dsType = this.serialize(obj, (ISourceListVO) listvo);
            } else if (listvo instanceof IContactListVO) {
                dsType = this.serialize(obj, (IContactListVO) listvo);
            } else if (listvo instanceof ILifeCycleModelListVO) {
                dsType = this.serialize(obj, (ILifeCycleModelListVO) listvo);
            } else {
                // prevent adding of empty object
                continue;
            }
            obj.addProperty("dsType", dsType.getValue());
            data.add(obj);
        }

        list.add("data", data);

        return list;
    }

    /**
     * Serialize a process list value object to JSON
     *
     * @param obj     object to add properties
     * @param subject source object
     * @return subject data set type
     */
    private DataSetType serialize(JsonObject obj, IProcessListVO subject) {

        JsonArray languages = new JsonArray();
        if (subject.getLanguages() != null) {
            for (String lang : subject.getLanguages())
                languages.add(lang);
            obj.add("languages", languages);
        }

        if (subject.getMetaDataOnly() != null && subject.getMetaDataOnly())
            obj.addProperty("metaDataOnly", subject.getMetaDataOnly());

        this.addProperty(obj, "name", subject.getName());
        obj.addProperty("uuid", subject.getUuidAsString());
        obj.addProperty("version", subject.getDataSetVersion());
        obj.addProperty("geo", subject.getLocation());

        IClassification classification = subject.getClassification();
        if (classification != null) {
            obj.addProperty("classific", classification.getClassHierarchyAsString());
            obj.addProperty("classificId", SodaUtil.getHighestClassId(classification));
            obj.addProperty("classificSystem", classification.getName());
        }

        TypeOfProcessValue type = subject.getType();
        if (type != null) {
            obj.addProperty("type", type.getValue());
        }

        if (ConfigurationService.INSTANCE.isShowReferenceProducts()) {
            List<IReferenceFlow> refFlowsList = subject.getReferenceFlows();

            JsonArray refFlows = new JsonArray();
            for (IReferenceFlow flow : refFlowsList) {
                JsonObject dsJson = new JsonObject();
                dsJson.addProperty("name", flow.getFlowName().getDefaultValue());
                dsJson.addProperty("unit", flow.getUnit());
                dsJson.addProperty("meanValue", flow.getMeanValue());
                dsJson.addProperty("uuid", flow.getReference().getRefObjectId());
                refFlows.add(dsJson);
            }
            obj.add("referenceFlows", refFlows);
        }

        ITimeInformation ti = subject.getTimeInformation();
        if (ti != null) {
            obj.addProperty("refYear", ti.getReferenceYear());
            obj.addProperty("validUntil", ti.getValidUntil());
        }

        try {
            obj.add("compliance", this.getComplianceSystems(subject.getComplianceSystems()));
        } catch (Exception e1) {
        }

        try {
            obj.addProperty("owner", subject.getOwnerReference().getShortDescription().getDefaultValue());
        } catch (Exception e1) {
        }

        try {
            obj.addProperty("subType", subject.getSubType().getValue());
        } catch (Exception e) {
        }

        try {
            if (!subject.getDataSources().isEmpty()) {
                List<IGlobalReference> dataSourceList = new ArrayList<>();
                DatasourcesFilterUtil dfu = new DatasourcesFilterUtil();
                dataSourceList = dfu.filterDataSources(subject.getDataSources());

                JsonArray dataSources = new JsonArray();
                for (IGlobalReference dsRef : dataSourceList) {
                    JsonObject dsJson = new JsonObject();
                    dsJson.addProperty("uuid", dsRef.getRefObjectId());
                    dsJson.addProperty("name", dsRef.getShortDescription().getDefaultValue());
                    dataSources.add(dsJson);
                }
                obj.add("dataSources", dataSources);
            }
        } catch (Exception e) {
        }

        try {
            if (subject.getRegistrationNumber() != null) {
                obj.addProperty("regNo", subject.getRegistrationNumber());
            }
        } catch (Exception e1) {
        }

        try {
            if (subject.getRegistrationAuthority() != null) {
                JsonObject regAuth = new JsonObject();
                regAuth.addProperty("uuid", subject.getRegistrationAuthority().getRefObjectId());
                regAuth.addProperty("name", subject.getRegistrationAuthority().getShortDescription().getDefaultValue());
                obj.add("regAuthority", regAuth);
            }
        } catch (Exception e1) {
        }

        if (subject.getSourceId() != null)
            obj.addProperty("nodeid", subject.getSourceId());

        obj.addProperty("uri", subject.getHref());

        if (subject.getDuplicates() != null && subject.getDuplicates().size() > 0) {
            JsonArray duplicates = new JsonArray();
            for (IDataSetListVO ds : subject.getDuplicates()) {
                JsonObject dupeJson = new JsonObject();
                serialize(dupeJson, (IProcessListVO) ds);
                duplicates.add(dupeJson);
            }

            obj.add("duplicates", duplicates);
        }

        return DataSetType.PROCESS;
    }

    private JsonElement getComplianceSystems(Set<IComplianceSystem> csList) {
        JsonArray data = new JsonArray();
        for (IComplianceSystem cs : csList) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", cs.getName());
            obj.addProperty("uri", cs.getReference().getUri());
            obj.addProperty("uuid", cs.getReference().getRefObjectId());
            data.add(obj);
        }

        return data;
    }

    /**
     * Serialize a flow list value object to JSON
     *
     * @param obj     object to add properties
     * @param subject source object
     * @return subject data set type
     */
    private DataSetType serialize(JsonObject obj, IFlowListVO subject) {
        this.addProperty(obj, "name", subject.getName());
        obj.addProperty("uuid", subject.getUuidAsString());
        obj.addProperty("version", subject.getDataSetVersion());
        obj.addProperty("type", subject.getType().getValue());

        if (subject instanceof Flow) {
            try {
                String classific = null;
                if (subject instanceof ElementaryFlow && ((ElementaryFlow) subject).getCategorization() != null) {
                    classific = ((ElementaryFlow) subject).getCategorization().getClassHierarchyAsString();
                } else {
                    if (subject.getClassification() != null)
                        classific = subject.getClassification().getClassHierarchyAsString();
                }
                obj.addProperty("classific", classific);
            } catch (Exception e) {
            }

            FlowPropertyDescription fp = ((Flow) subject).getReferencePropertyDescription();
            if (fp != null) {
                this.addProperty(obj, "refProp", fp.getName());
                obj.addProperty("refPropUnit", fp.getFlowPropertyUnit());
            }
        }
        return DataSetType.FLOW;
    }

    /**
     * Serialize a flow property list value object to JSON
     *
     * @param obj     object to add properties
     * @param subject source object
     * @return subject data set type
     */
    private DataSetType serialize(JsonObject obj, IFlowPropertyListVO subject) {
        this.addProperty(obj, "name", subject.getName());
        obj.addProperty("uuid", subject.getUuidAsString());
        obj.addProperty("version", subject.getDataSetVersion());
        try {
            obj.addProperty("classific", subject.getClassification().getClassHierarchyAsString());
        } catch (Exception e) {
        }
        IUnitGroupType ugDetails = subject.getUnitGroupDetails();
        if (ugDetails != null) {
            this.addProperty(obj, "defUnitGrp", ugDetails.getName());
            obj.addProperty("defUnit", ugDetails.getDefaultUnit());
        }
        return DataSetType.FLOW_PROPERTY;
    }

    /**
     * Serialize a LCIA method list value object to JSON
     *
     * @param obj     object to add properties
     * @param subject source object
     * @return subject data set type
     */
    private DataSetType serialize(JsonObject obj, ILCIAMethodListVO subject) {
        this.addProperty(obj, "name", subject.getName());
        obj.addProperty("uuid", subject.getUuidAsString());
        obj.addProperty("version", subject.getDataSetVersion());
        obj.addProperty("type", subject.getType().getValue());
        de.fzk.iai.ilcd.service.model.lciamethod.ITimeInformation ti = subject.getTimeInformation();
        if (ti != null) {
            try {
                obj.addProperty("refYear", ti.getReferenceYear().getValue());
            } catch (Exception e) {
            }
            try {
                obj.addProperty("dur", ti.getDuration().getValue());
            } catch (Exception e) {
            }
        }
        return DataSetType.LCIA_METHOD;
    }

    /**
     * Serialize an unit group list value object to JSON
     *
     * @param obj     object to add properties
     * @param subject source object
     * @return subject data set type
     */
    private DataSetType serialize(JsonObject obj, IUnitGroupListVO subject) {
        this.addProperty(obj, "name", subject.getName());
        obj.addProperty("uuid", subject.getUuidAsString());
        obj.addProperty("version", subject.getDataSetVersion());
        try {
            obj.addProperty("classific", subject.getClassification().getClassHierarchyAsString());
        } catch (Exception e) {
        }
        if (subject instanceof UnitGroup) {
            Unit refUnit = ((UnitGroup) subject).getReferenceUnit();
            if (refUnit != null) {
                try {
                    obj.addProperty("defUnit", refUnit.getName());
                } catch (Exception e) {
                }
            }
        }
        return DataSetType.UNIT_GROUP;
    }

    /**
     * Serialize a source list value object to JSON
     *
     * @param obj     object to add properties
     * @param subject source object
     * @return subject data set type
     */
    private DataSetType serialize(JsonObject obj, ISourceListVO subject) {
        this.addProperty(obj, "name", subject.getName());
        obj.addProperty("uuid", subject.getUuidAsString());
        obj.addProperty("version", subject.getDataSetVersion());
        if (subject.getClassification() != null)
            obj.addProperty("classific", subject.getClassification().getClassHierarchyAsString());
        if (subject instanceof ISourceVO) {
            PublicationTypeValue pType = ((ISourceVO) subject).getPublicationType();
            if (pType != null) {
                obj.addProperty("pubType", pType.getValue());
            }
        }
        return DataSetType.SOURCE;
    }

    /**
     * Serialize a contact list value object to JSON
     *
     * @param obj     object to add properties
     * @param subject source object
     * @return subject data set type
     */
    private DataSetType serialize(JsonObject obj, IContactListVO subject) {
        this.addProperty(obj, "name", subject.getName());
        if (subject.getShortName() != null)
            this.addProperty(obj, "shortName", subject.getShortName());
        obj.addProperty("uuid", subject.getUuidAsString());
        obj.addProperty("version", subject.getDataSetVersion());
        try {
            obj.addProperty("classific", subject.getClassification().getClassHierarchyAsString());
        } catch (Exception e) {
        }
        obj.addProperty("email", subject.getEmail());
        obj.addProperty("www", subject.getWww());
        return DataSetType.CONTACT;
    }


    /**
     * Serialize a process list value object to JSON
     *
     * @param obj     object to add properties
     * @param subject source object
     * @return subject data set type
     */
    private DataSetType serialize(JsonObject obj, ILifeCycleModelListVO subject) {

        JsonArray languages = new JsonArray();
        if (subject.getLanguages() != null) {
            for (String lang : subject.getLanguages())
                languages.add(lang);
            obj.add("languages", languages);
        }

        this.addProperty(obj, "name", subject.getName());
        obj.addProperty("uuid", subject.getUuidAsString());
        obj.addProperty("version", subject.getDataSetVersion());

        IClassification classification = subject.getClassification();
        if (classification != null) {
            obj.addProperty("classific", classification.getClassHierarchyAsString());
            obj.addProperty("classificId", SodaUtil.getHighestClassId(classification));
            obj.addProperty("classificSystem", classification.getName());
        }


        obj.add("compliance", this.getComplianceSystems(subject.getComplianceSystems()));


        safeAddListIGlobalReference(obj, subject, subject.getReferenceToOwnershipOfDataSet(), "owner");
        safeAddListIGlobalReference(obj, subject, subject.getReferenceToDiagram(), "diagram");
        safeAddListIGlobalReference(obj, subject, subject.getReferenceToExternalDocumentation(), "externaldocs");
        safeAddListIGlobalReference(obj, subject, subject.getReferenceToProcess(), "techProcesses");
        safeAddListIGlobalReference(obj, subject, subject.getReferenceToResultingProcess(), "resultingProcess");


        if (subject.getSourceId() != null)
            obj.addProperty("nodeid", subject.getSourceId());

        obj.addProperty("uri", subject.getHref());


        return DataSetType.LIFECYCLEMODEL;
    }

    private void safeAddListIGlobalReference(JsonObject obj, ILifeCycleModelListVO subject, List<IGlobalReference> ls,
                                             String label) {
        try {
            JsonArray arr = new JsonArray();
            if (ls != null)
                for (IGlobalReference ref : ls) {
                    JsonObject a = new JsonObject();
                    a.addProperty("uri", Optional.ofNullable(ref).map(IGlobalReference::getUri).orElse(null));
                    a.addProperty("refObjectId", Optional.ofNullable(ref).map(IGlobalReference::getRefObjectId).orElse(null));
                    a.addProperty("version", Optional.ofNullable(ref).map(IGlobalReference::getVersionAsString).orElse(null));
                    a.addProperty("type", Optional.ofNullable(ref).map(IGlobalReference::getType).map(GlobalReferenceTypeValue::getValue).orElse(null));
                    a.addProperty("shortDescription", Optional.ofNullable(ref).map(IGlobalReference::getShortDescription).map(IMultiLangString::getDefaultValue).orElse(null));
                    arr.add(a);
                }
            obj.add(label, arr);
        } catch (Exception e1) {
        }
    }


    /**
     * Add multilang string property
     *
     * @param obj   object to add to
     * @param name  name of property
     * @param mlStr multilang string instance
     */
    private void addProperty(JsonObject obj, String name, IMultiLangString mlStr) {
        if (mlStr != null) {
            // with lang fallback
            if (this.langFallback)
                obj.addProperty(name, mlStr.getValueWithFallback(this.lang));
            else if (this.lang == null)
                // default case, no lang
                obj.addProperty(name, mlStr.getValue());
            else
                // with lang
                obj.addProperty(name, mlStr.getValue(this.lang));
        }
    }

    public boolean isLangFallback() {
        return langFallback;
    }

    public void setLangFallback(boolean langFallback) {
        this.langFallback = langFallback;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
