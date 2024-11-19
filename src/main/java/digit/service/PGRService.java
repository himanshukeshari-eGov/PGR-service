package digit.service;



import digit.config.Configuration;
import digit.config.ServiceConstants;
import digit.repository.PGRRepository;
import digit.validator.ServiceRequestValidator;
import digit.web.models.ServiceRequest;
import org.egov.common.contract.request.RequestInfo;

import digit.web.models.ServiceWrapper;
import digit.web.models.RequestSearchCriteria;

import digit.util.MDMSUtils;
import org.egov.common.contract.request.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import digit.kafka.Producer;

import java.util.*;

@org.springframework.stereotype.Service
public class PGRService {



    private EnrichmentService enrichmentService;


    private WorkflowService workflowService;

    private ServiceRequestValidator serviceRequestValidator;

    private ServiceRequestValidator validator;


    private Configuration config;

    private PGRRepository repository;

    private MDMSUtils mdmsUtils;

    private Producer producer;

    private UserService userService;



    @Autowired
    public PGRService(EnrichmentService enrichmentService,
                      ServiceRequestValidator serviceRequestValidator, ServiceRequestValidator validator, Producer producer,
                      Configuration config,  MDMSUtils mdmsUtils , UserService userService, PGRRepository repository, WorkflowService workflowService) {
        this.enrichmentService = enrichmentService;
        this.repository=repository;
        this.serviceRequestValidator = serviceRequestValidator;
        this.validator = validator;
        this.producer = producer;
        this.config = config;
        this.mdmsUtils = mdmsUtils;
        this.workflowService=workflowService;
    }


    /**
     * Creates a complaint in the system
     * @param request The service request containg the complaint information
     * @return
     */
    public ServiceRequest create(ServiceRequest request){
        Object mdmsData = mdmsUtils.fetchMdmsData(request);
        validator.validateCreate(request, mdmsData);
        enrichmentService.enrichCreateRequest(request);
        workflowService.updateWorkflowStatus(request);
        producer.push(config.getCreateTopic(),request);
        return request;
    }


    /**
     * Searches the complaints in the system based on the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params on which to search
     * @return
     */
    public List<ServiceWrapper> search(RequestInfo requestInfo, RequestSearchCriteria criteria){
        validator.validateSearch(requestInfo, criteria);

        enrichmentService.enrichSearchRequest(requestInfo, criteria);

        if(criteria.isEmpty())
            return new ArrayList<>();

        if(criteria.getMobileNumber()!=null && CollectionUtils.isEmpty(criteria.getUserIds()))
            return new ArrayList<>();

        criteria.setIsPlainSearch(false);

        List<ServiceWrapper> serviceWrappers = repository.getServiceWrappers(criteria);

        if(CollectionUtils.isEmpty(serviceWrappers))
            return new ArrayList<>();;

        List<ServiceWrapper> enrichedServiceWrappers = workflowService.enrichWorkflow(requestInfo,serviceWrappers);
        Map<Long, List<ServiceWrapper>> sortedWrappers = new TreeMap<>(Collections.reverseOrder());
        for(ServiceWrapper svc : enrichedServiceWrappers){
            if(sortedWrappers.containsKey(svc.getService().getAuditDetails().getCreatedTime())){
                sortedWrappers.get(svc.getService().getAuditDetails().getCreatedTime()).add(svc);
            }else{
                List<ServiceWrapper> serviceWrapperList = new ArrayList<>();
                serviceWrapperList.add(svc);
                sortedWrappers.put(svc.getService().getAuditDetails().getCreatedTime(), serviceWrapperList);
            }
        }
        List<ServiceWrapper> sortedServiceWrappers = new ArrayList<>();
        for(Long createdTimeDesc : sortedWrappers.keySet()){
            sortedServiceWrappers.addAll(sortedWrappers.get(createdTimeDesc));
        }
        return serviceWrappers;
    }


    /**
     * Updates the complaint (used to forward the complaint from one application status to another)
     * @param request The request containing the complaint to be updated
     * @return
     */
    public ServiceRequest update(ServiceRequest request){
        Object mdmsData = mdmsUtils.fetchMdmsData(request);
        validator.validateUpdate(request, mdmsData);
        enrichmentService.enrichUpdateRequest(request);
        workflowService.updateWorkflowStatus(request);
        producer.push(config.getUpdateTopic(),request);
        return request;
    }

    /**
     * Returns the total number of comaplaints matching the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params for which count is required
     * @return
     */
    public Integer count(RequestInfo requestInfo, RequestSearchCriteria criteria){
        criteria.setIsPlainSearch(false);
        Integer count = repository.getCount(criteria);
        return count;
    }


}