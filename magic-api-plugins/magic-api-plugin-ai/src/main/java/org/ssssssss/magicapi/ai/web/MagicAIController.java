package org.ssssssss.magicapi.ai.web;

import org.springframework.web.bind.annotation.*;
import org.ssssssss.magicapi.ai.model.AIChatRequest;
import org.ssssssss.magicapi.ai.model.AIChatResponse;
import org.ssssssss.magicapi.ai.model.AIGeneratedApi;
import org.ssssssss.magicapi.ai.model.AITestRequest;
import org.ssssssss.magicapi.ai.service.MagicAIService;
import org.ssssssss.magicapi.core.annotation.Valid;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.interceptor.Authorization;
import org.ssssssss.magicapi.core.model.ApiInfo;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 对话、在线测试、一键发布
 */
public class MagicAIController extends MagicController implements MagicExceptionHandler {

	private final MagicAIService magicAIService;

	public MagicAIController(MagicConfiguration configuration, MagicAIService magicAIService) {
		super(configuration);
		this.magicAIService = magicAIService;
	}

	@GetMapping("/ai/status")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<Map<String, Object>> status() {
		return new JsonBean<>(magicAIService.status());
	}

	@PostMapping("/ai/chat")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<AIChatResponse> chat(@RequestBody AIChatRequest request) {
		try {
			return new JsonBean<>(magicAIService.chat(request));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return new JsonBean<>(0, e.getMessage());
		} catch (Exception e) {
			return new JsonBean<>(0, "AI 对话失败: " + e.getMessage());
		}
	}

	@PostMapping("/ai/apply")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<ApiInfo> apply(@RequestBody Map<String, Object> body) {
		try {
			AIGeneratedApi definition = JSONCompat.toGenerated(body.get("definition"));
			String apiId = body.get("apiId") == null ? null : String.valueOf(body.get("apiId"));
			String groupId = body.get("groupId") == null ? null : String.valueOf(body.get("groupId"));
			return new JsonBean<>(magicAIService.saveGenerated(definition, apiId, groupId));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return new JsonBean<>(0, e.getMessage());
		} catch (Exception e) {
			return new JsonBean<>(0, "保存接口失败: " + e.getMessage());
		}
	}

	@PostMapping("/ai/test")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<Object> test(@RequestBody AITestRequest request, MagicHttpServletRequest httpRequest) {
		try {
			Object result = magicAIService.testApi(request, httpRequest, configuration, configuration.getMagicAPIService());
			return new JsonBean<>(result);
		} catch (IllegalArgumentException | IllegalStateException e) {
			return new JsonBean<>(0, e.getMessage());
		} catch (Exception e) {
			return new JsonBean<>(0, "在线测试失败: " + e.getMessage());
		}
	}

	@PostMapping("/ai/publish")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.LOCK)
	public JsonBean<Map<String, Object>> publish(@RequestBody Map<String, String> body) {
		try {
			String apiId = body == null ? null : body.get("apiId");
			boolean ok = magicAIService.publish(apiId);
			Map<String, Object> data = new HashMap<>();
			data.put("success", ok);
			data.put("apiId", apiId);
			data.put("locked", ok);
			data.put("message", ok ? "发布成功（已锁定，不可再编辑，解锁后可继续修改）" : "发布失败");
			return new JsonBean<>(data);
		} catch (IllegalArgumentException | IllegalStateException e) {
			return new JsonBean<>(0, e.getMessage());
		} catch (Exception e) {
			return new JsonBean<>(0, "发布失败: " + e.getMessage());
		}
	}

	/**
	 * 避免控制器直接依赖 fastjson API 细节
	 */
	static final class JSONCompat {
		private JSONCompat() {
		}

		@SuppressWarnings("unchecked")
		static AIGeneratedApi toGenerated(Object definition) {
			if (definition == null) {
				throw new IllegalArgumentException("definition 不能为空");
			}
			if (definition instanceof AIGeneratedApi) {
				return (AIGeneratedApi) definition;
			}
			return com.alibaba.fastjson.JSON.parseObject(
					com.alibaba.fastjson.JSON.toJSONString(definition),
					AIGeneratedApi.class
			);
		}
	}
}
