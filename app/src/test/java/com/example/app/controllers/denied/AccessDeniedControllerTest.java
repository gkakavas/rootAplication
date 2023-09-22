package com.example.app.controllers.denied;

import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.AccessDeniedController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.web.servlet.function.RequestPredicates.contentType;


@WebMvcTest
@ContextConfiguration(classes = {AccessDeniedController.class, TestSecurityConfig.class})
public class AccessDeniedControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return a specific access denied HTML page")
    void shouldReturnASpecificAccessDeniedHtmlPage() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/access-denied")
                        .header("Authorization","testToken")
                        )
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }
}
