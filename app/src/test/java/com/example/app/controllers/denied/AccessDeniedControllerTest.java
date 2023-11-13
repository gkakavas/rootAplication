package com.example.app.controllers.denied;

import com.example.app.config.TestSecurityConfig;
import com.example.app.controllers.AccessDeniedController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("unit")
@WebMvcTest
@ContextConfiguration(classes = {AccessDeniedController.class, TestSecurityConfig.class})
public class AccessDeniedControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return a specific access denied HTML page")
    void shouldReturnASpecificAccessDeniedHtmlPage() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }
}
