package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest //main annotation to add
@AutoConfigureMockMvc
public class MyIntegrationIT {

    @Autowired
    private MockMvc mockMvc; //client program

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));
    }

    @Test
    @DirtiesContext
    void shouldAddUser() throws Exception {
        UserRequest request = new UserRequest("Michael", "Jordan", "mj@example.com", 23, 100000.0);

        mockMvc.perform(post("/hello")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Michael Jordan"))
                .andExpect(jsonPath("$.email").value("mj@example.com"));

        // Verify it was actually added
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[4].fullName").value("Michael Jordan"));
    }
}
