package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * BPA application object to capture the details of land, land owners, and address of the land.
 */
@Schema(description = "BPA application object to capture the details of land, land owners, and address of the land.")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-11-18T11:03:15.769126038+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Workflow   {

    @JsonProperty("action")
    private String action = null;

    @JsonProperty("assignes")
    @Valid
    private List<String> assignes = null;


    @JsonProperty("comments")
    private String comments = null;

    @JsonProperty("verificationDocuments")
    @Valid
    private List<Document> verificationDocuments = null;


    public Workflow addAssignesItem(String assignesItem) {
        if (this.assignes == null) {
            this.assignes = new ArrayList<>();
        }
        this.assignes.add(assignesItem);
        return this;
    }

    public Workflow addVarificationDocumentsItem(Document verificationDocumentsItem) {
        if (this.verificationDocuments == null) {
            this.verificationDocuments = new ArrayList<>();
        }
        this.verificationDocuments.add(verificationDocumentsItem);
        return this;
    }

}
