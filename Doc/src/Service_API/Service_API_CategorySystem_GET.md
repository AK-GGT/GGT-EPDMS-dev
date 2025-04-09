---

GET Category System Definition
==============================

GET operation that returns a stored category system definition for a specific category system.

Data can either be returned in XML (default) or XLS formats, which can be controlled by using a request parameter (see below).

Requests
--------

### Syntax

    GET /categorySystems/{categorySystem-name}

### Request Parameters

| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *format*         | *XML*,XLS  | Returns the result in the specified format. |
| *countStock*     | String     | When the id of a data stock is given here, for each category the total counts of datasets in this datastock will be added to the output (XLS only). |
| *lang*           | String     | Returns the category names in the requested language if proper translation files are configured and available. |
| *allLanguages*   | Boolean    | This will write the category names into a single XLS sheet for all languages for which translations are present (XLS only). Does not work in combination with *countStock*. |


Responses
---------

### Response Elements

A CategorySystem definitions in accordance with the ILCD format (see example below).

Examples
--------

### Sample Request

    GET /categorySystems/ILCD

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8"?>
<c:CategorySystem xmlns:c="http://lca.jrc.it/ILCD/Categories" name="ILCD">
    <c:categories dataType="Process">
        <c:category id="1" name="Energy carriers and technologies">
            <c:category id="1.1" name="Energetic raw materials"/>
            <c:category id="1.2" name="Electricity"/>
            <c:category id="1.3" name="Heat and steam"/>
            <c:category id="1.4" name="Mechanical energy"/>
            <c:category id="1.5" name="Hard coal based fuels"/>
            <c:category id="1.6" name="Lignite based fuels"/>
            <c:category id="1.7" name="Crude oil based fuels"/>
            <c:category id="1.8" name="Natural gas based fuels"/>
            <c:category id="1.9" name="Nuclear fuels"/>
            <c:category id="1.10" name="Other non-renewable fuels"/>
            <c:category id="1.11" name="Renewable fuels"/>
        </c:category>
        <c:category id="2" name="Materials production">
            <c:category id="2.1" name="Non-energetic raw materials"/>
            <c:category id="2.2" name="Metals and semimetals"/>
            <c:category id="2.3" name="Organic chemicals"/>
            <c:category id="2.4" name="Inorganic chemicals"/>
            <c:category id="2.5" name="Glass and ceramics"/>
            <c:category id="2.6" name="Other mineralic materials"/>
            <c:category id="2.7" name="Plastics"/>
            <c:category id="2.8" name="Paper and cardboards"/>
            <c:category id="2.9" name="Water"/>
            <c:category id="2.10" name="Agricultural production means"/>
            <c:category id="2.11" name="Food and renewable raw materials"/>
            <c:category id="2.12" name="Wood"/>
            <c:category id="2.13" name="Other materials"/>
        </c:category>
        <c:category id="3" name="Systems">
            <c:category id="3.1" name="Packaging"/>
            <c:category id="3.2" name="Electrics and electronics"/>
            <c:category id="3.3" name="Vehicles"/>
            <c:category id="3.4" name="Other machines"/>
            <c:category id="3.5" name="Construction"/>
            <c:category id="3.6" name="White goods"/>
            <c:category id="3.7" name="Textiles, furnitures and other interiors"/>
            <c:category id="3.8" name="Unspecific parts"/>
            <c:category id="3.9" name="Paints and chemical preparations"/>
            <c:category id="3.10" name="Other systems"/>
        </c:category>
        <c:category id="4" name="End-of-life treatment">
            <c:category id="4.1" name="Reuse or further use"/>
            <c:category id="4.2" name="Material recycling"/>
            <c:category id="4.3" name="Raw material recycling"/>
            <c:category id="4.4" name="Energy recycling"/>
            <c:category id="4.5" name="Landfilling"/>
            <c:category id="4.6" name="Waste collection"/>
            <c:category id="4.7" name="Waste water treatment"/>
            <c:category id="4.8" name="Raw gas treatment"/>
            <c:category id="4.9" name="Other end-of-life services"/>
        </c:category>
        <c:category id="5" name="Transport services">
            <c:category id="5.1" name="Road"/>
            <c:category id="5.2" name="Rail"/>
            <c:category id="5.3" name="Water"/>
            <c:category id="5.4" name="Air"/>
            <c:category id="5.5" name="Other transport"/>
        </c:category>
        <c:category id="6" name="Other Services">
            <c:category id="6.1" name="Cleaning"/>
            <c:category id="6.2" name="Storage"/>
            <c:category id="6.3" name="Health, social services, beauty and wellness"/>
            <c:category id="6.4" name="Repair and maintenance"/>
            <c:category id="6.5" name="Sale and whole sale"/>
            <c:category id="6.6" name="Communication and information services"/>
            <c:category id="6.7" name="Financial, legal, and insurance"/>
            <c:category id="6.8" name="Administration and government"/>
            <c:category id="6.9" name="Defence"/>
            <c:category id="6.10" name="Lodging and gastronomy"/>
            <c:category id="6.11" name="Education"/>
            <c:category id="6.12" name="Research and development"/>
            <c:category id="6.13" name="Entertainment"/>
            <c:category id="6.14" name="Renting"/>
            <c:category id="6.15" name="Engineering and consulting"/>
            <c:category id="6.16" name="Other services"/>
        </c:category>
        <c:category id="7" name="Use and consumption">
            <c:category id="7.1" name="Consumption of goods"/>
            <c:category id="7.2" name="Use of energy-using products"/>
            <c:category id="7.3" name="Other use and consumption"/>
        </c:category>
    </c:categories>
    <c:categories dataType="LCIAMethod">
        <c:category id="1" name="Endpoint/damage level LCIA methods">
            <c:category id="1.1" name="Natural resources">
                <c:category id="1.1.1" name="Land use"/>
                <c:category id="1.1.2" name="Abiotic resource depletion"/>
                <c:category id="1.1.3" name="Biotic resource depletion"/>
                <c:category id="1.1.4" name="Combined"/>
                <c:category id="1.1.5" name="other"/>
            </c:category>
            <c:category id="1.2" name="Natural environment">
                <c:category id="1.2.1" name="Climate change"/>
                <c:category id="1.2.2" name="Ozone depletion"/>
                <c:category id="1.2.3" name="Terrestrial Eutrophication"/>
                <c:category id="1.2.4" name="Aquatic Eutrophication"/>
                <c:category id="1.2.5" name="Acidification"/>
                <c:category id="1.2.6" name="Photochemical ozone creation"/>
                <c:category id="1.2.7" name="Land use"/>
                <c:category id="1.2.8" name="Ionizing radiation"/>
                <c:category id="1.2.9" name="Aquatic eco-toxicity"/>
                <c:category id="1.2.10" name="Terrestrial eco-toxicity"/>
                <c:category id="1.2.11" name="Combined"/>
                <c:category id="1.2.12" name="other"/>
            </c:category>
            <c:category id="1.3" name="Human health">
                <c:category id="1.3.1" name="Climate change"/>
                <c:category id="1.3.2" name="Ozone depletion"/>
                <c:category id="1.3.3" name="Photochemical ozone creation"/>
                <c:category id="1.3.4" name="Ionizing radiation"/>
                <c:category id="1.3.5" name="Cancer human health effects"/>
                <c:category id="1.3.6" name="Non-cancer human health effects"/>
                <c:category id="1.3.7" name="Respiratory inorganics"/>
                <c:category id="1.3.8" name="Combined"/>
                <c:category id="1.3.9" name="other"/>
            </c:category>
            <c:category id="1.4" name="Man-made environment"/>
            <c:category id="1.4" name="Combined"/>
            <c:category id="1.4" name="other"/>
        </c:category>
        <c:category id="2" name="Midpoint level LCIA methods">
            <c:category id="2.1" name="Climate change"/>
            <c:category id="2.2" name="Ozone depletion"/>
            <c:category id="2.3" name="Terrestrial Eutrophication"/>
            <c:category id="2.4" name="Aquatic Eutrophication"/>
            <c:category id="2.5" name="Acidification"/>
            <c:category id="2.6" name="Photochemical ozone creation"/>
            <c:category id="2.7" name="Land use"/>
            <c:category id="2.8" name="Abiotic resource depletion"/>
            <c:category id="2.9" name="Biotic resource depletion"/>
            <c:category id="2.10" name="Ionizing radiation"/>
            <c:category id="2.11" name="Cancer human health effects"/>
            <c:category id="2.12" name="Non-cancer human health effects"/>
            <c:category id="2.13" name="Respiratory inorganics"/>
            <c:category id="2.14" name="Aquatic eco-toxicity"/>
            <c:category id="2.15" name="Terrestrial eco-toxicity"/>
            <c:category id="2.16" name="Combined"/>
            <c:category id="2.17" name="other"/>
        </c:category>
    </c:categories>
    <c:categories dataType="Flow">
        <c:category id="1" name="Emissions">
            <c:category id="1.1" name="Radioactives"/>
            <c:category id="1.2" name="Pesticides"/>
            <c:category id="1.3" name="Particles"/>
            <c:category id="1.4" name="Metal and semimetal elements and ions"/>
            <c:category id="1.5" name="Non-metallic or -semimetallic ions"/>
            <c:category id="1.6" name="Inorganic covalent compounds"/>
            <c:category id="1.7" name="Cyclic organics"/>
            <c:category id="1.8" name="Acylic organics"/>
            <c:category id="1.9" name="Other substance type"/>
        </c:category>
        <c:category id="5" name="Energy carriers and technologies">
            <c:category id="5.1" name="Energetic raw materials"/>
            <c:category id="5.2" name="Electricity"/>
            <c:category id="5.3" name="Heat and steam"/>
            <c:category id="5.4" name="Mechanical energy"/>
            <c:category id="5.5" name="Hard coal based fuels"/>
            <c:category id="5.6" name="Lignite based fuels"/>
            <c:category id="5.7" name="Crude oil based fuels"/>
            <c:category id="5.8" name="Natural gas based fuels"/>
            <c:category id="5.9" name="Nuclear fuels"/>
            <c:category id="5.10" name="Other non-renewable fuels"/>
            <c:category id="5.11" name="Renewable fuels"/>
        </c:category>
        <c:category id="6" name="Materials production">
            <c:category id="6.1" name="Raw materials"/>
            <c:category id="6.2" name="Metals and semimetals"/>
            <c:category id="6.3" name="Organic chemicals"/>
            <c:category id="6.4" name="Inorganic chemicals"/>
            <c:category id="6.5" name="Glass and ceramics"/>
            <c:category id="6.6" name="Other mineralic materials"/>
            <c:category id="6.7" name="Plastics"/>
            <c:category id="6.8" name="Paper and cardboards"/>
            <c:category id="6.9" name="Water"/>
            <c:category id="6.10" name="Agricultural production means"/>
            <c:category id="6.11" name="Food and renewable raw materials"/>
            <c:category id="6.12" name="Wood"/>
            <c:category id="6.13" name="Other materials"/>
        </c:category>
        <c:category id="7" name="Systems">
            <c:category id="7.1" name="Packaging"/>
            <c:category id="7.2" name="Electrics and electronics"/>
            <c:category id="7.3" name="Vehicles"/>
            <c:category id="7.4" name="Other machines"/>
            <c:category id="7.5" name="Construction"/>
            <c:category id="7.6" name="White goods"/>
            <c:category id="7.7" name="Textiles, furnitures and other interiors"/>
            <c:category id="7.8" name="Unspecific parts"/>
            <c:category id="7.9" name="Paints and chemical preparations"/>
            <c:category id="7.10" name="Other systems"/>
        </c:category>
        <c:category id="8" name="End-of-life treatment">
            <c:category id="8.1" name="Reuse or further use"/>
            <c:category id="8.2" name="Material recycling"/>
            <c:category id="8.3" name="Raw material recycling"/>
            <c:category id="8.4" name="Energy recycling"/>
            <c:category id="8.5" name="Landfilling"/>
            <c:category id="8.6" name="Waste collection"/>
            <c:category id="8.7" name="Waste water treatment"/>
            <c:category id="8.8" name="Raw gas treatment"/>
            <c:category id="8.9" name="Other end-of-life services"/>
        </c:category>
        <c:category id="9" name="Transport services">
            <c:category id="9.1" name="Road"/>
            <c:category id="9.2" name="Rail"/>
            <c:category id="9.3" name="Water"/>
            <c:category id="9.4" name="Air"/>
            <c:category id="9.5" name="Other transport"/>
        </c:category>
        <c:category id="10" name="Other Services">
            <c:category id="10.1" name="Cleaning"/>
            <c:category id="10.2" name="Storage"/>
            <c:category id="10.3" name="Health, social services, beauty and wellness"/>
            <c:category id="10.4" name="Repair and maintenance"/>
            <c:category id="10.5" name="Sale and whole sale"/>
            <c:category id="10.6" name="Communication and information services"/>
            <c:category id="10.7" name="Financial, legal, and insurance"/>
            <c:category id="10.8" name="Administration and government"/>
            <c:category id="10.9" name="Defence"/>
            <c:category id="10.10" name="Lodging and gastronomy"/>
            <c:category id="10.11" name="Education"/>
            <c:category id="10.12" name="Research and development"/>
            <c:category id="10.13" name="Entertainment"/>
            <c:category id="10.14" name="Renting"/>
            <c:category id="10.15" name="Engineering and consulting"/>
            <c:category id="10.16" name="Other services"/>
        </c:category>
        <c:category id="11" name="Use and consumption">
            <c:category id="11.1" name="Consumption of goods"/>
            <c:category id="11.2" name="Use of energy-using products"/>
            <c:category id="11.3" name="Other use and consumption"/>
        </c:category>
        <c:category id="12" name="Wastes">
            <c:category id="12.1" name="Mining waste"/>
            <c:category id="12.2" name="Construction waste"/>
            <c:category id="12.3" name="Production residues"/>
            <c:category id="12.4" name="Post consumer waste"/>
            <c:category id="12.5" name="Radioactive waste"/>
            <c:category id="12.6" name="Raw gas"/>
            <c:category id="12.7" name="Waste water"/>
            <c:category id="12.8" name="Other waste"/>
        </c:category>
    </c:categories>
    <c:categories dataType="FlowProperty">
        <c:category id="1" name="Technical flow properties"/>
        <c:category id="2" name="Chemical composition of flows"/>
        <c:category id="3" name="Economic flow properties"/>
        <c:category id="4" name="Other flow properties"/>
    </c:categories>
    <c:categories dataType="UnitGroup">
        <c:category id="1" name="Technical unit groups"/>
        <c:category id="2" name="Chemical composition unit groups"/>
        <c:category id="3" name="Economic unit groups"/>
        <c:category id="4" name="Other unit groups"/>
    </c:categories>
    <c:categories dataType="Contact">
        <c:category id="1" name="Group of organisations, project"/>
        <c:category id="2" name="Organisations">
            <c:category id="2.1" name="Private companies"/>
            <c:category id="2.2" name="Governmental organisations"/>
            <c:category id="2.3" name="Non-governmental organisations"/>
            <c:category id="2.4" name="Other organisations"/>
        </c:category>
        <c:category id="3" name="Working groups within organisation"/>
        <c:category id="4" name="Persons"/>
        <c:category id="5" name="Other"/>
    </c:categories>
    <c:categories dataType="Source">
        <c:category id="0" name="Images"/>
        <c:category id="1" name="Data set formats"/>
        <c:category id="2" name="Databases"/>
        <c:category id="3" name="Compliance systems"/>
        <c:category id="4" name="Statistical classifications"/>
        <c:category id="5" name="Publications and communications"/>
        <c:category id="6" name="Other source types"/>
    </c:categories>
</c:CategorySystem>
~~~~


