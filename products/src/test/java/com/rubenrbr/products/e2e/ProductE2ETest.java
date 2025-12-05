package com.rubenrbr.products.e2e;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductE2ETest {

  @Autowired private WebTestClient webTestClient;

  private static WireMockServer wireMockServer;

  @BeforeAll
  static void startWireMock() {
    wireMockServer = new WireMockServer(wireMockConfig().port(8089));
    wireMockServer.start();
    WireMock.configureFor("localhost", 8089);
  }

  @AfterAll
  static void stopWireMock() {
    if (wireMockServer != null && wireMockServer.isRunning()) {
      wireMockServer.stop();
    }
  }

  @BeforeEach
  void setUp() {
    wireMockServer.resetAll();
    setupWireMockStubs();
  }

  private void setupWireMockStubs() {
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/1/similarids"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[\"2\", \"3\", \"4\"]")));

    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/2"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"2\",\"name\":\"Pants\",\"price\":19.99,\"availability\":true}")));

    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/3"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"3\",\"name\":\"Shoes\",\"price\":29.99,\"availability\":false}")));

    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/4"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"4\",\"name\":\"Hat\",\"price\":14.99,\"availability\":true}")));

    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/999"))
            .willReturn(WireMock.aResponse().withStatus(404).withBody("Product not found")));

    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/999/similarids"))
            .willReturn(WireMock.aResponse().withStatus(404).withBody("Product not found")));
  }

  @Test
  void getSimilarProducts_shouldReturnListOfSimilarProducts() {
    webTestClient
        .get()
        .uri("/product/1/similar")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(3)
        .jsonPath("$[?(@.id == '2')].name")
        .isEqualTo("Pants")
        .jsonPath("$[?(@.id == '3')].name")
        .isEqualTo("Shoes")
        .jsonPath("$[?(@.id == '4')].name")
        .isEqualTo("Hat");
  }

  @Test
  void getSimilarProducts_shouldIncludeProductWithAvailabilityFalse() {
    webTestClient
        .get()
        .uri("/product/1/similar")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(3)
        .jsonPath("$[?(@.id == '3')].availability")
        .isEqualTo(false);
  }

  @Test
  void getSimilarProducts_whenProductNotFound_shouldReturn404() {
    webTestClient
        .get()
        .uri("/product/999/similar")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void getSimilarProducts_whenExternalApiReturnsEmptyList_shouldReturnEmptyArray() {
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/5/similarids"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[]")));

    webTestClient
        .get()
        .uri("/product/5/similar")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(0);
  }

  @Test
  void getSimilarProducts_whenExternalApiReturns500_shouldReturn500() {
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/100/similarids"))
            .willReturn(WireMock.aResponse().withStatus(500).withBody("Internal Server Error")));

    webTestClient
        .get()
        .uri("/product/100/similar")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }

  @Test
  void getSimilarProducts_whenExternalApiReturns503_shouldReturn500() {
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/101/similarids"))
            .willReturn(WireMock.aResponse().withStatus(503).withBody("Service Unavailable")));

    webTestClient
        .get()
        .uri("/product/101/similar")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }

  @Test
  void getSimilarProducts_whenOneProductDetailFails_shouldFilterItOut() {
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/6/similarids"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[\"2\", \"999\", \"4\"]")));

    webTestClient
        .get()
        .uri("/product/6/similar")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(2)
        .jsonPath("$[?(@.id == '2')].name")
        .isEqualTo("Pants")
        .jsonPath("$[?(@.id == '4')].name")
        .isEqualTo("Hat");
  }

  @Test
  void getSimilarProducts_whenOneProductDetailReturns500_shouldFilterItOut() {
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/7/similarids"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[\"2\", \"500\", \"4\"]")));

    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/500"))
            .willReturn(WireMock.aResponse().withStatus(500).withBody("Internal Server Error")));

    webTestClient
        .get()
        .uri("/product/7/similar")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }

  @Test
  void getSimilarProducts_withLargeNumberOfSimilarProducts_shouldReturnAll() {
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/8/similarids"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[\"2\", \"3\", \"4\", \"9\", \"10\"]")));

    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/9"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"9\",\"name\":\"Jacket\",\"price\":89.99,\"availability\":true}")));

    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/10"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"10\",\"name\":\"Scarf\",\"price\":19.99,\"availability\":true}")));

    webTestClient
        .get()
        .uri("/product/8/similar")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(5);
  }

  @Test
  void getSimilarProducts_withAlphanumericProductId_shouldWork() {
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/ABC123/similarids"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[\"2\", \"3\"]")));

    webTestClient
        .get()
        .uri("/product/ABC123/similar")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(2);
  }

  @Test
  void getSimilarProducts_shouldReturnDifferentProductsForDifferentIds() {
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/11/similarids"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[\"2\"]")));

    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo("/product/12/similarids"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[\"3\", \"4\"]")));

    webTestClient
        .get()
        .uri("/product/11/similar")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(1)
        .jsonPath("$[0].id")
        .isEqualTo("2");

    webTestClient
        .get()
        .uri("/product/12/similar")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()")
        .isEqualTo(2)
        .jsonPath("$[?(@.id == '3')].name")
        .isEqualTo("Shoes")
        .jsonPath("$[?(@.id == '4')].name")
        .isEqualTo("Hat");
  }
}
