package de.iai.ilcd.model.adapter;

import de.fzk.iai.ilcd.service.client.impl.vo.dataset.*;
import de.fzk.iai.ilcd.service.model.*;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.adapter.dataset.*;

import java.util.List;

public class DataSetListAdapter extends DataSetList {

    public DataSetListAdapter(List<? extends IDataSetListVO> list) {
        this(list, null);
    }

    public DataSetListAdapter(List<? extends IDataSetListVO> list, String language) {
        this(list, language, false);
    }

    public DataSetListAdapter(List<? extends IDataSetListVO> list, String language, boolean langFallback) {

        List<DataSetVO> lst = this.getDataSet();

        for (IDataSetListVO dataset : list) {
            if (dataset instanceof IProcessListVO) {
                ProcessDataSetVO d = new ProcessVOAdapter((IProcessListVO) dataset, language, langFallback).getDataSet();
                addItem(d, lst, language, langFallback);
            } else if (dataset instanceof ILCIAMethodListVO) {
                LCIAMethodDataSetVO d = new LCIAMethodVOAdapter((ILCIAMethodListVO) dataset, language, langFallback).getDataSet();
                addItem(d, lst, language, langFallback);
            } else if (dataset instanceof IFlowListVO) {
                FlowDataSetVO d = new FlowVOAdapter((IFlowListVO) dataset, language, langFallback).getDataSet();
                addItem(d, lst, language, langFallback);
            } else if (dataset instanceof IFlowPropertyListVO) {
                FlowPropertyDataSetVO d = new FlowPropertyVOAdapter((IFlowPropertyListVO) dataset, language, langFallback).getDataSet();
                addItem(d, lst, language, langFallback);
            } else if (dataset instanceof IUnitGroupListVO) {
                UnitGroupDataSetVO d = new UnitGroupVOAdapter((IUnitGroupListVO) dataset, language, langFallback).getDataSet();
                addItem(d, lst, language, langFallback);
            } else if (dataset instanceof ISourceListVO) {
                SourceDataSetVO d = new SourceVOAdapter((ISourceListVO) dataset, language, langFallback).getDataSet();
                addItem(d, lst, language, langFallback);
            } else if (dataset instanceof IContactListVO) {
                ContactDataSetVO d = new ContactVOAdapter((IContactListVO) dataset, language, langFallback).getDataSet();
                addItem(d, lst, language, langFallback);
            } else if (dataset instanceof ILifeCycleModelListVO) {
                LifeCycleModelDataSetVO d = new LifeCycleModelVOAdapter((ILifeCycleModelListVO) dataset, language, langFallback).getDataSet();
                addItem(d, lst, language, langFallback);
            }
        }
    }

    private void addItem(DataSetVO d, List<DataSetVO> lst, String language, boolean langFallback) {
        if (language == null)
            lst.add(d);
        else if (!langFallback && d.getName().getValue(language) != null)
            lst.add(d);
        else if (langFallback) {
            for (String lang : ConfigurationService.INSTANCE.getPreferredLanguages()) {
                if (d.getName().getValue(lang) != null) {
                    lst.add(d);
                    break;
                }
            }
        }
    }
}
