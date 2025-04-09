---

Process Response Elements
-------------------------

| Name             | Description                                              |
| :--------------: | :------------------------------------------------------: |
| *process* (process) | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: None                                          |
| *@accessRestricted* | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process                                       |
| *uuid*           | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process                                       |
| *permanentUri*   | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process                                       |
| *dataSetVersion* | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process                                       |
| *name*           | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors: process                                       |
| *classification* | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process                                       |
| *class*          | \<description here\>                                     |
|                  | Type: String                                             |
|                  | may occur multiple times                                 |
|                  | Ancestors: process.classification                        |
| *@level*         | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process.classification.class                  |
| *generalComment* | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors: process                                       |
| *synonyms*       | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | may occur multiple times                                 |
|                  | Ancestors: process                                       |
| *type* (process)          | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process                                       |
| *quantitativeReference* (process) | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process                                       |
| *referenceFlow* (process)  | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process.quantitativeReference                 |
| *name* (process)          | \<description here\>                                     |
|         | Type: String Multilang                                   |
|                  | Ancestors: process.quantitativeReference.referenceFlow   |
| *meanValue* (process)      | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process.quantitativeReference.referenceFlow   |
| *reference*      | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process.quantitativeReference.referenceFlow   |
| *@type*          | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors:                                               |
|                  | process.quantitativeReference.referenceFlow.reference    |
| *@refObjectId*   | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors:                                               |
|                  | process.quantitativeReference.referenceFlow.reference    |
| *shortDescription* | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors:                                               |
|                  | process.quantitativeReference.referenceFlow.reference    |
| *functionalUnit* (process) | \<description here\>                                     |
|         | Type: String Multilang                                   |
|                  | Ancestors: process.quantitativeReference                 |
| *location* (process)       | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process                                       |
| *time* (process)          | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process                                       |
| *referenceYear* (process)  | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process.time                                  |
| *validUntil* (process)    | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process.time                                  |
| *parameterized* (process) | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process                                       |
| *hasResults* (process)     | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process                                       |
| *containsProductModel* (process)| \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process                                       |
| *lciMethodInformation* (process) | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process                                       |
| *methodPrinciple* (process) | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process.lciMethodInformation                  |
| *approach* (process)      | \<description here\>                                     |
|         | Type: String                                             |
|                  | may occur multiple times                                 |        |
|                  | Ancestors: process.lciMethodInformation                  |
| *completenessProductModel* (process) | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process                                       |
| *complianceSystem* (process) | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process                                       |
| *@name*          | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process.complianceSystem                      |
| *overallCompliance* (process) | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process.complianceSystem                      |
| *review* (process)        | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process                                       |
| *@type*          | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process.review                                |
| *scope* (process)         | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process.review                                |
| *@name*          | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process.review.scope                          |
| *method* (process) | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process.review.scope                          |
| *@name*          | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process.review.scope.method                   |
| *dataQualityIndicators* (process) | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process.review                                |
| *dataQualityIndicator* (process)| \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process.review.dataQualityIndicators          |
| *@name*          | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors:                                               |
|                  | process.review.dataQualityIndicators.dataQualityIndicator |
| *@value*         | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors:                                               |
|                  | process.review.dataQualityIndicators.dataQualityIndicator |
| *reviewDetails* (process)  | \<description here\>                                     |
|         | Type: String Multilang                                   |
|                  | Ancestors: process.review                                |
| *overallQuality* (process)| \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process                                       |
| *useAdvice* (process)      | \<description here\>                                     |
|         | Type: String Multilang                                   |
|                  | Ancestors: process                                       |
| *accessInformation* (process) | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process                                       |
| *copyright* (process)     | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process.accessInformation                     |
| *licenseType* (process)    | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process.accessInformation                     |
| *useRestrictions* (process) | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors: process.accessInformation                     |
| *format* (process)        | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process                                       |
| *ownership* (process)      | \<description here\>                                     |
|         | Type: String                                             |
|                  | Ancestors: process                                       |
| *@type*          | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process.ownership                             |
| *@refObjectId*   | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: process.ownership                             |
| *shortDescription* | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors: process.ownership                             |
