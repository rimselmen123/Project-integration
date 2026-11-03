package com.example.ecommerce;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdditionalIntegrationTest {
  @Test
  void angularOriginIsAllowedAndOtherOriginsAreRejected() throws Exception {
    mvc.perform(options("/api/cart").header("Origin", "http://localhost:4200")
        .header("Access-Control-Request-Method", "POST")
        .header("Access-Control-Request-Headers", "authorization,content-type"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    mvc.perform(options("/api/cart").header("Origin", "http://untrusted.example")
        .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isForbidden());
  }
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  String login() throws Exception {
    return body(mvc.perform(
                post("/api/auth/login")
                    .contentType("application/json")
                    .content("{\"email\":\"admin@demo.com\",\"password\":\"Demo12345!\"}"))
            .andExpect(status().isOk())
            .andReturn())
        .get("token")
        .asText();
  }

  String client() throws Exception {
    String email = UUID.randomUUID() + "@example.com";
    return body(mvc.perform(
                post("/api/auth/register")
                    .contentType("application/json")
                    .content(
                        "{\"name\":\"Test\",\"email\":\""
                            + email
                            + "\",\"password\":\"Strong123!\"}"))
            .andExpect(status().isCreated())
            .andReturn())
        .get("token")
        .asText();
  }

  JsonNode body(MvcResult r) throws Exception {
    return json.readTree(r.getResponse().getContentAsString());
  }

  long product(String admin, String price, int stock) throws Exception {
    return body(mvc.perform(
                post("/api/products")
                    .header("Authorization", "Bearer " + admin)
                    .contentType("application/json")
                    .content(
                        "{\"name\":\"Test product\",\"description\":\"Fixture\",\"price\":"
                            + price
                            + ",\"stock\":"
                            + stock
                            + ",\"categoryId\":1,\"imageUrl\":\"/assets/placeholder.svg\",\"active\":true}"))
            .andExpect(status().isCreated())
            .andReturn())
        .get("id")
        .asLong();
  }

  void add(String token, long id, int quantity) throws Exception {
    mvc.perform(
            post("/api/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"productId\":" + id + ",\"quantity\":" + quantity + "}"))
        .andExpect(status().isOk());
  }

  MvcResult checkout(String token) throws Exception {
    return mvc.perform(
            post("/api/orders")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"shippingAddress\":\"12 rue de la Demo, Tunis\"}"))
        .andReturn();
  }

  @Test
  void validLargeTotalCanBePersisted() throws Exception {
    String admin = login(), client = client();
    long id = product(admin, "99999999.99", 101);
    add(client, id, 101);
    MvcResult result = checkout(client);
    assertThat(result.getResponse().getStatus()).isEqualTo(201);
    assertThat(body(result).get("total").decimalValue()).isEqualByComparingTo("10099999998.99");
  }

  @Test
  void twoClientsCannotBuyTheSameLastStock() throws Exception {
    String admin = login(), a = client(), b = client();
    long id = product(admin, "10.00", 1);
    add(a, id, 1);
    add(b, id, 1);
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      Future<Integer> first =
          executor.submit(
              () -> {
                start.await();
                return checkout(a).getResponse().getStatus();
              });
      Future<Integer> second =
          executor.submit(
              () -> {
                start.await();
                return checkout(b).getResponse().getStatus();
              });
      start.countDown();
      assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
          .containsExactlyInAnyOrder(201, 409);
      mvc.perform(get("/api/products/" + id))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.stock").value(0));
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void cartUpdateRemovalAndOrderSnapshotSurviveProductChanges() throws Exception {
    String admin = login(), client = client();
    long id = product(admin, "7.50", 10);
    add(client, id, 1);
    JsonNode cart =
        body(
            mvc.perform(get("/api/cart").header("Authorization", "Bearer " + client))
                .andExpect(status().isOk())
                .andReturn());
    long item = cart.get("items").get(0).get("id").asLong();
    mvc.perform(
            put("/api/cart/items/" + item)
                .header("Authorization", "Bearer " + client)
                .contentType("application/json")
                .content("{\"quantity\":2}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(15));
    mvc.perform(delete("/api/cart/items/" + item).header("Authorization", "Bearer " + client))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/cart").header("Authorization", "Bearer " + client))
        .andExpect(jsonPath("$.items.length()").value(0));
    add(client, id, 2);
    MvcResult order = checkout(client);
    assertThat(order.getResponse().getStatus()).isEqualTo(201);
    long orderId = body(order).get("id").asLong();
    mvc.perform(
            put("/api/products/" + id)
                .header("Authorization", "Bearer " + admin)
                .contentType("application/json")
                .content(
                    "{\"name\":\"Changed\",\"price\":100,\"stock\":8,\"categoryId\":1,\"active\":true}"))
        .andExpect(status().isOk());
    mvc.perform(delete("/api/products/" + id).header("Authorization", "Bearer " + admin))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/products/" + id)).andExpect(status().isNotFound());
    mvc.perform(get("/api/orders/" + orderId).header("Authorization", "Bearer " + client))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].productName").value("Test product"))
        .andExpect(jsonPath("$.items[0].price").value(7.5))
        .andExpect(jsonPath("$.total").value(15));
  }

  @Test
  void categoryCrudAndUsedCategoryConflict() throws Exception {
    String admin = login();
    long id =
        body(mvc.perform(
                    post("/api/categories")
                        .header("Authorization", "Bearer " + admin)
                        .contentType("application/json")
                        .content("{\"name\":\"New category\",\"description\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andReturn())
            .get("id")
            .asLong();
    mvc.perform(
            put("/api/categories/" + id)
                .header("Authorization", "Bearer " + admin)
                .contentType("application/json")
                .content("{\"name\":\"Edited category\",\"description\":\"Updated\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Edited category"));
    mvc.perform(delete("/api/categories/" + id).header("Authorization", "Bearer " + admin))
        .andExpect(status().isNoContent());
    mvc.perform(delete("/api/categories/1").header("Authorization", "Bearer " + admin))
        .andExpect(status().isConflict());
  }
}
