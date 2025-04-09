---

LCIAMethod Response Elements
----------------------------

| Name             | Description                                              |
| :--------------: | :------------------------------------------------------: |
| *LCIAMethod* (lciamethod)     | \<description here\>                                     |
|      			   | Type: String                                             |
|                  | Ancestors: None                                          |
| *uuid*           | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: LCIAMethod                                    |
| *permanentUri*   | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: LCIAMethod                                    |
| *dataSetVersion* | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: LCIAMethod                                    |
| *name*           | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors: LCIAMethod                                    |
| *classification* | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: LCIAMethod                                    |
| *class*          | \<description here\>                                     |
|                  | Type: String                                             |
|                  | may occur multiple times                                 |
|                  | Ancestors: LCIAMethod.classification                     |
| *@level*         | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: LCIAMethod.classification.class               |
| *generalComment* | \<description here\>                                     |
|                  | Type: String Multilang                                   |
|                  | Ancestors: LCIAMethod                                    |
| *type* (lciamethod)         | \<description here\>                          |
|                  | Type: String                                             |
|                  | Ancestors: LCIAMethod                                    |
| *methodology* (lciamethod)    | \<description here\>                                     |
|      | Type: String                                             |
|                  | may occur multiple times                                 |
|                  | Ancestors: LCIAMethod                                    |
| *impactCategory* (lciamethod)| \<description here\>                                     |
|      | Type: String                                             |
|                  | Ancestors: LCIAMethod                                    |
| *areaOfProtection* (lciamethod) | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: LCIAMethod                                    |
| *impactIndicator* (lciamethod) | \<description here\>                                     |
|                  | Type: String                                             |
|                  | Ancestors: LCIAMethod                                    |
| *time* (lciamethod) | \<description here\>                                     |
|      | Type: String                                             |
|                  | Ancestors: LCIAMethod                                    |
| *referenceYear* (lciamethod) | \<description here\>                                     |
|      | Type: String Multilang                                   |
|                  | Ancestors: LCIAMethod.time                               |
| *duration* (lciamethod)       | \<description here\>                                     |
|      | Type: String Multilang                                   |
|                  | Ancestors: LCIAMethod.time                               |
