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
        return Map.of();
    }

    @Override
    public List<Map<String, String>> listAccessKeys(String userName) {
        return List.of();
    }

    @Override
    public void deleteAccessKey(String userName, String accessKeyId) {

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
    public String createRole(String roleName, String trustPolicy, String description) {
        return "";
    }

    @Override
    public void deleteRole(String roleName) {

    }

    @Override
    public Map<String, String> getRoleDetails(String roleName) {
        return Map.of();
    }

    @Override
    public List<String> listRoles() {
        return List.of();
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
    public String createGroup(String groupName) {
        return "";
    }

    @Override
    public void addUserToGroup(String groupName, String userName) {

    }

    @Override
    public List<String> listGroups() {
        return List.of();
    }

    @Override
    public List<String> listUsersInGroup(String groupName) {
        return List.of();
    }
}
