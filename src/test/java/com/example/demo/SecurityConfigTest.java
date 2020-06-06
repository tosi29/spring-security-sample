package com.example.demo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @AfterClass
    public static void tearDownAfterClass() {
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void noAuthenticationTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/secure/adminonly"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("**/auth/login"));

        mockMvc.perform(MockMvcRequestBuilders.get("/secure/memberonly"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("**/auth/login"));

        mockMvc.perform(MockMvcRequestBuilders.get("/secure/all"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("**/auth/login"));

        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(username="admin", password="password", roles={"ADMIN"})
    public void adminRoleTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/secure/adminonly"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

        mockMvc.perform(MockMvcRequestBuilders.get("/secure/memberonly"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(MockMvcRequestBuilders.get("/secure/all"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(username="member", password="password", roles={"MEMBER"})
    public void memberRoleTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/secure/adminonly"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        mockMvc.perform(MockMvcRequestBuilders.get("/secure/memberonly"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

        mockMvc.perform(MockMvcRequestBuilders.get("/secure/all"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(username="admin", password="password", roles={"ADMIN"})
    public void logoutWithoutToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout"))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username="admin", password="password", roles={"ADMIN"})
    public void logoutWithLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }

    @Test
    public void logoutWithoutLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout"))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }


    @Test
    public void denyAjaxPostRequestWithoutLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/secure/api").header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    @WithMockUser(username="admin", password="password", roles={"ADMIN"})
    public void denyAjaxPostRequestWithInvalidToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/secure/api").header("X-Requested-With", "XMLHttpRequest").with(SecurityMockMvcRequestPostProcessors.csrf().useInvalidToken()))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    @WithMockUser(username="admin", password="password", roles={"ADMIN"})
    public void allowAjaxPostRequestWithToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/secure/api").header("X-Requested-With", "XMLHttpRequest").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }


}
