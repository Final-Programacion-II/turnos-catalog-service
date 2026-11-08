package ar.edu.um.turnos.catalog.web.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.um.turnos.catalog.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * El catálogo no emite tokens: los usuarios inician sesión en turnos (ver docs/decisiones.md, punto 8).
 */
@AutoConfigureMockMvc
@IntegrationTest
class AuthenticateControllerIT {

    private static final String LOGIN = "{ \"username\": \"admin\", \"password\": \"admin\" }";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void elLoginDeEjemploYaNoExiste() throws Exception {
        // Sin token, el pedido ni siquiera llega: todo /api pide un JWT
        mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
            .andExpect(status().isUnauthorized())
            .andExpect(header().doesNotExist("Authorization"));
    }

    @Test
    void conUnTokenValidoTampocoSeEmiteOtro() throws Exception {
        // Con token llega al controlador, pero ya no hay un POST que devuelva un JWT nuevo
        mockMvc
            .perform(post("/api/authenticate").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(LOGIN))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(header().doesNotExist("Authorization"));
    }
}
