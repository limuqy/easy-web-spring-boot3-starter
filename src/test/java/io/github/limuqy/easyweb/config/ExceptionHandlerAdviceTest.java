package io.github.limuqy.easyweb.config;

import io.github.limuqy.easyweb.core.exception.BusinessException;
import io.github.limuqy.easyweb.mybitis.base.RestResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExceptionHandlerAdviceTest.TestController.class)
@Import({ExceptionHandlerAdvice.class, ExceptionHandlerAdviceTest.TestController.class})
class ExceptionHandlerAdviceTest {

    @SpringBootConfiguration
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void businessException_returnsFailResponse() throws Exception {
        mockMvc.perform(get("/test/business-error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("业务异常"));
    }

    @Test
    void runtimeException_returns500() throws Exception {
        mockMvc.perform(get("/test/runtime-error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("系统发生未知异常")));
    }

    @Test
    void normalResponse_returnsOk() throws Exception {
        mockMvc.perform(get("/test/ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("success"));
    }

    @Test
    void okResponse_hasSuccessFormat() throws Exception {
        mockMvc.perform(get("/test/ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNumber())
                .andExpect(jsonPath("$.data").exists());
    }

    @RestController
    static class TestController {

        @GetMapping("/test/business-error")
        public RestResponse<?> businessError() {
            throw new BusinessException("业务异常");
        }

        @GetMapping("/test/runtime-error")
        public RestResponse<?> runtimeError() {
            throw new RuntimeException("运行时异常");
        }

        @GetMapping("/test/ok")
        public RestResponse<?> ok() {
            return RestResponse.ok("success");
        }
    }
}
