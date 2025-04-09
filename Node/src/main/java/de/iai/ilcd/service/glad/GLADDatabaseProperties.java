package de.iai.ilcd.service.glad;

import de.iai.ilcd.service.glad.model.*;

import javax.persistence.*;

/**
 * The properties object representing the configurable GLAD properties.
 * These properties are persisted in the database.
 *
 * @author sarai
 */
@Entity
@Table(name = "glad_database_properties")
public class GLADDatabaseProperties {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RepresentativenessType representativenessType;

    @Enumerated(EnumType.STRING)
    private ReviewSystem reviewSystem;

    @Enumerated(EnumType.STRING)
    private BiogenicCarbonModeling biogenicCarbonModeling;

    @Enumerated(EnumType.STRING)
    private EndOfLifeModeling endOfLifeModeling;

    @Enumerated(EnumType.STRING)
    private WaterModeling waterModeling;

    @Enumerated(EnumType.STRING)
    private InfrastructureModeling infrastructureModeling;

    @Enumerated(EnumType.STRING)
    private EmissionModeling emissionModeling;

    @Enumerated(EnumType.STRING)
    private CarbonStorageModeling carbonStorageModeling;


    public Long getId() {
        return this.id;
    }

    //Getter and setter methods for parameters defined above

    public RepresentativenessType getRepresentativenessType() {
        return this.representativenessType;
    }

    public void setRepresentativenessType(RepresentativenessType representativenessType) {
        this.representativenessType = representativenessType;
    }

    public ReviewSystem getReviewSystem() {
        return this.reviewSystem;
    }

    public void setReviewSystem(ReviewSystem reviewSystem) {
        this.reviewSystem = reviewSystem;
    }

    public BiogenicCarbonModeling getBiogenicCarbonModeling() {
        return this.biogenicCarbonModeling;
    }


    public void setBiogenicCarbonModeling(BiogenicCarbonModeling biogenicCarbonModeling) {
        this.biogenicCarbonModeling = biogenicCarbonModeling;
    }

    public EndOfLifeModeling getEndOfLifeModeling() {

        return this.endOfLifeModeling;
    }


    public void setEndOfLifeModeling(EndOfLifeModeling endOfLifeModeling) {
        this.endOfLifeModeling = endOfLifeModeling;
    }

    public WaterModeling getWaterModeling() {
        return this.waterModeling;
    }

    public void setWaterModeling(WaterModeling waterModeling) {
        this.waterModeling = waterModeling;
    }

    public InfrastructureModeling getInfrastructureModeling() {
        return this.infrastructureModeling;
    }


    public void setInfrastructureModeling(InfrastructureModeling infrastructureModeling) {
        this.infrastructureModeling = infrastructureModeling;
    }

    public EmissionModeling getEmissionModeling() {
        return this.emissionModeling;
    }


    public void setEmissionModeling(EmissionModeling emissionModeling) {
        this.emissionModeling = emissionModeling;
    }

    public CarbonStorageModeling getCarbonStorageModeling() {
        return this.carbonStorageModeling;
    }


    public void setCarbonStorageModeling(CarbonStorageModeling carbonStorageModeling) {
        this.carbonStorageModeling = carbonStorageModeling;
    }

}
