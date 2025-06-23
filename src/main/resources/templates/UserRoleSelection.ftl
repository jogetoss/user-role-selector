<script>
   $(function(){
    const roleSelections = $("#userRoleSelections li");
    let targetMenu = null;
    
    if ($('body').hasClass('horizontal_menu')) {
        targetMenu = $("ul.dropdown-menu");
    } else if ($("li.user-link ul.dropdown-menu").length > 0) {
        targetMenu = $("li.user-link ul.dropdown-menu");
    } else if ($("ul.user-menu ul").length > 0) {
        targetMenu = $("ul.user-menu ul");
    }
    
    if (targetMenu && targetMenu.length > 0) {
        targetMenu.append(roleSelections);
    } else {
        console.log("Unable to locate target menu for user role selection injection");
    }
});
</script>

<div id="userRoleSelections" style="display: none;">
    <#list options as option>
        <li>
            <a class="switchRole" title="Switch to ${option.label!?html}" onClick="window.location='/jw/web/userview/${appId!}/${userviewId!}/${option.id!}/';return false;">
            
            <#if option.selected>
                ${current!}
            <#else>
                ${choose!}
            </#if>

             ${option.label!?html}</a>
        </li>
    </#list>
</div>
