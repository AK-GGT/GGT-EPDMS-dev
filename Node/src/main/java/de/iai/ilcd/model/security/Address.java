package de.iai.ilcd.model.security;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author clemens.duepmeier
 */
@Embeddable
public class Address implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5891661401495400754L;

    private String country;

    private String city;

    private String zipCode;

    private String streetAddress;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
