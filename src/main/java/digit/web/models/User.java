package digit.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.Role;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends org.egov.common.contract.request.User {

    private Long id;
    private String userName;
    private String name;
    private String type;
    private String mobileNumber;
    private String emailId;
    private List<Role> roles;
    private String tenantId;
    private String uuid;
    private Boolean active;



}