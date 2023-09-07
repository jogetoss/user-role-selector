<script>
    $(function(){
        if ($('body')[0].classList.contains('horizontal_menu')) {
            $("ul.dropdown-menu").append( $("#userRoleSelections li") );
        } else {
            $("ul.user-menu").find("ul").append( $("#userRoleSelections li") );
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
