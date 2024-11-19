package digit.service;

import digit.config.Configuration;
import digit.util.UserUtils;
import digit.web.models.*;


import org.egov.common.contract.request.RequestInfo;
import digit.web.models.User;
import digit.web.models.UserDetailResponse;
import digit.web.models.UserSearchRequest;
import digit.web.models.CreateUserRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import digit.config.ServiceConstants;
import org.springframework.util.CollectionUtils;


import org.egov.common.contract.request.RequestInfo;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static digit.config.ServiceConstants.USERTYPE_CITIZEN;


@org.springframework.stereotype.Service
public class UserService {


    private UserUtils userUtils;

    private Configuration config;

    @Autowired
    public UserService(UserUtils userUtils, Configuration config) {
        this.userUtils = userUtils;
        this.config = config;
    }

    /**
     * Calls user service to enrich user from search or upsert user
     * @param request
     */
    public void callUserService(ServiceRequest request){

        if(!StringUtils.isEmpty(request.getServiceWrapper().getService().getAccountId()))
            enrichUser(request);
        else if(request.getServiceWrapper().getService().getCitizen()!=null)
            upsertUser(request);

    }


    /**
     * Creates or updates the user based on if the user exists. The user existance is searched based on userName = mobileNumber
     * If the there is already a user with that mobileNumber, the existing user is updated
     * @param request
     */
    private void upsertUser(ServiceRequest request){

        User user = request.getServiceWrapper().getService().getCitizen();
        String tenantId = request.getServiceWrapper().getService().getTenantId();
        User userServiceResponse = null;

        // Search on mobile number as user name
        UserDetailResponse userDetailResponse = searchUser(userUtils.getStateLevelTenant(tenantId),null, user.getMobileNumber());
        if (!userDetailResponse.getUser().isEmpty()) {
            User userFromSearch = userDetailResponse.getUser().get(0);
            if(!user.getName().equalsIgnoreCase(userFromSearch.getName())){
                userServiceResponse = updateUser(request.getRequestInfo(),user,userFromSearch);
            }
            else userServiceResponse = userDetailResponse.getUser().get(0);
        }
        else {
            userServiceResponse = createUser(request.getRequestInfo(),tenantId,user);
        }

        // Enrich the accountId
        request.getServiceWrapper().getService().setAccountId(userServiceResponse.getUuid());
    }


    /**
     * Calls user search to fetch a user and enriches it in request
     * @param request
     */
    private void enrichUser(ServiceRequest request){

        RequestInfo requestInfo = request.getRequestInfo();
        String accountId = request.getServiceWrapper().getService().getAccountId();
        String tenantId = request.getServiceWrapper().getService().getTenantId();

        UserDetailResponse userDetailResponse = searchUser(userUtils.getStateLevelTenant(tenantId),accountId,null);

        if(userDetailResponse.getUser().isEmpty())
            throw new CustomException("INVALID_ACCOUNTID","No user exist for the given accountId");

        else request.getServiceWrapper().getService().setCitizen( userDetailResponse.getUser().get(0));

    }

    /**
     * Creates the user from the given userInfo by calling user service
     * @param requestInfo
     * @param tenantId
     * @param userInfo
     * @return
     */
    private User createUser(RequestInfo requestInfo,String tenantId, User userInfo) {

        userUtils.addUserDefaultFields(userInfo.getMobileNumber(),tenantId, (digit.web.models.User) userInfo);
        StringBuilder uri = new StringBuilder(config.getUserHost())
                .append(config.getUserContextPath())
                .append(config.getUserCreateEndpoint());


        UserDetailResponse userDetailResponse = userUtils.userCall(new CreateUserRequest(requestInfo, userInfo), uri);

        return (User) userDetailResponse.getUser().get(0);

    }

    /**
     * Updates the given user by calling user service
     * @param requestInfo
     * @param user
     * @param userFromSearch
     * @return
     */
    private User updateUser(RequestInfo requestInfo,User user,User userFromSearch) {

        userFromSearch.setName(user.getName());


        StringBuilder uri = new StringBuilder(config.getUserHost())
                .append(config.getUserContextPath())
                .append(config.getUserUpdateEndpoint());


       UserDetailResponse userDetailResponse = userUtils.userCall(new CreateUserRequest(requestInfo, userFromSearch), uri);

        return (User) userDetailResponse.getUser().get(0);

    }

    /**
     * calls the user search API based on the given accountId and userName
     * @param stateLevelTenant
     * @param accountId
     * @param userName
     * @return
     */
    private UserDetailResponse searchUser(String stateLevelTenant, String accountId, String userName){

        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType(USERTYPE_CITIZEN);
        userSearchRequest.setTenantId(stateLevelTenant);

        if(StringUtils.isEmpty(accountId) && StringUtils.isEmpty(userName))
            return null;

        if(!StringUtils.isEmpty(accountId))
            userSearchRequest.setUuid(Collections.singletonList(accountId));

        if(!StringUtils.isEmpty(userName))
            userSearchRequest.setUserName(userName);

        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        return userUtils.userCall(userSearchRequest,uri);

    }








}