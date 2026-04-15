package org.ssssssss.magicapi.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Skill 提示词加载器：根据用户消息关键词匹配，动态加载对应的 Skill 提示词
 */
public class SkillPromptLoader {

    private static final Logger logger = LoggerFactory.getLogger(SkillPromptLoader.class);

    private static final String PROMPT_DIR = "prompt/";
    private static final int MAX_SKILLS = 3;

    /** 缓存：文件名 → 文件内容 */
    private static final Map<String, String> PROMPT_CACHE = new LinkedHashMap<>();

    /** Skill 关键词映射：文件名 → 关键词列表 */
    private static final Map<String, List<String>> SKILL_KEYWORDS = new LinkedHashMap<>();

    static {
        // 初始化关键词映射
        SKILL_KEYWORDS.put("skill-db-sql", Arrays.asList(
                "查询", "select", "查找", "列表", "分页", "page", "新增", "insert", "添加",
                "修改", "update", "更新", "删除", "delete", "selectOne", "单条", "count",
                "selectInt", "selectValue", "数据库", "sql", "SQL"));

        SKILL_KEYWORDS.put("skill-db-table", Arrays.asList(
                "table", "表", "eq", "ne", "gt", "lt", "like", "where", "orderBy",
                "groupBy", "primary", "save", "batchInsert", "批量插入", "exists",
                "db.table", "链式", "fluent"));

        SKILL_KEYWORDS.put("skill-dynamic-sql", Arrays.asList(
                "动态", "dynamic", "mybatis", "<if", "<where", "<set", "<foreach",
                "条件查询", "模糊", "搜索", "条件", "动态SQL", "动态sql"));

        SKILL_KEYWORDS.put("skill-transaction-cache", Arrays.asList(
                "事务", "transaction", "commit", "rollback", "缓存", "cache",
                "多数据源", "slave", "从库", "数据源"));

        SKILL_KEYWORDS.put("skill-request-response", Arrays.asList(
                "请求", "参数", "header", "body", "path", "cookie", "getFile",
                "上传", "文件", "response", "download", "下载", "exit", "验证",
                "校验", "接口", "CRUD", "crud"));

        SKILL_KEYWORDS.put("skill-http-client", Arrays.asList(
                "http", "调用", "请求外部", "connect", "第三方", "API", "api",
                "远程", "外部接口", "对接", "http请求", "HTTP请求"));

        SKILL_KEYWORDS.put("skill-collection-ops", Arrays.asList(
                "集合", "数组", "map", "filter", "group", "join", "sort", "distinct",
                "reduce", "列表操作", "转换", "聚合", "sum", "avg", "tree", "树",
                "分组", "排序", "去重", "遍历", "toMap"));

        SKILL_KEYWORDS.put("skill-linq", Arrays.asList(
                "linq", "LINQ", "select.*from", "group by", "order by", "left join",
                "关联查询", "内存查询", "集合查询"));

        SKILL_KEYWORDS.put("skill-java-interop", Arrays.asList(
                "java", "Java", "import", "Date", "Spring", "Bean", "bean",
                "类型转换", "::int", "::date", "asString", "asInt", "new ",
                "调用Java", "调用java", "自定义函数"));

        SKILL_KEYWORDS.put("skill-advanced", Arrays.asList(
                "异步", "async", "lambda", "箭头函数", "可选链", "?.",
                "展开", "...", "log", "日志", "env", "环境变量",
                "try", "catch", "redis", "Redis", "range"));

        // 加载所有提示词文件
        loadAllPrompts();
    }

    private static void loadAllPrompts() {
        // 加载 base prompt
        loadPromptFile("base-prompt");

        // 加载所有 skill 文件
        for (String skillName : SKILL_KEYWORDS.keySet()) {
            loadPromptFile(skillName);
        }
    }

    private static void loadPromptFile(String name) {
        String path = PROMPT_DIR + name + ".txt";
        try (InputStream is = SkillPromptLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                logger.warn("提示词文件未找到: {}", path);
                return;
            }
            String content = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            PROMPT_CACHE.put(name, content);
            logger.debug("已加载提示词文件: {}", name);
        } catch (Exception e) {
            logger.error("加载提示词文件失败: {}", path, e);
        }
    }

    /**
     * 根据用户消息构建完整的系统提示词
     *
     * @param userMessage 用户消息
     * @return 组装后的系统提示词（base + 匹配的 skills）
     */
    public static String buildPrompt(String userMessage) {
        StringBuilder prompt = new StringBuilder();

        // 始终加载 base prompt
        String basePrompt = PROMPT_CACHE.get("base-prompt");
        if (basePrompt != null) {
            prompt.append(basePrompt);
        }

        // 匹配 skills
        List<String> matchedSkills = matchSkills(userMessage);

        for (String skill : matchedSkills) {
            String skillContent = PROMPT_CACHE.get(skill);
            if (skillContent != null) {
                prompt.append("\n\n").append(skillContent);
            }
        }

        return prompt.toString();
    }

    /**
     * 根据用户消息匹配最相关的 skill（最多 MAX_SKILLS 个）
     */
    static List<String> matchSkills(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            // 默认加载最常用的 skill
            return Arrays.asList("skill-db-sql", "skill-request-response");
        }

        String msgLower = userMessage.toLowerCase();

        // 计算每个 skill 的关键词命中数
        Map<String, Integer> hitCounts = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : SKILL_KEYWORDS.entrySet()) {
            int count = 0;
            for (String keyword : entry.getValue()) {
                if (msgLower.contains(keyword.toLowerCase())) {
                    count++;
                }
            }
            if (count > 0) {
                hitCounts.put(entry.getKey(), count);
            }
        }

        if (hitCounts.isEmpty()) {
            // 无匹配时，加载默认 skill
            return Arrays.asList("skill-db-sql", "skill-request-response");
        }

        // 按命中数降序排列，取 top N
        return hitCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(MAX_SKILLS)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
