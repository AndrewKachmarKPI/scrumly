package com.scrumly.userservice.userservice.services;

import com.scrumly.enums.userservice.PermissionType;

import java.util.List;

public interface PermissionService {
    boolean hasPermission(PermissionType permission,
                          String username,
                          List<String> params);

}
