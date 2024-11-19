package digit.service;

import digit.config.Configuration;
import digit.util.IdgenUtil;
import digit.util.PGRUtils;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;
import org.springframework.util.StringUtils;

import static digit.config.ServiceConstants.USERTYPE_CITIZEN;

@org.springframework.stereotype.Service
public class EnrichmentService {


    private PGRUtils utils;

    private IdgenUtil idgenUtil;

    private Configuration config;

    private UserService userService;



    @Autowired
    public EnrichmentService(PGRUtils utils, IdgenUtil idgenUtil, Configuration config, UserService userService) {
        this.utils = utils;
        this.idgenUtil = idgenUtil;
        this.config = config;
        this.userService=userService;

    }


    /**
     * Enriches the create request with auditDetails. uuids and custom ids from idGen service
     * @param serviceRequest The create request
     */
    public void enrichCreateRequest(ServiceRequest serviceRequest){

        RequestInfo requestInfo = serviceRequest.getRequestInfo();
        Service service = serviceRequest.getServiceWrapper().getService();
        Workflow workflow = serviceRequest.getServiceWrapper().getWorkflow();
        String tenantId = service.getTenantId();
        if(requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_CITIZEN))
            serviceRequest.getServiceWrapper().getService().setAccountId(requestInfo.getUserInfo().getUuid());
        userService.callUserService(serviceRequest);
        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(), service,true);

        service.setAuditDetails(auditDetails);
        service.setId(UUID.randomUUID().toString());
        service.getAddress().setId(UUID.randomUUID().toString());
        service.getAddress().setTenantId(tenantId);

        if(workflow.getVerificationDocuments()!=null){
            workflow.getVerificationDocuments().forEach(document -> {
                document.setId(UUID.randomUUID().toString());
            });
        }

        if(StringUtils.isEmpty(service.getAccountId()))
            service.setAccountId(service.getCitizen().getUuid());

        List<String> customIds = idgenUtil.getIdList(requestInfo,tenantId,config.getServiceRequestIdGenName(),config.getServiceRequestIdGenFormat(),1);

        service.setServiceRequestId(customIds.get(0));


    }


    /**
     * Enriches the update request (updates the lastModifiedTime in auditDetails0
     * @param serviceRequest The update request
     */
    public void enrichUpdateRequest(ServiceRequest serviceRequest){

        RequestInfo requestInfo = serviceRequest.getRequestInfo();
        Service service = serviceRequest.getServiceWrapper().getService();
        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(), service,false);

        service.setAuditDetails(auditDetails);
        userService.callUserService(serviceRequest);

    }

    /**
     * Enriches the search criteria in case of default search and enriches the userIds from mobileNumber in case of seach based on mobileNumber.
     * Also sets the default limit and offset if none is provided
     * @param requestInfo
     * @param criteria
     */
    public void enrichSearchRequest(RequestInfo requestInfo, RequestSearchCriteria criteria){


        if(criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")){
            String citizenMobileNumber = requestInfo.getUserInfo().getUserName();
            criteria.setMobileNumber(citizenMobileNumber);
        }
        criteria.setAccountId(requestInfo.getUserInfo().getUuid());

        String tenantId = (criteria.getTenantId()!=null) ? criteria.getTenantId() : requestInfo.getUserInfo().getTenantId();



        if(criteria.getLimit()==null)
            criteria.setLimit(100);

        if(criteria.getOffset()==null)
            criteria.setOffset(0);

        if(criteria.getLimit()!=null && criteria.getLimit() > 100)
            criteria.setLimit(100);

    }




}