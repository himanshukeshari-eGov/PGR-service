package digit.web.controllers;


import digit.service.PGRService;
import digit.web.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.egov.common.contract.response.ResponseInfo;

import java.io.IOException;
import java.util.*;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import digit.config.ServiceConstants;

import java.util.Optional;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-11-18T11:03:15.769126038+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("")
public class RequestApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private PGRService pgrService;

    @Autowired
    public RequestApiController(ObjectMapper objectMapper, HttpServletRequest request, PGRService pgrService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.pgrService=pgrService;
    }

    @RequestMapping(value = "/request/_count", method = RequestMethod.POST)
    public ResponseEntity<CountResponse> requestCountPost(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                            @Valid @ModelAttribute RequestSearchCriteria criteria){
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            Integer count = pgrService.count(requestInfoWrapper.getRequestInfo(), criteria);
            ResponseInfo responseInfo = ResponseInfo.builder().status("true").build();
            CountResponse response = CountResponse.builder().responseInfo(responseInfo).count(count).build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        return new ResponseEntity<CountResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/request/_create", method = RequestMethod.POST)
    public ResponseEntity<ServiceResponse> requestCreatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Request schema.", required = true, schema = @Schema()) @Valid @RequestBody ServiceRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            ServiceRequest createResponse = pgrService.create(body);
            ResponseInfo responseInfo = ResponseInfo.builder().status("true").build();
            ServiceWrapper serviceWrapper = ServiceWrapper.builder().service(createResponse.getServiceWrapper().getService()).workflow(createResponse.getServiceWrapper().getWorkflow()).build();
            ServiceResponse response = ServiceResponse.builder().responseInfo(responseInfo).serviceWrappers(Collections.singletonList(serviceWrapper)).build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<ServiceResponse>(HttpStatus.NOT_IMPLEMENTED);

    }

    @RequestMapping(value = "/request/_search", method = RequestMethod.POST)
    public ResponseEntity<ServiceResponse> requestSearchPost(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                             @Valid @ModelAttribute RequestSearchCriteria criteria){
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            List<ServiceWrapper> serviceWrappers = pgrService.search(requestInfoWrapper.getRequestInfo(), criteria);
            ResponseInfo responseInfo = ResponseInfo.builder().status("true").build();
            ServiceResponse response = ServiceResponse.builder().responseInfo(responseInfo).serviceWrappers(serviceWrappers).build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        return new ResponseEntity<ServiceResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/request/_update", method = RequestMethod.POST)
    public ResponseEntity<ServiceResponse> requestUpdatePost(@Parameter(in = ParameterIn.DEFAULT, description = "Request schema.", required = true, schema = @Schema()) @Valid @RequestBody ServiceRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            ServiceRequest serviceRequest = pgrService.update(body);
            ServiceWrapper serviceWrapper = ServiceWrapper.builder().service(serviceRequest.getServiceWrapper().getService()).workflow(serviceRequest.getServiceWrapper().getWorkflow()).build();
            ResponseInfo responseInfo = ResponseInfo.builder().status("true").build();
            ServiceResponse response = ServiceResponse.builder().responseInfo(responseInfo).serviceWrappers(Collections.singletonList(serviceWrapper)).build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        return new ResponseEntity<ServiceResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

}
