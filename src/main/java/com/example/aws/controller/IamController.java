package com.example.aws.controller;

import com.example.aws.model.RoleRequestBody;
import com.example.aws.service.IamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/iam")
public class IamController {

    private final IamService iamService;

    @GetMapping("/user")
    public Map<String, String> listUsers() {
        return iamService.listUsers();
    }

    @PostMapping("/user/{userName}")
    public Map<String, String> createUser(@PathVariable String userName){
        return iamService.createUser(userName);
    }

    //userName is unique for an aws account
    @GetMapping("/user/{userName}/details")
    public Map<String, String> getUserDetails(@PathVariable String userName){
        return iamService.getUserDetails(userName);
    }

    @DeleteMapping("/user/{userName}")
    public void deleteUser(@PathVariable String userName){
        iamService.deleteUser(userName);
    }

    @PostMapping("/group/{groupName}")
    public Map<String, String> createGroup(@PathVariable String groupName){
        return iamService.createGroup(groupName);
    }

    @DeleteMapping("/group/{groupName}")
    public void deleteGroup(@PathVariable String groupName){
        iamService.deleteGroup(groupName);
    }

    @PutMapping("/group/{groupName}/user/{userName}/add")
    public void addUserToGroup(@PathVariable String groupName, @PathVariable String userName){
        iamService.addUserToGroup(groupName,userName);
    }

    @PutMapping("/group/{groupName}/user/{userName}/del")
    public void removeUserFromGroup(@PathVariable String groupName, @PathVariable String userName){
        iamService.removeUserFromGroup(groupName,userName);
    }

    @GetMapping("/group")
    public Map<String, String> listGroups(){
        return iamService.listGroups();
    }

    @GetMapping("/group/{groupName}")
    public List<String> listUsersInGroup(@PathVariable String groupName){
        return iamService.listUsersInGroup(groupName);
    }

    @PostMapping("/role/{roleName}")
    public Map<String, String> createRole(@PathVariable String roleName, @RequestBody RoleRequestBody body){
        return iamService.createRole(roleName,body.trustPolicy(),body.description());
    }

    @DeleteMapping("/role/{roleName}")
    public void deleteRole(@PathVariable String roleName){
        iamService.deleteRole(roleName);
    }

    @GetMapping("/role/{roleName}")
    public Map<String, String> getRoleDetails(@PathVariable String roleName){
        return iamService.getRoleDetails(roleName);
    }

    @GetMapping("/role")
    public Map<String,String> listRoles(){
        return iamService.listRoles();
    }

    @PostMapping("/key/user/{userName}")
    public Map<String, String> createAccessKey(@PathVariable String userName){
        return iamService.createAccessKey(userName);
    }

    @GetMapping("/key/user/{userName}")
    public List<Map<String, String>> listAccessKeys(@PathVariable String userName){
        return iamService.listAccessKeys(userName);
    }

    @DeleteMapping("/key/{accessKeyId}/user/{userName}")
    public void deleteAccessKey(@PathVariable String userName, @PathVariable String accessKeyId){
        iamService.deleteAccessKey(userName,accessKeyId);
    }
}
