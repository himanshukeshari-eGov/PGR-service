package digit.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.config.ServiceConstants;
import digit.repository.ServiceRequestRepository;
import digit.web.models.ServiceRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
public class MDMSUtils {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Configuration config;

    private ServiceRequestRepository serviceRequestRepository;

    private ServiceConstants serviceConstants;

    @Autowired
    public MDMSUtils(Configuration config, ServiceRequestRepository serviceRequestRepository) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
    }


    public Object fetchMdmsData(ServiceRequest request){
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = request.getServiceWrapper().getService().getTenantId();
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo,tenantId);
        Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
        return result;
    }

    public MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo,String tenantId){
        List<ModuleDetail> pgrModuleRequest = getPGRModuleRequest();

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.addAll(pgrModuleRequest);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        return mdmsCriteriaReq;
    }

    private List<ModuleDetail> getPGRModuleRequest() {

        // master details for TL module
        List<MasterDetail> pgrMasterDetails = new ArrayList<>();

        // filter to only get code field from master data
        final String filterCode = "$.[?(@.active==true)]";

        pgrMasterDetails.add(MasterDetail.builder().name(ServiceConstants.MDMS_SERVICEDEF).filter(filterCode).build());

        ModuleDetail pgrModuleDtls = ModuleDetail.builder().masterDetails(pgrMasterDetails)
                .moduleName(ServiceConstants.MDMS_MODULE_NAME).build();


        return Collections.singletonList(pgrModuleDtls);

    }




//    public Map<String, Map<String, JSONArray>> fetchMdmsData(RequestInfo requestInfo, String tenantId, String moduleName,
//                                                                                List<String> masterNameList) {
//        StringBuilder uri = new StringBuilder();
//        uri.append(configs.getMdmsHost()).append(configs.getMdmsEndPoint());
//        MdmsCriteriaReq mdmsCriteriaReq = getMdmsRequest(requestInfo, tenantId, moduleName, masterNameList);
//        Object response = new HashMap<>();
//        Integer rate = 0;
//        MdmsResponse mdmsResponse = new MdmsResponse();
//        try {
//            response = restTemplate.postForObject(uri.toString(), mdmsCriteriaReq, Map.class);
//            mdmsResponse = mapper.convertValue(response, MdmsResponse.class);
//        }catch(Exception e) {
//            log.error(ERROR_WHILE_FETCHING_FROM_MDMS,e);
//        }
//
//        return mdmsResponse.getMdmsRes();
//        //log.info(ulbToCategoryListMap.toString());
//    }

//    private MdmsCriteriaReq getMdmsRequest(RequestInfo requestInfo, String tenantId,
//                                           String moduleName, List<String> masterNameList) {
//        List<MasterDetail> masterDetailList = new ArrayList<>();
//        for(String masterName: masterNameList) {
//            MasterDetail masterDetail = new MasterDetail();
//            masterDetail.setName(masterName);
//            masterDetailList.add(masterDetail);
//        }
//
//        ModuleDetail moduleDetail = new ModuleDetail();
//        moduleDetail.setMasterDetails(masterDetailList);
//        moduleDetail.setModuleName(moduleName);
//        List<ModuleDetail> moduleDetailList = new ArrayList<>();
//        moduleDetailList.add(moduleDetail);
//
//        MdmsCriteria mdmsCriteria = new MdmsCriteria();
//        mdmsCriteria.setTenantId(tenantId.split("\\.")[0]);
//        mdmsCriteria.setModuleDetails(moduleDetailList);
//
//        MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
//        mdmsCriteriaReq.setMdmsCriteria(mdmsCriteria);
//        mdmsCriteriaReq.setRequestInfo(requestInfo);
//
//        return mdmsCriteriaReq;
//    }
    public StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
    }
}