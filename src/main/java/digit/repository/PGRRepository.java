package digit.repository;

import digit.repository.rowmapper.PGRQueryBuilder;
import digit.repository.rowmapper.PGRRowMapper;
import digit.util.PGRUtils;
import digit.web.models.RequestSearchCriteria;
import digit.web.models.Service;
import digit.web.models.ServiceWrapper;
import digit.web.models.Workflow;
import org.egov.tracer.model.CustomException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import digit.config.ServiceConstants;
import org.springframework.stereotype.Component;

@Component
public class PGRRepository {


    private PGRQueryBuilder queryBuilder;

    private PGRRowMapper rowMapper;

    private JdbcTemplate jdbcTemplate;

    private PGRUtils utils;


    @Autowired
    public PGRRepository(PGRQueryBuilder queryBuilder, PGRRowMapper rowMapper, JdbcTemplate jdbcTemplate, PGRUtils utils) {
        this.queryBuilder = queryBuilder;
        this.rowMapper = rowMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.utils = utils;
    }




    /**
     * searches services based on search criteria and then wraps it into serviceWrappers
     * @param criteria
     * @return
     */
    public List<ServiceWrapper> getServiceWrappers(RequestSearchCriteria criteria){
        List<Service> services = getServices(criteria);
        List<String> serviceRequestids = services.stream().map(Service::getServiceRequestId).collect(Collectors.toList());
        Map<String, Workflow> idToWorkflowMap = new HashMap<>();
        List<ServiceWrapper> serviceWrappers = new ArrayList<>();

        for(Service service : services){
            ServiceWrapper serviceWrapper = ServiceWrapper.builder().service(service).workflow(idToWorkflowMap.get(service.getServiceRequestId())).build();
            serviceWrappers.add(serviceWrapper);
        }
        return serviceWrappers;
    }

    /**
     * searches services based on search criteria
     * @param criteria
     * @return
     */
    public List<Service> getServices(RequestSearchCriteria criteria) {

        String tenantId = criteria.getTenantId();
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getPGRSearchQuery(criteria, preparedStmtList);
        List<Service> services =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return services;
    }

    /**
     * Returns the count based on the search criteria
     * @param criteria
     * @return
     */
    public Integer getCount(RequestSearchCriteria criteria) {

        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getCountQuery(criteria, preparedStmtList);
        Integer count =  jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
        return count;
    }


    public Map<String, Integer> fetchDynamicData(String tenantId) {
        List<Object> preparedStmtListCompalintsResolved = new ArrayList<>();
        String query = queryBuilder.getResolvedComplaints(tenantId,preparedStmtListCompalintsResolved );
        int complaintsResolved = jdbcTemplate.queryForObject(query,preparedStmtListCompalintsResolved.toArray(),Integer.class);

        List<Object> preparedStmtListAverageResolutionTime = new ArrayList<>();
        query = queryBuilder.getAverageResolutionTime(tenantId, preparedStmtListAverageResolutionTime);
        Integer averageResolutionTime = jdbcTemplate.queryForObject(
                query,
                preparedStmtListAverageResolutionTime.toArray(),
                Integer.class
        );

        int averageResolutionTimeValue = (averageResolutionTime != null) ? averageResolutionTime : 0;
        Map<String, Integer> dynamicData = new HashMap<String,Integer>();
        dynamicData.put(ServiceConstants.COMPLAINTS_RESOLVED, complaintsResolved);
        dynamicData.put(ServiceConstants.AVERAGE_RESOLUTION_TIME, averageResolutionTimeValue);

        return dynamicData;
    }



}

