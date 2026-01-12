package org.joget.marketplace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.spring.web.ParameterizedUrlHandlerMapping;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Group;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

public class UserRoleSelectorHashVariable extends DefaultHashVariablePlugin {
    
    private final static String MESSAGE_PATH = "messages/UserRoleSelectorHashVariable";
    
    public String getName() {
        return AppPluginUtil.getMessage("org.joget.marketplace.UserRoleSelectorHashVariable.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    public String getPrefix() {
        return "userRoleSelector";
    }

    public String getVersion() {
        return Activator.PLUGIN_VERSION;
    }

    public String getDescription() {
        return AppPluginUtil.getMessage("org.joget.marketplace.UserRoleSelectorHashVariable.pluginDesc", getClassName(), MESSAGE_PATH);
    }

    public String getLabel() {
        return AppPluginUtil.getMessage("org.joget.marketplace.UserRoleSelectorHashVariable.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }

    @Override
    public String processHashVariable(String variableKey) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        WorkflowUserManager wum = (WorkflowUserManager)AppUtil.getApplicationContext().getBean("workflowUserManager");
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ApplicationContext ac = AppUtil.getApplicationContext();
        ExtDirectoryManager directoryManager = (ExtDirectoryManager) ac.getBean("directoryManager");
        
        User user = wum.getCurrentUser();

        if (user == null) {
            return "";
        }
        
        Map<String, String> params = (Map) request.getAttribute(ParameterizedUrlHandlerMapping.PATH_PARAMETERS);
        
        String appDefAppId = "";
        String currentUserviewID = "";
        AppDefinition appDef = null;
        try{
            appDef = (AppDefinition) getProperty("appDefinition");
            appDefAppId = appDef.getAppId();
            
            currentUserviewID = params.get("userviewId");
        }catch(Exception ex){
            
        }
        
        //get current userview
        
        Collection<UserviewDefinition> uvDefList = appDef.getUserviewDefinitionList();
        UserviewDefinition currentUvDef = null;
        
        for (UserviewDefinition uvDef : uvDefList) {
            if(uvDef.getId().equalsIgnoreCase(currentUserviewID)){
                currentUvDef = uvDef;
                break;
            }
        }
        
        if(currentUvDef == null){
            return "";
        }
        
        JSONObject userviewObj = new JSONObject(currentUvDef.getJson());
        JSONObject settingObj = userviewObj.getJSONObject("setting");
        Collection<String> permissionFound = new ArrayList();
                
        //at userview settings > adv tools > permissions 
        if (settingObj.getJSONObject("properties").has("permission_rules")) {
            JSONArray permissionRules = settingObj.getJSONObject("properties").getJSONArray("permission_rules");
            if (permissionRules != null && permissionRules.length() > 0) {
                for (int i = 0; i < permissionRules.length(); i++) {
                    JSONObject rule = permissionRules.getJSONObject(i);
                    if (rule.has("permission")) {
                        JSONObject permissionObj = rule.optJSONObject("permission");
                        if(permissionObj.get("className").equals("org.joget.marketplace.UserRoleSelectorPermission")){
                            permissionFound.add(permissionObj.getJSONObject("properties").getString("groupId"));
                        }
                    }
                }
            }
        }
        
        //at userview settings > permission level
        if (settingObj.getJSONObject("properties").has("permission")){
            JSONObject rule = settingObj.getJSONObject("properties").getJSONObject("permission");
            if(rule.get("className").equals("org.joget.marketplace.UserRoleSelectorPermission")){
                permissionFound.add(rule.getJSONObject("properties").getString("groupId"));
            }
        }
        
        String currentUserviewKeyValue = "";
        
        if (params != null && params.containsKey("key")) {
            currentUserviewKeyValue = params.get("key");
        }
        
//        LogUtil.info("params.userviewId", params.get("userviewId"));
//        LogUtil.info("params.menuId", params.get("menuId"));
//        LogUtil.info("params.appId", params.get("appId"));
        
        Map dataModel = new HashMap();
        dataModel.put("currentUserViewKeyValue", currentUserviewKeyValue);
        dataModel.put("userviewId", params.get("userviewId"));
        dataModel.put("appId", params.get("appId"));
        dataModel.put("menuId", params.get("menuId"));
        
        Collection<Group> groups = directoryManager.getGroupByUsername(user.getUsername());
            
        Collection<Map> optionsMap = new ArrayList<>();

        if (groups != null) {
            for (Group g : groups) {
                //only show groups that are used in the userview AND has the user in it.
                if(permissionFound.contains(g.getId())){
                    Map m = new HashMap();
                    m.put("id", g.getId());
                    m.put("label", g.getName());
                    m.put("selected", currentUserviewKeyValue.equalsIgnoreCase(g.getId()));
                    optionsMap.add(m);
                }
            }
        }
        
        dataModel.put("options", optionsMap);
        
        //first check and retrieve parameters passed in with URL query parameters syntax wrapped in square bracket []
        String message = "";
        if (variableKey.contains("[") && variableKey.contains("]")) {
            message = variableKey.substring(variableKey.indexOf("[") + 1, variableKey.indexOf("]"));
            
            variableKey = variableKey.substring(0, variableKey.indexOf("["));
        }
        
        String current = "";
        String choose = "";
                
        try{
            current = message.split("\\|")[0];
            choose = message.split("\\|")[1];
        }catch(Exception ex){
            
        }
        
        if(!current.isEmpty()){
            dataModel.put("current", current);
        }else{
            dataModel.put("current", "<i class=\"fas fa-user-circle\"></i>");
        }
        
        if(!choose.isEmpty()){
            dataModel.put("choose", choose);
        }else{
            dataModel.put("choose", "<i class=\"far fa-user-circle\"></i>");
        }
        
        if(variableKey.equals("single")){
            return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/UserRoleSelection.ftl", MESSAGE_PATH);
        }else{
            return "";
        }
    }
    
    @Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<String>();
        syntax.add("userRoleSelector.single");
        
        return syntax;
    }
    
    @Override
    public String getPropertyAssistantDefinition() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/UserRoleSelectorHashVariable.json", null, true, MESSAGE_PATH);
    }

}