package com.datalabel.common;

import com.datalabel.entity.User;
import com.datalabel.service.OrganizationService;
import com.datalabel.service.RoleOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataPermissionUtils {
    
    @Autowired
    private RoleOrganizationService roleOrganizationService;
    
    @Autowired
    private OrganizationService organizationService;
    
    /**
     * 判断用户是否是超级管理员
     * userType == 1 表示是管理员类型，但只有同时拥有所有组织权限的才是超级管理员
     * 注意：超级管理员在getAccessibleOrgIds中返回null，表示拥有全部权限
     */
    public boolean isAdmin(User user) {
        // 简单判断：userType == 1 就是超级管理员
        // 组织权限通过getAccessibleOrgIds来控制
        return user != null && user.getUserType() != null && user.getUserType() == 1;
    }
    
    /**
     * 检查角色是否有超出用户权限范围的组织权限
     */
    public boolean isRoleHasExtraOrgPermission(User user, Long roleId) {
        List<Long> userOrgIds = getAccessibleOrgIds(user);
        List<Long> roleOrgIds = roleOrganizationService.findOrgIdsByRoleId(roleId);
        
        // 如果角色没有绑定任何组织权限，那么角色可能有全部权限
        if (roleOrgIds == null || roleOrgIds.isEmpty()) {
            // 超级管理员可以分配无组织限制的角色，普通管理员不行
            return !isAdmin(user);
        }
        
        // 超级管理员可以看到所有组织，null表示全部权限
        if (userOrgIds == null) {
            return false; // 超级管理员可以分配任何角色
        }
        
        // 检查角色的组织权限是否都在用户的权限范围内
        Set<Long> userOrgSet = new HashSet<>(userOrgIds);
        for (Long orgId : roleOrgIds) {
            if (!userOrgSet.contains(orgId)) {
                return true; // 角色有权限范围外的组织
            }
        }
        
        return false;
    }
    
    public List<Long> getAccessibleOrgIds(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        
        if (isAdmin(user)) {
            return null;
        }
        
        Long roleId = user.getRoleId();
        if (roleId == null) {
            return Collections.emptyList();
        }
        
        List<Long> directOrgIds = roleOrganizationService.findOrgIdsByRoleId(roleId);
        Set<Long> allOrgIds = new HashSet<>(directOrgIds);
        
        for (Long orgId : directOrgIds) {
            List<Long> childrenIds = organizationService.findAllChildrenIds(orgId);
            allOrgIds.addAll(childrenIds);
        }
        
        return new java.util.ArrayList<>(allOrgIds);
    }
    
    public boolean hasOrgPermission(User user, Long orgId) {
        if (orgId == null) {
            return false;
        }
        
        if (isAdmin(user)) {
            return true;
        }
        
        List<Long> orgIds = getAccessibleOrgIds(user);
        return orgIds.contains(orgId);
    }
    
    public User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }
}
