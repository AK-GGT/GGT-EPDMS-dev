---

FlowProperty Response Elements
------------------------------

| Name             | Description                                              |
| :--------------: | :------------------------------------------------------- |
| *flowProperty* (flow)   | \<description here\>                                     |
|            | Type: String                                             |
|                  | Ancestors: None                                          |
| *uuid*           | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flowProperty                                  |
| *permanentUri*   | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flowProperty                                  |
| *dataSetVersion* | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flowProperty                                  |
| *name*           | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors: flowProperty                                  |
| *generalComment* | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors: flowProperty                                  |
| *synonyms*       | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | may occur multiple times                                 |
|                  | Ancestors: flowProperty                                  |
| *unitGroup* (flow)| \<description here\>                                    |
|                  | Type: String                                             |
|                  | Ancestors: flowProperty                                  |
| *name* (flow)    | \<description here\>                                     |
|            | Type: String Multilang                                   |
|                  | Ancestors: flowProperty.unitGroup                        |
| *defaultUnit* (flow)    | \<description here\>                                     |
|            | Type: String                                             |
|                  | Ancestors: flowProperty.unitGroup                        |
| *reference*      | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flowProperty.unitGroup                        |
| *@type*          | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flowProperty.unitGroup.reference              |
| *@refObjectId*   | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: flowProperty.unitGroup.reference              |
| *shortDescription* | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors: flowProperty.unitGroup.reference              |
