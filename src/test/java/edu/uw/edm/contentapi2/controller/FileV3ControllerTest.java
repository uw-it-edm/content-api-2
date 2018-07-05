package edu.uw.edm.contentapi2.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uw.edm.contentapi2.common.FieldMapper;
import edu.uw.edm.contentapi2.controller.content.v3.FileV3Controller;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentDispositionType;
import edu.uw.edm.contentapi2.controller.content.v3.model.ContentRenditionType;
import edu.uw.edm.contentapi2.security.User;
import edu.uw.edm.contentapi2.service.FileServingService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileV3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FieldMapper fieldMapper;

    @MockBean
    FileServingService fileServingService;

    @InjectMocks
    private FileV3Controller fileV3Controller;

    @Test
    public void getFileTest() throws Exception {
        this.mockMvc.perform(get("/v3/file/my-item-id").header("auth-header", "test-user"))
                .andExpect(status().isOk());
        verify(fileServingService, times(1)).serveFile(eq("my-item-id"), eq(ContentRenditionType.Web), eq(ContentDispositionType.inline), eq(false), any(User.class), any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenForcePdfThrowIllegalArgumentException() throws Exception {
        fileV3Controller.read("my-item-id", ContentRenditionType.Web, true, false, ContentDispositionType.inline, mock(User.class), mock(HttpServletRequest.class), mock(HttpServletResponse.class));
    }
}
