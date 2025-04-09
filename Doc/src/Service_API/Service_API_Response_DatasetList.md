---

DatasetList Response Elements
-----------------------------

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *dataSetList*  | The container element for the list of dataset objects.    |
|                | Type: Container                                           |
|                | Ancestors: None                                           |
| *@sourceId*    | Node ID of the originating node.                          |
|                | Type: String                                              |
|                | Ancestors: dataSetList                                    |
| *@totalSize*   | Total size of the result set, i.e. number of contained dataset objects. |
|                | Type: Integer                                             |
|                | Ancestors: dataSetList                                    |
| *@startIndex*  | The index of the first item in this list in relation to the entire result set. |
|                | Type: Integer                                             |
|                | Ancestors: dataSetList                                    |
| *@pageSize*    | The page size (number of items) for this list.            |
|                | Type: Integer                                             |
|                | Ancestors: dataSetList                                    |
| *process* (process)      | A process dataset object. See section ["Process Response Elements"](./Service_API_Response_Process.md) for a detailed description. |
|                | Type: Container                                           |
|                | optional, may occur multiple times                        |
|                | Ancestors: dataSetList                                    |
| *flow* (flow)        | A flow dataset object. See section ["Flow Response Elements"](./Service_API_Response_Flow.md) for a detailed description. |
|                | Type: Container                                           |
|                | optional, may occur multiple times                        |
|                | Ancestors: dataSetList                                    |
| *flowProperty* (flowproperty) | A flow property dataset object. See section ["FlowProperty Response Elements"](./Service_API_Response_FlowProperty.md) for a detailed description.              |
|                | Type: Container                                           |
|                | optional, may occur multiple times                        |
|                | Ancestors: dataSetList                                    |
| *unitGroup*(unitgroup)    | A unit group dataset object. See section ["UnitGroup Response Elements"](./Service_API_Response_UnitGroup.md) for a detailed description.     |
|                | Type: Container                                           |
|                | optional, may occur multiple times                        |
|                | Ancestors: dataSetList                                    |
| *contact* (contact) | A source dataset object. See section ["Source Response Elements"](./Service_API_Response_Source.md) for a detailed description. |
|                | Type: Container                                           |
|                | optional, may occur multiple times                        |
|                | Ancestors: dataSetList                                    |
| *contact* (contact)     | A contact dataset object. See section ["Contact Response Elements"](./Service_API_Response_Contact.md) for a detailed description. |
|                | Type: Container                                           |
|                | optional, may occur multiple times                        |
|                | Ancestors: dataSetList                                    |
| *lciamethod* (lciamethod)   | An LCIA method dataset object. See section ["LCIAMethod Response Elements"](./Service_API_Response_LCIAMethod.md) for a detailed description. |
|                | Type: Container                                           |
|                | optional, may occur multiple times                        |
|                | Ancestors: dataSetList                                    |
