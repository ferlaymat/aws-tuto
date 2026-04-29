package com.example.aws.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class IamServiceImpl implements IamService {

    private final IamClient iamClient;

    @Override
    public Map<String, String> listUsers() {

        return iamClient.listUsers().users().stream().collect(Collectors.toMap(User::userId, User::userName));
    }

    @Override
    public Map<String, String> createUser(String userName) {
        CreateUserRequest request = CreateUserRequest.builder().userName(userName).build();
        CreateUserResponse response = iamClient.createUser(request);
        User user = response.user();

        return Map.of(
                "userName", user.userName(),
                "arn", user.arn(),
                "userId", user.userId(),
                "createDate", user.createDate().toString());
    }

    @Override
    public void deleteUser(String userName) {
        DeleteUserRequest request = DeleteUserRequest.builder().userName(userName).build();
        iamClient.deleteUser(request);
    }

    @Override
    public Map<String, String> getUserDetails(String userName) {
        GetUserRequest request = GetUserRequest.builder().userName(userName).build();
        GetUserResponse response = iamClient.getUser(request);

        User user = response.user();

        return Map.of(
                "userName", user.userName(),
                "arn", user.arn(),
                "userId", user.userId(),
                "createDate", user.createDate().toString());

    }

    @Override
    public Map<String, String> createAccessKey(String userName) {
        CreateAccessKeyRequest request = CreateAccessKeyRequest.builder().userName(userName).build();
        CreateAccessKeyResponse response = iamClient.createAccessKey(request);

        return Map.of("keyId", response.accessKey().accessKeyId(), "secretKey", response.accessKey().secretAccessKey());
    }

    @Override
    public List<Map<String, String>> listAccessKeys(String userName) {
        ListAccessKeysRequest request = ListAccessKeysRequest.builder()
                .userName(userName)
                .build();
        ListAccessKeysResponse response = iamClient.listAccessKeys(request);
        return response.accessKeyMetadata().stream()
                .map(keyMetadata -> Map.of(
                        "accessKeyId", keyMetadata.accessKeyId(),
                        "status", keyMetadata.status().toString(),
                        "createDate", keyMetadata.createDate().toString()
                ))
                .toList();

    }

    @Override
    public void deleteAccessKey(String userName, String accessKeyId) {
        DeleteAccessKeyRequest request = DeleteAccessKeyRequest.builder().userName(userName).accessKeyId(accessKeyId).build();
        iamClient.deleteAccessKey(request);
    }

    @Override
    public void attachManagedPolicyToUser(String userName, String policyArn) {

    }

    @Override
    public void detachPolicyFromUser(String userName, String policyArn) {

    }

    @Override
    public List<Map<String, String>> listUserPolicies(String userName) {
        return List.of();
    }

    @Override
    public void putInlinePolicy(String userName, String policyName, String policyDocument) {

    }

    @Override
    public void deleteInlinePolicy(String userName, String policyName) {

    }

    @Override
    public String getInlinePolicy(String userName, String policyName) {
        return "";
    }

    @Override
    public Map<String, String> createRole(String roleName, String trustPolicy, String description) {
        CreateRoleRequest request = CreateRoleRequest.builder().roleName(roleName).description(description).assumeRolePolicyDocument(trustPolicy).build();
        CreateRoleResponse response = iamClient.createRole(request);
        return Map.of(
                "roleId", response.role().roleId(),
                "roleName", response.role().roleName(),
                "arn", response.role().arn(),
                "createdAt", response.role().createDate().toString()
        );
    }

    @Override
    public void deleteRole(String roleName) {
        DeleteRoleRequest request = DeleteRoleRequest.builder().roleName(roleName).build();
        iamClient.deleteRole(request);
    }

    @Override
    public Map<String, String> getRoleDetails(String roleName) {
        GetRoleRequest request = GetRoleRequest.builder().roleName(roleName).build();
        GetRoleResponse response = iamClient.getRole(request);
        return Map.of(
                "roleId", response.role().roleId(),
                "roleName", response.role().roleName(),
                "arn", response.role().arn(),
                "createdAt", response.role().createDate().toString()
        );
    }

    @Override
    public Map<String, String> listRoles() {
        return iamClient.listRoles().roles().stream().collect(Collectors.toMap(Role::roleId, Role::roleName));
    }

    @Override
    public void attachManagedPolicyToRole(String roleName, String policyArn) {

    }

    @Override
    public void detachPolicyFromRole(String roleName, String policyArn) {

    }

    @Override
    public List<Map<String, String>> listRolePolicies(String roleName) {
        return List.of();
    }

    @Override
    public Map<String, String> createGroup(String groupName) {
        CreateGroupRequest request = CreateGroupRequest.builder().groupName(groupName).build();
        CreateGroupResponse response = iamClient.createGroup(request);
        return Map.of(
                "groupId", response.group().groupId(),
                "groupName", response.group().groupName(),
                "arn", response.group().arn(),
                "createdAt", response.group().createDate().toString()
        );
    }

    @Override
    public void deleteGroup(String groupName) {
        DeleteGroupRequest request = DeleteGroupRequest.builder().groupName(groupName).build();
        iamClient.deleteGroup(request);
    }

    @Override
    public void addUserToGroup(String groupName, String userName) {
        AddUserToGroupRequest request = AddUserToGroupRequest.builder().groupName(groupName).userName(userName).build();
        iamClient.addUserToGroup(request);
    }

    @Override
    public void removeUserFromGroup(String groupName, String userName) {
        RemoveUserFromGroupRequest request = RemoveUserFromGroupRequest.builder().groupName(groupName).userName(userName).build();
        iamClient.removeUserFromGroup(request);
    }

    @Override
    public Map<String, String> listGroups() {
        return iamClient.listGroups().groups().stream().collect(Collectors.toMap(Group::groupId, Group::groupName));
    }

    @Override
    public List<String> listUsersInGroup(String groupName) {
        GetGroupRequest request = GetGroupRequest.builder().groupName(groupName).build();
        GetGroupResponse response = iamClient.getGroup(request);
        return response.users().stream().map(User::userName).toList();
    }
}
