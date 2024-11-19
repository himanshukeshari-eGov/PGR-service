package digit.validator;


import com.jayway.jsonpath.JsonPath;
import digit.config.Configuration;
import digit.repository.PGRRepository;
import digit.web.models.RequestSearchCriteria;
import digit.web.models.ServiceRequest;
import digit.web.models.ServiceWrapper;
import org.egov.common.contract.request.RequestInfo;
import digit.config.ServiceConstants;
import digit.util.HRMSUtil;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;



@Component
public class ServiceRequestValidator {


    private Configuration config;

    private PGRRepository repository;

    private HRMSUtil hrmsUtil;

    private ServiceConstants serviceConstants;



    @Autowired
    public ServiceRequestValidator(Configuration config,  HRMSUtil hrmsUtil, PGRRepository repository) {
        this.config = config;

        this.hrmsUtil = hrmsUtil;
        this.repository=repository;
    }


    /**
     * Validates the create request
     * @param request Request for creating the complaint
     * @param mdmsData The master data for pgr
     */
    public void validateCreate(ServiceRequest request, Object mdmsData){
        Map<String,String> errorMap = new HashMap<>();
        validateSource(request.getServiceWrapper().getService().getSource());
        validateMDMS(request, mdmsData);
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    /**
     * Validates if the source is in the given list configures in application properties
     * @param source
     */
    private void validateSource(String source){

        List<String> allowedSourceStr = Arrays.asList(config.getAllowedSource().split(","));

        if(!allowedSourceStr.contains(source))
            throw new CustomException("INVALID_SOURCE","The source: "+source+" is not valid");

    }


    /**
     * Validates if the update request is valid
     * @param request The request to update complaint
     * @param mdmsData The master data for pgr
     */
    public void validateUpdate(ServiceRequest request, Object mdmsData){

        String id = request.getServiceWrapper().getService().getId();
        String tenantId = request.getServiceWrapper().getService().getTenantId();
        validateSource(request.getServiceWrapper().getService().getSource());
        validateMDMS(request, mdmsData);
        validateDepartment(request, mdmsData);
        RequestSearchCriteria criteria = RequestSearchCriteria.builder().ids(Collections.singleton(id)).tenantId(tenantId).build();
        criteria.setIsPlainSearch(false);
        List<ServiceWrapper> serviceWrappers = repository.getServiceWrappers(criteria);

        if(CollectionUtils.isEmpty(serviceWrappers))
            throw new CustomException("INVALID_UPDATE","The record that you are trying to update does not exists");

    }


    private void validateMDMS(ServiceRequest request, Object mdmsData){

        String serviceCode = request.getServiceWrapper().getService().getServiceCode();
        String jsonPath = serviceConstants.MDMS_SERVICEDEF_SEARCH.replace("{SERVICEDEF}",serviceCode);

        List<Object> res = null;

        try{
            res = JsonPath.read(mdmsData,jsonPath);
        }
        catch (Exception e){
            throw new CustomException("JSONPATH_ERROR","Failed to parse mdms response");
        }

        if(CollectionUtils.isEmpty(res))
            throw new CustomException("INVALID_SERVICECODE","The service code: "+serviceCode+" is not present in MDMS");


    }


    /**
     *
     * @param request
     * @param mdmsData
     */
    private void validateDepartment(ServiceRequest request, Object mdmsData){

        String serviceCode = request.getServiceWrapper().getService().getServiceCode();
        List<String> assignes = request.getServiceWrapper().getWorkflow().getAssignes();

        if(CollectionUtils.isEmpty(assignes))
            return;

        List<String> departments = hrmsUtil.getDepartment(assignes, request.getRequestInfo());

        String jsonPath = ServiceConstants.MDMS_DEPARTMENT_SEARCH.replace("{SERVICEDEF}",serviceCode);

        List<String> res = null;
        String departmentFromMDMS;

        try{
            res = JsonPath.read(mdmsData,jsonPath);
        }
        catch (Exception e){
            throw new CustomException("JSONPATH_ERROR","Failed to parse mdms response for department");
        }

        if(CollectionUtils.isEmpty(res))
            throw new CustomException("PARSING_ERROR","Failed to fetch department from mdms data for serviceCode: "+serviceCode);
        else departmentFromMDMS = res.get(0);

        Map<String, String> errorMap = new HashMap<>();

        if(!departments.contains(departmentFromMDMS))
            errorMap.put("INVALID_ASSIGNMENT","The application cannot be assigned to employee of department: "+departments.toString());


        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);

    }


    public void validateSearch(RequestInfo requestInfo, RequestSearchCriteria criteria){

        if( (criteria.getMobileNumber()!=null
                || criteria.getServiceRequestId()!=null || criteria.getIds()!=null
                || criteria.getServiceCode()!=null )
                && criteria.getTenantId()==null)
            throw new CustomException("INVALID_SEARCH","TenantId is mandatory search param");

        validateSearchParam(requestInfo, criteria);

    }




    private void validateSearchParam(RequestInfo requestInfo, RequestSearchCriteria criteria){

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase("EMPLOYEE" ) && criteria.isEmpty())
            throw new CustomException("INVALID_SEARCH","Search without params is not allowed");


        String allowedParamStr = null;

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN" ))
            allowedParamStr = config.getAllowedCitizenSearchParameters();
        else if(requestInfo.getUserInfo().getType().equalsIgnoreCase("EMPLOYEE" ) || requestInfo.getUserInfo().getType().equalsIgnoreCase("SYSTEM") )
            allowedParamStr = config.getAllowedEmployeeSearchParameters();
        else throw new CustomException("INVALID SEARCH","The userType: "+requestInfo.getUserInfo().getType()+
                    " does not have any search config");

        List<String> allowedParams = Arrays.asList(allowedParamStr.split(","));

        if(criteria.getServiceCode()!=null && !allowedParams.contains("serviceCode"))
            throw new CustomException("INVALID SEARCH","Search on serviceCode is not allowed");

        if(criteria.getServiceRequestId()!=null && !allowedParams.contains("serviceRequestId"))
            throw new CustomException("INVALID SEARCH","Search on serviceRequestId is not allowed");

        if(criteria.getApplicationStatus()!=null && !allowedParams.contains("applicationStatus"))
            throw new CustomException("INVALID SEARCH","Search on applicationStatus is not allowed");

        if(criteria.getMobileNumber()!=null && !allowedParams.contains("mobileNumber"))
            throw new CustomException("INVALID SEARCH","Search on mobileNumber is not allowed");

        if(criteria.getIds()!=null && !allowedParams.contains("ids"))
            throw new CustomException("INVALID SEARCH","Search on ids is not allowed");

    }


}
