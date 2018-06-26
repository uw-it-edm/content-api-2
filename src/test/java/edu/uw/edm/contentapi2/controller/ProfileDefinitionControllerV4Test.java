package edu.uw.edm.contentapi2.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.ProfileDefinitionService;
import edu.uw.edm.contentapi2.service.model.ProfileDefinitionV4;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProfileDefinitionControllerV4Test {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileDefinitionService profileDefinitionService;
    @MockBean
    FieldMapper fieldMapper;

    @Test
    public void getProfileDefinitionV4Test() throws Exception {
        ProfileDefinitionV4 profileDefinition = ProfileDefinitionV4.builder()
                .profile("testProfile")
                .metadata(new HashMap<>())
                .build();
        when(profileDefinitionService.getProfileDefinition(any(String.class), any(User.class))).thenReturn(profileDefinition);

        this.mockMvc.perform(get("/v4/testProfile/profile").header("auth-header", "test-user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.profile").value("testProfile"));
    }
}
