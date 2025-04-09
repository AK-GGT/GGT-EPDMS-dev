![ ](images/soda4LCA_logo_sm.png)

# soda4LCA GLAD Guide


Setting Up GLAD 
===================
To be able to use the GLAD framework, the GLAD feature needs to be enabled in the 
`soda4LCA.properties` configuration file.
To enable the GLAD feature, the following properties should be set:
 
- Enabling the GLAD feature: `feature.glad = true`

- Setting the GLAD URL:
  - `feature.glad.url = https://sandbox.globallcadataaccess.org` for the test system
  - or
  - `feature.glad.url = https://www.globallcadataaccess.org` for the production system
 
- Setting the API key for access to GLAD database: `feature.glad.apikey = 756b8d7814d943ec842dfefe97812d10`

Please note that separate API keys are being issued for test and production instances.

Reload the configuration via the admin menu or restart your instance for these
settings to take effect.
 
 
 Change/Show Data Provider Name And Global Properties for GLAD
-----------------------------------------
To show the data provider name for GLAD and all GLAD-specific properties which are globally set for all assigned data sets, 
either select 'Global Configuration'->'Global Parameters for GLAD'
in the admin menu shown at the top of the page or click on the 'Global Parameters for GLAD' 
button shown in the admin index page. Then the data provider name for 
GLAD is shown in the text field labeled 'Data Provider (GLAD)'.
   

![ ](images/soda4LCA_GLAD_guide_show_glad_configuration.png)
   
 
![ ](images/soda4LCA_GLAD_guide_GLAD_provider_unchanged.png)
   
In order to change this name, type in the wanted new name and save 
the changes by clicking on button labeled 'Save configuration'.
   

![ ](images/soda4LCA_GLAD_guide_GLAD_provider_new.png)

Changing a global GLAD property is analogous to changing or setting the data provider name.
If you are on the 'Global Parameters for GLAD' page, then you can configure the properties for GLAD which are set globally 
(for all assigned data sets) by clicking on the drop-down menu of the to be changed property and then clicking on the option the property shall have.

In the last step, click on the 'Save configuration' button to save all changes made on this view.
   
Register a Data Set to GLAD
============================
1. Before a data set can be registered to GLAD, the GLAD feature has to be enabled 
   (if not done yet, look up Section 1 - 'Setting Up GLAD' for further instructions) 
   and one has to be authenticated (see Administration guide for further information). 
   To register a data set to GLAD, start from the admin index page and head to the 
   'Manage Processes' page (either by clicking on the given button in the index page 
   or by selecting 'Manage Datasets' -> 'Manage Processes' in the admin menu at top of page).

2. In the next step select by clicking on the selection box left of data set name 
   you want to register to GLAD and then click on the button 'Register'. You will be 
   directed to a page containing an overview of data sets you want to register.
   
   
   ![ ](images/soda4LCA_GLAD_guide_select_1.png)
   
   
   ![ ](images/soda4LCA_GLAD_guide_register_button_1.png)
   
   If you want to register all data sets from given page then click on the selection box 
   right above the first data set name as shown in image below. Then click on the button 
   'Register' to get to the next step.
   
   
   ![ ](images/soda4LCA_GLAD_guide_select_all.png)
   
   
   ![ ](images/soda4LCA_GLAD_guide_register_button_all.png)
   
3. At the bottom on the page, click at the drop-down menu to be able to see all registries 
   you can register to (GLAD should be included in this menu) and select 'GLAD'. 
   Then click on the button 'register'.
   
   
   ![ ](images/soda4LCA_GLAD_guide_1_register_in.png)
   
   
   ![ ](images/soda4LCA_GLAD_guide_1_register_in_button.png)
   
4. In the last step, you are directed to a summary. This page shows how many data sets were 
   successfully registered to GLAD and how many data sets were not registered. 
   
   
   ![ ](images/soda4LCA_GLAD_guide_summary_1.png)
    
   Version with all data sets registered:
   
   
   ![ ](images/soda4LCA_GLAD_guide_summary_all.png)
   
   If some data sets could 
   not be registered to GLAD then the given data sets and the reason why the data sets could 
   not be registered are shown in the table below. Possible reasons are:

   - The data set is rejected by GLAD
 
   - The data set is already registered to GLAD 
  
   - An error occurred while data set was registered
   
   
   ![ ](images/soda4LCA_GLAD_guide_summary_exists.png)
   
   
   ![ ](images/soda4LCA_GLAD_guide_summary_exists_2.png)
  

Show All Data Sets Registered to GLAD
======================================
1. To show all data sets registered to GLAD, go to the 'Manage Processes' view. 
   At the top left at the page is a drop-down menu labeled 'Registered in:'. 
   
   
   ![ ](images/soda4LCA_GLAD_guide_select_registered_click.png)
   
2. Click at the drop-down menu and select GLAD. Now all data sets which are registered 
   to GLAD from given data stock are shown in the table below.
   
   
   ![ ](images/soda4LCA_GLAD_guide_registered_GLAD.png)
   

Deregister a Data Set From GLAD
===============================
1. To deregister a data set from GLAD, go to the 'Manage Processes' view. 

2. Then change the view of processes by selecting 'GLAD' in the drop-down menu 
   labeled with 'registered in' (for further instructions see Section 3 - 
   'Show All Data Sets Registered to GLAD'). Now all data sets registered 
   to GLAD from given data stock are shown.
 
3. In the next step select the given data set(s) you want to deregister from 
   GLAD by selecting the check box left of the name of data set.
   
   
   ![ ](images/soda4LCA_GLAD_guide_deregister_1_select.png)
   
   Variant with all selected data sets
   
   
   ![ ](images/soda4LCA_GLAD_guide_deregister_all_select.png)
   
4. Lastly click on the button 'Deregister' to deregister selected data sets from GLAD.
   
   
   ![ ](images/soda4LCA_GLAD_guide_deregister_1_button.png)