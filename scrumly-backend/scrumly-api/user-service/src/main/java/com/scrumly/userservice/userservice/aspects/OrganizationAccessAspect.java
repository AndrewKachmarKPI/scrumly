package com.scrumly.userservice.userservice.aspects;

import com.scrumly.userservice.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class OrganizationAccessAspect {
    private final UserService userService;


}
