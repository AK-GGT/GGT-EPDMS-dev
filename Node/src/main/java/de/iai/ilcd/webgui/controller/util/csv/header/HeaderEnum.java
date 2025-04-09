package de.iai.ilcd.webgui.controller.util.csv.header;

public enum HeaderEnum {
    UUID("UUID", "UUID"),
    Version("Version", "Version"),
    Name("Name", "Name"),
    Category("Category", "Kategorie"),
    Compliance("Compliance", "Konformität"),
    //<new>
    LocationCode("Location code", "Laenderkennung"),
    // </new>
    Type("Type", "Typ"),
    // <new>
    ReferenceYear("Reference year", "Referenzjahr"),
    ValidUntil("Valid until", "Gueltig bis"),
    URL("URL", "URL"),
    DeclarationOwner("Declaration owner", "Declaration owner"),
    PublicationDate("Publication date", "Veroeffentlicht am"),//1.2
    RegistrationNumber("Registration number", "Registrierungsnummer"),//1.2
    RegistrationAuthority("Registration authority", "Registrierungsstelle"),//1.2
    PredecessorUUID("Predecessor UUID", "UUID des Vorgängers"),//1.2
    PredecessorVersion("Predecessor Version", "Version des Vorgängers"),//1.2
    PredecessorURL("Predecessor URL", "URL des Vorgängers"), //1.2
    // </new>
    Refquantity("Ref. quantity", "Bezugsgroesse"),
    Refunit("Ref. unit", "Bezugseinheit"),
    ReferenceflowUUID("Reference flow UUID", "Referenzfluss-UUID"),
    Referenceflowname("Reference flow name", "Referenzfluss-Name"),
    Bulkdensity_kgm3("Bulk Density (kg/m3)", "Schuettdichte (kg/m3)"),
    Grammage_kgm2("Grammage (kg/m2)", "Flaechengewicht (kg/m2)"),
    Grossdensity_kgm3("Gross Density (kg/m3)", "Rohdichte (kg/m3)"),
    Layerthickness_m("Layer Thickness (m)", "Schichtdicke (m)"),
    Productiveness_m2("Productiveness (m2)", "Ergiebigkeit (m2)"),
    Lineardensity_kgm("Linear Density (kg/m)", "Laengengewicht (kg/m)"),
    Stueckgewicht_kg("Weight Per Piece (kg)", "Stueckgewicht (kg)"),
    Conversionfactorto1kg("Conversion factor to 1kg", "Umrechungsfaktor auf 1kg"),
    CarbonContentBiogenic("Carbon content (biogenic) in kg", "biogener Kohlenstoffgehalt in kg"),
    CarbonContentBiogenicPackaging("Carbon content (biogenic) - packaging in kg", "biogener Kohlenstoffgehalt (Verpackung) in kg"),
    Module("Module", "Modul"),
    Scenario("Scenario", "Szenario"),
    ScenarioDescription("Scenario Description", "Szenariobeschreibung");


    public final String title_en, title_de;

    HeaderEnum(String en, String de) {
        this.title_en = en;
        this.title_de = de;
    }
}
