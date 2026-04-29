package com.example.aws.service;

import java.util.List;
import java.util.Map;

public interface IamService {

    Map<String, String> listUsers();

    Map<String, String> createUser(String userName);

    void deleteUser(String userName);

    Map<String, String> getUserDetails(String userName);

    Map<String, String> createAccessKey(String userName);

    List<Map<String, String>> listAccessKeys(String userName);

    void deleteAccessKey(String userName, String accessKeyId);

    Map<String, String> createRole(String roleName, String trustPolicy, String description);

    void deleteRole(String roleName);

    Map<String, String> getRoleDetails(String roleName);

    Map<String, String> listRoles();

    Map<String, String> createGroup(String groupName);

    void deleteGroup(String groupName);

    void addUserToGroup(String groupName, String userName);

    void removeUserFromGroup(String groupName, String userName);

    Map<String, String> listGroups();

    List<String> listUsersInGroup(String groupName);

    //TODO finish implementation

    void attachManagedPolicyToUser(String userName, String policyArn);

    void detachPolicyFromUser(String userName, String policyArn);

    List<Map<String, String>> listUserPolicies(String userName);

    void putInlinePolicy(String userName, String policyName, String policyDocument);

    void deleteInlinePolicy(String userName, String policyName);

    String getInlinePolicy(String userName, String policyName);

    void attachManagedPolicyToRole(String roleName, String policyArn);

    void detachPolicyFromRole(String roleName, String policyArn);

    List<Map<String, String>> listRolePolicies(String roleName);
}
