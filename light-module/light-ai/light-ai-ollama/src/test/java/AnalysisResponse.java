import io.swagger.v3.oas.annotations.media.Schema;

public class AnalysisResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "是否异常")
    private boolean abnormal;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "预计天数")
    private int estimatedDays;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "运维建议")
    private String recommend;
}
