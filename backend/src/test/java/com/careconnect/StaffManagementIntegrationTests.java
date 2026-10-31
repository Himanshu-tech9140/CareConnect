package com.careconnect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:careconnect-tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "careconnect.demo-seed=false",
        "careconnect.bootstrap-admin.username=test-admin",
        "careconnect.bootstrap-admin.password=TestAdmin123!",
        "careconnect.bootstrap-admin.display-name=Test Administrator"
})
class StaffManagementIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void staffEndpointRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/staff"))
                                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateDoctorAndNurseAccounts() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(post("/api/admin/staff")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(staffJson("doctor.integration", "Dr. Integration", "PROVIDER", "Cardiology")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("doctor.integration"))
                .andExpect(jsonPath("$.role").value("PROVIDER"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(post("/api/admin/staff")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(staffJson("nurse.integration", "Nurse Integration", "NURSE", "Ward A")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("nurse.integration"))
                .andExpect(jsonPath("$.role").value("NURSE"))
                .andExpect(jsonPath("$.subtitle").value("Nursing · Ward A"));

        mockMvc.perform(get("/api/admin/staff").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username", hasItem("doctor.integration")))
                .andExpect(jsonPath("$[*].username", hasItem("nurse.integration")));
    }

    @Test
    void duplicateUsernameIsRejected() throws Exception {
        String token = loginAsAdmin();
        String payload = staffJson("duplicate.integration", "Duplicate User", "PROVIDER", "Primary Care");

        mockMvc.perform(post("/api/admin/staff")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/staff")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("That username is already registered"));
    }

    private String loginAsAdmin() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test-admin\",\"password\":\"TestAdmin123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode response = objectMapper.readTree(body);
        return response.get("token").asText();
    }

    private String staffJson(String username, String displayName, String role, String department) {
        return "{"
                + "\"username\":\"" + username + "\","
                + "\"password\":\"Staff123!\","
                + "\"displayName\":\"" + displayName + "\","
                + "\"role\":\"" + role + "\","
                + "\"specialization\":\"General Medicine\","
                + "\"qualification\":\"MD\","
                + "\"licenseNumber\":\"CA-TEST-001\","
                + "\"department\":\"" + department + "\""
                + "}";
    }
}
