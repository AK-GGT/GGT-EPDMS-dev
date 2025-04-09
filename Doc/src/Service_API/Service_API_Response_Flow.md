---

Flow Response Elements
----------------------

| Name             | Description                                              |
| :--------------: | :------------------------------------------------------- |
| *flow* (flow)          | \<description here\>                                     |
|            | Type: String                                             |
|                  | Ancestors: None                                          |
| *uuid*           | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flow                                          |
| *permanentUri*   | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flow                                          |
| *dataSetVersion* | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flow                                          |
| *name*           | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors: flow                                          |
| *generalComment* | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors: flow                                          |
| *synonyms*       | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | may occur multiple times                                 |
|                  | Ancestors: flow                                          |
| *flowCategorization* (flow) | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flow                                          |
| *category*       | \<description here\>                                     |
|                  | Type: String                                             |
|                  | may occur multiple times                                 |
|                  | Ancestors: flow.flowCategorization                       |
| *@level*         | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flow.flowCategorization.category              |
| *type* (flow)           | \<description here\>                                     |
|            | Type: String                                             |
|                  | Ancestors: flow                                          |
| *casNumber* (flow)      | \<description here\>                                     |
|            | Type: String                                             |
|                  | Ancestors: flow                                          |
| *sumFormula* (flow)    | \<description here\>                                     |
|            | Type: String                                             |
|                  | Ancestors: flow                                          |
| *referenceFlowProperty* (flow)| \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flow                                          |
| *@href* ()          | \<description here\>                                     |
|                | Type: String                                             |
|                  | Ancestors: flow.referenceFlowProperty                    |
| *name* (flow)           | \<description here\>                                     |
|            | Type: String Multilang                                   |
|                  | Ancestors: flow.referenceFlowProperty                    |
| *defaultUnit* (flow)    | \<description here\>                                     |
|            | Type: String                                             |
|                  | Ancestors: flow.referenceFlowProperty                    |
| *reference*      | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flow.referenceFlowProperty                    |
| *@type*          | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flow.referenceFlowProperty.reference          |
| *@refObjectId*   | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flow.referenceFlowProperty.reference          |
| *shortDescription* | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors: flow.referenceFlowProperty.reference          |
