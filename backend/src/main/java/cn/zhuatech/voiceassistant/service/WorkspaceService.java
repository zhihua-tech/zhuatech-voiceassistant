/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.voiceassistant.service;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Service
public class WorkspaceService {
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public RunResult run(RunRequest request) {
        List<String> warnings = new ArrayList<>();
        if (!request.humanReview()) warnings.add("未启用人工复核，结果不能进入正式业务流程");
        if (request.confidenceFloor() < 70) warnings.add("置信度阈值低于建议值 70，需扩大人工抽检范围");
        if (request.context() == null || request.context().isBlank()) warnings.add("缺少补充上下文，本次仅按基础规则处理");

        List<Insight> insights = List.of(
            new Insight("事实", "识别主要意图为物流进度查询", 94),
            new Insight("关注", "知识答案引用 OMS 物流节点", 82),
            new Insight("边界", "未检测到支付或敏感身份信息", 76)
        );
        List<Action> actions = List.of(
            new Action("播报预计送达区间", "业务负责人", "今天"),
            new Action("向用户发送物流查询链接", "审核人员", "本周"),
            new Action("低置信度时转人工坐席", "系统管理员", "复核后")
        );
        Map<String, Object> providerPayload = new LinkedHashMap<>();
        providerPayload.put("subject", request.subject());
        providerPayload.put("scenario", request.scenario());
        providerPayload.put("context", request.context());
        providerPayload.put("confidenceFloor", request.confidenceFloor());
        providerPayload.put("provider", "deepseek-compatible");
        providerPayload.put("model", "deepseek-chat");

        String status = request.humanReview() ? "REVIEW_READY" : "HUMAN_REVIEW_REQUIRED";
        return new RunResult(status, "LOW", "用户希望查询订单到货时间，已完成身份核验并命中物流知识。回答需播报预计送达区间，不承诺精确时间。", insights, actions,
            List.copyOf(warnings), providerPayload, "LOCAL_DEMO_PIPELINE", OffsetDateTime.now());
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public record RunRequest(
        @NotBlank String subject,
        @NotBlank String scenario,
        @Min(0) @Max(100) int confidenceFloor,
        boolean humanReview,
        @Size(max = 1200) String context
    ) {}

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public record Insight(String type, String content, int confidence) {}
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public record Action(String task, String ownerRole, String dueHint) {}
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public record RunResult(String status, String riskLevel, String summary, List<Insight> insights,
                            List<Action> actions, List<String> warnings, Map<String, Object> providerPayload,
                            String executionMode, OffsetDateTime generatedAt) {}
}
