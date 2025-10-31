package org.joget.marketplace;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DatalistPermission;
import org.joget.apps.form.model.FormPermission;
import org.joget.apps.userview.model.UserviewPermission;
import org.joget.commons.spring.web.ParameterizedUrlHandlerMapping;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Group;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.ApplicationContext;

public class UserRoleSelectorPermission extends UserviewPermission implements FormPermission, DatalistPermission {
    public final static String MESSAGE_PATH = "messages/UserRoleSelectorPermission";
    
    public String getName() {
        return AppPluginUtil.getMessage("org.joget.marketplace.UserRoleSelectorPermission.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    public String getVersion() {
        return "8.0.4";
    }

    public String getDescription() {
        return AppPluginUtil.getMessage("org.joget.marketplace.UserRoleSelectorPermission.pluginDesc", getClassName(), MESSAGE_PATH);
    }

    public String getLabel() {
        return AppPluginUtil.getMessage("org.joget.marketplace.UserRoleSelectorPermission.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    public String getClassName() {
        return getClass().getName();
    }
    
    @Override
    public boolean isAuthorize() {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        
        ApplicationContext ac = AppUtil.getApplicationContext();
        ExtDirectoryManager directoryManager = (ExtDirectoryManager) ac.getBean("directoryManager");
        User user = getCurrentUser();
        
        String userGroup = getPropertyString("groupId");
        String userviewKeyValue = "";
        
        Map<String, String> params = (Map) request.getAttribute(ParameterizedUrlHandlerMapping.PATH_PARAMETERS);
        if (params != null && params.containsKey("key")) {
            userviewKeyValue = params.get("key");
        }
        
//        LogUtil.info(this.getClassName(), "params.userviewId " + "[" + params.get("userviewId") + "]");
//        LogUtil.info(this.getClassName(), "params.menuId " + "[" + params.get("menuId") + "]");
//        LogUtil.info(this.getClassName(), "params.appId " + "[" + params.get("appId") + "]");
        
        if(userviewKeyValue.equalsIgnoreCase("_") || userviewKeyValue.startsWith("#") || userviewKeyValue.isEmpty()){
            //LogUtil.info(this.getClassName(), user.getUsername() + " for permission user group [" + userGroup + "] : false");
            return false;
        }

        if(!userviewKeyValue.equals(userGroup)){
            //LogUtil.info(this.getClassName(), user.getUsername() + " for permission user group [" + userGroup + "] : false");
            return false;
        }
        
        Collection<Group> groups = directoryManager.getGroupByUsername(user.getUsername());
            
        if (groups != null) {
            for (Group g : groups) {
                if (userGroup.equals(g.getId())) {
                    //if user belongs to the current selected group, then it is authorized
                    //LogUtil.info(this.getClassName(), user.getUsername() + " for permission user group [" + g.getName() + "] : true");
                    return true;
                }
            }
        }
        
        //LogUtil.info(this.getClassName(), user.getUsername() + " for permission user group [" + userGroup + "] : false");
        return false;
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/UserRoleSelectorPermission.json", null, true, MESSAGE_PATH);
    }
    
}
