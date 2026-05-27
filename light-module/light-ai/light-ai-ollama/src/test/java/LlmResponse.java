import io.swagger.v3.oas.annotations.media.Schema;

public class LlmResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "回复内容")
    private String content;
}
