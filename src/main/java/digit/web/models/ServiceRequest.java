package digit.web.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Request object to fetch the report data
 */
@ApiModel(description = "Request object to fetch the report data")
@Validated

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceRequest   {

    @NotNull
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo = null;

    @NotNull
    @JsonProperty("ServiceWrapper")
    private ServiceWrapper ServiceWrapper;

//    @Valid
//    @NonNull
//    @JsonProperty("service")
//    private Service service = null;
//
//    @Valid
//    @JsonProperty("workflow")
//    private Workflow workflow = null;


}
