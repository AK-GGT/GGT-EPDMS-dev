package de.iai.ilcd.model.common;

import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.dao.DataSetDaoType;

import java.util.*;
import java.util.stream.Stream;

public class DataSetReference {

    // For the log string
    private static final OptionalInt longestTypeLength = Stream.of(DataSetType.values())
            .mapToInt(t -> t.name().length())
            .max();
    private Long id;
    private String uuid;
    private DataSetType type;
    private String version;
    private String displayName;
    private DataSetDaoType daoType;
    private Class<? extends DataSet> dataSetClass;

    public DataSetReference() {
        super();
    }

    public DataSetReference(DataSet dataSet) {
        this.id = dataSet.getId();
        this.uuid = dataSet.getUuidAsString();
        this.type = dataSet.getDataSetType();
        this.version = dataSet.getDataSetVersion();
        this.displayName = dataSet.getName().getDefaultValue();
        this.daoType = DataSetDaoType.getFor(dataSet);
        this.dataSetClass = dataSet.getClass();
    }

    public static String toLogString(DataSet dataSet) {
        return toNiceString(dataSet.getDataSetType(), dataSet.getUuidAsString(), dataSet.getDataSetVersion(), dataSet.getDefaultName());
    }

    private static String toNiceString(DataSetType type, String uuid, String version, String displayName) {
        int longestTypeLengthInt = longestTypeLength.isPresent() ? longestTypeLength.getAsInt() : 0;
        int numberOfFillers = longestTypeLengthInt - type.name().length();
        StringBuilder fillers = new StringBuilder();
        for (int i = 0; i < numberOfFillers; i++)
            fillers.append("#");

        return "### " +
                type.name() +
                " " + fillers + "#### (" +
                uuid + ", " + version +
                ") ###### " + displayName;
    }

    public static Comparator<DataSetReference> getByDaoTypeComparator() {
        Comparator<DataSetDaoType> comparator = DataSetDaoType.getComparatorForProcessingOrder();

        return (r, s) -> {

            if (r == null && s == null)
                return 0;
            else if (r == null)
                return -1;
            else if (s == null)
                return 1;
            else
                return comparator.compare(r.getDaoType(), s.getDaoType());
        };
    }

    public static TreeSet<DataSetReference> sort(Collection<DataSetReference> col) {
        TreeSet<DataSetReference> treeSet = new TreeSet<>(DataSetReference.getByDaoTypeComparator());
        treeSet.addAll(col);
        return treeSet;
    }

    public Class<? extends DataSet> getDataSetClass() {
        return dataSetClass;
    }

    public void setDataSetClass(Class<? extends DataSet> dataSetClass) {
        this.dataSetClass = dataSetClass;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuidAsString() {
        return uuid;
    }

    public void setUuid(Uuid uuid) {
        this.uuid = uuid.getUuid();
    }

    public DataSetType getType() {
        return type;
    }

    public void setType(DataSetType type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(DataSetVersion version) {
        this.version = version.getVersionString();
    }

    public DataSetVersion getDataSetVersion() {
        try {
            return DataSetVersion.parse(version);
        } catch (FormatException fe) {
            fe.printStackTrace();
            return null; // Should never occur though
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public DataSetDaoType getDaoType() {
        return daoType;
    }

    public void setDaoType(DataSetDaoType daoType) {
        this.daoType = daoType;
    }

    @Override
    public boolean equals(Object object) {
        // object is null or no DataSet
        if (!(object instanceof DataSetReference)) {
            return false;
        }

        // check if same "subclass"
        if (!this.getClass().equals(object.getClass())) {
            return false;
        }

        DataSetReference other = (DataSetReference) object;

        // compare ID, if set
        if ((this.id == null && other.getId() != null) || (this.id != null && !this.id.equals(other.getId()))) {
            return false;
        }

        // compare UUID, if set
        if ((this.uuid == null && other.getUuidAsString() != null) || (this.uuid != null && !this.uuid.equals(other.getUuidAsString()))) {
            return false;
        }

        // compare version, if set
        if ((this.version == null && other.getVersion() != null) || (this.version != null && !this.version.equals(other.getVersion()))) {
            return false;
        }

        // compare type, if set
        if ((this.type == null && other.getType() != null) || (this.type != null && !this.type.equals(other.getType()))) {
            return false;
        }

        // same class, same ID, same UUID, same version, same type [same means also: both null]
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid, type, version, displayName);
    }

    @Override
    public String toString() {
        return "DataSetReference{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", type=" + type +
                ", version=" + version +
                ", displayName='" + displayName + '\'' +
                ", daoType=" + daoType +
                '}';
    }

    public String toLogString() {
        return toNiceString(type, uuid, version, displayName);
    }

    public DataSet getReferencedDataSet() {
        if (id != null)
            return daoType.getDao().getByDataSetId(id);
        else if (uuid != null)
            return daoType.getDao().getByUuidAndVersion(uuid, version);
        else
            return null;
    }
}
