package com.example.ecommerce;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  String token(String email) throws Exception {
    return json.readTree(
            mvc.perform(
                    post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"Demo12345!\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString())
        .get("token")
        .asText();
  }

  JsonNode body(MvcResult result) throws Exception {
    return json.readTree(result.getResponse().getContentAsString());
  }

  @Test
  void registrationCannotEscalateRoleAndDuplicateIsConflict() throws Exception {
    String data =
        "{\"name\":\"Etudiant\",\"email\":\"new@example.com\",\"password\":\"Strong123!\",\"role\":\"ADMIN\"}";
    mvc.perform(post("/api/auth/register").contentType("application/json").content(data))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.user.role").value("CLIENT"))
        .andExpect(jsonPath("$.user.password").doesNotExist());
    mvc.perform(post("/api/auth/register").contentType("application/json").content(data))
        .andExpect(status().isConflict());
  }

  @Test
  void anonymousAndClientCannotWriteCatalog() throws Exception {
    mvc.perform(post("/api/products").contentType("application/json").content("{}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/products")
                .header("Authorization", "Bearer " + token("client@demo.com"))
                .contentType("application/json")
                .content("{}"))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].categoryName").exists());
  }

  @Test
  void invalidLoginAndQuantityAreRejected() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"client@demo.com\",\"password\":\"bad\"}"))
        .andExpect(status().isUnauthorized());
    String t = token("client@demo.com");
    mvc.perform(
            post("/api/cart/items")
                .header("Authorization", "Bearer " + t)
                .contentType("application/json")
                .content("{\"productId\":1,\"quantity\":0}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/cart/items")
                .header("Authorization", "Bearer " + t)
                .contentType("application/json")
                .content("{\"productId\":1,\"quantity\":999}"))
        .andExpect(status().isConflict());
    mvc.perform(get("/api/cart").header("Authorization", "Bearer invalid"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void checkoutSnapshotsPricesClearsCartAndEnforcesOwnership() throws Exception {
    String t = token("client@demo.com");
    mvc.perform(delete("/api/cart").header("Authorization", "Bearer " + t))
        .andExpect(status().isNoContent());
    JsonNode cart =
        body(
            mvc.perform(
                    post("/api/cart/items")
                        .header("Authorization", "Bearer " + t)
                        .contentType("application/json")
                        .content("{\"productId\":1,\"quantity\":2}"))
                .andExpect(status().isOk())
                .andReturn());
    assertThat(cart.get("total").decimalValue()).isEqualByComparingTo("79.80");
    JsonNode order =
        body(
            mvc.perform(
                    post("/api/orders")
                        .header("Authorization", "Bearer " + t)
                        .contentType("application/json")
                        .content("{\"shippingAddress\":\"12 rue de l Universite, Tunis\"}"))
                .andExpect(status().isCreated())
                .andReturn());
    long id = order.get("id").asLong();
    assertThat(order.get("total").decimalValue()).isEqualByComparingTo("79.80");
    mvc.perform(get("/api/cart").header("Authorization", "Bearer " + t))
        .andExpect(jsonPath("$.items.length()").value(0));
    JsonNode other =
        body(
            mvc.perform(
                    post("/api/auth/register")
                        .contentType("application/json")
                        .content(
                            "{\"name\":\"Autre\",\"email\":\"other@example.com\",\"password\":\"Strong123!\"}"))
                .andExpect(status().isCreated())
                .andReturn());
    mvc.perform(
            get("/api/orders/" + id)
                .header("Authorization", "Bearer " + other.get("token").asText()))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/orders/all").header("Authorization", "Bearer " + t))
        .andExpect(status().isForbidden());
    mvc.perform(
            patch("/api/orders/" + id + "/status")
                .header("Authorization", "Bearer " + token("admin@demo.com"))
                .contentType("application/json")
                .content("{\"status\":\"CONFIRMEE\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMEE"));
    mvc.perform(
            post("/api/orders")
                .header("Authorization", "Bearer " + t)
                .contentType("application/json")
                .content("{\"shippingAddress\":\"12 rue de l Universite, Tunis\"}"))
        .andExpect(status().isConflict());
  }
}
