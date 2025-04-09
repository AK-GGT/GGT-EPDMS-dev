package de.iai.ilcd.model.common;

import de.iai.ilcd.model.common.exception.FormatException;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author clemens.duepmeier
 */
@Embeddable
public class DataSetVersion implements Serializable, Comparable<DataSetVersion> {

    private static final long serialVersionUID = -4749909463906972676L;

    private int version = 0;

    private int majorVersion = 0;

    private int minorVersion = 0;

    private int subMinorVersion = 0;

    public DataSetVersion() {

    }

    public DataSetVersion(int major, int minor, int subMinor) throws FormatException {
        if (major < 0 || minor < 0 || subMinor < 0) {
            throw new FormatException("Every version number must be greater or equal to 0");
        }
        this.majorVersion = major;
        this.minorVersion = minor;
        this.subMinorVersion = subMinor;
        this.version = toInt(major, minor, subMinor);
    }

    public DataSetVersion(int major, int minor) throws FormatException {
        this(major, minor, 0);
    }

    public DataSetVersion(int major) throws FormatException {
        this(major, 0, 0);
    }

    public static DataSetVersion parse(String versionString) throws FormatException {

        if (versionString == null)
            return null;

        DataSetVersion version = null;

        // versionString should contain three unsigned int number separated by dots
        String[] numbers = versionString.split("\\.");
        if (numbers.length < 1 || numbers.length > 3) {
            throw new FormatException("The version number should be a string with the form xx.yy.zzz where xx, yy, zzz are numbers");
        }
        try {
            int major = Integer.parseInt(numbers[0]);
            int minor = 0;
            if (numbers.length > 1) {
                minor = Integer.parseInt(numbers[1]);
            }
            int subMinor = 0;
            if (numbers.length > 2) {
                subMinor = Integer.parseInt(numbers[2]);
            }

            if (major < 0 || minor < 0 || subMinor < 0) {
                throw new FormatException("All parts of the version string xx.yy.zzz must have values greater or equal 0");
            }
            version = new DataSetVersion(major, minor, subMinor);
        } catch (NumberFormatException ex) {
            throw new FormatException("One of the parts contained in the version number string xx.yy.zzz could no be parsed to an Integer");
        }

        return version;
    }

    /**
     * Determine if version is blank (version is <code>null</code> or {@link #isZero() zero})
     *
     * @param version version to check
     * @return <code>true</code>, if version is blank, <code>false</code> otherwise
     */
    public static boolean isNotBlank(DataSetVersion version) {
        return version != null && !version.isZero();
    }

    public int getVersion() {
        return this.version;
    }

    public int getMajorVersion() {
        return this.majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
        this.version = toInt(this.majorVersion, this.minorVersion, this.subMinorVersion);
    }

    public int getMinorVersion() {
        return this.minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
        this.version = toInt(this.majorVersion, this.minorVersion, this.subMinorVersion);
    }

    public int getSubMinorVersion() {
        return this.subMinorVersion;
    }

    public void setSubMinorVersion(int subMinorVersion) {
        this.subMinorVersion = subMinorVersion;
        this.version = toInt(this.majorVersion, this.minorVersion, this.subMinorVersion);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final DataSetVersion other = (DataSetVersion) obj;
        if (this.majorVersion != other.majorVersion) {
            return false;
        }
        if (this.minorVersion != other.minorVersion) {
            return false;
        }
        if (this.subMinorVersion != other.subMinorVersion) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.majorVersion;
        hash = 97 * hash + this.minorVersion;
        return hash;
    }

    @Override
    public int compareTo(DataSetVersion other) {

        if (this.majorVersion > other.majorVersion) {
            return 1;
        } else if (this.majorVersion < other.majorVersion) {
            return -1;
        }
        if (this.minorVersion > other.minorVersion) {
            return 1;
        } else if (this.minorVersion < other.minorVersion) {
            return -1;
        }
        if (this.subMinorVersion > other.subMinorVersion) {
            return 1;
        } else if (this.subMinorVersion < other.subMinorVersion) {
            return -1;
        }
        // now they must be equal
        return 0;
    }

    public String getVersionString() {
        return this.toString();
    }

    @Override
    public String toString() {
        return String.format("%02d.%02d.%03d", this.majorVersion, this.minorVersion, this.subMinorVersion);
    }

    public int toInt(int major, int minor, int subMinor) {
        return major * 100000 + minor * 1000 + subMinor;
    }

    /**
     * Determine if all version numbers are 0
     *
     * @return <code>true</code> if all version numbers are 0
     */
    public boolean isZero() {
        return this.majorVersion == 0 && this.minorVersion == 0 && this.subMinorVersion == 0;
    }

}
