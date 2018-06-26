package fjtdg.demo.response_template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fjtdg.demo.Greeting;
import fjtdg.demo.Weather;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.boot.test.json.JacksonTester.initFields;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ResponseTemplateGreetingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
        .port(8800)
        .extensions(new ResponseTemplateTransformer(true))
    );

    private JacksonTester<Greeting> greetingJson;
    private JacksonTester<Weather> weatherJson;

    @Before
    public void setUp() throws Exception {
        initFields(this, objectMapper);
        wireMockRule.stubFor(
            get(urlPathEqualTo("/weather"))
                .withQueryParam("location", equalTo("Here"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\":{{request.requestLine.query.id}},\"sky\":\"Cloudy\"}")
                )
        );
        wireMockRule.stubFor(
            get(urlPathEqualTo("/weather"))
                .withQueryParam("location", equalTo("There"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\":{{request.requestLine.query.id}},\"sky\":\"Sunny\"}")
                )
        );
    }

    @Test
    public void shouldDefaultToWorld() throws Exception {
        final String body = restTemplate.getForObject("/template/greeting", String.class);
        final Greeting greeting = greetingJson.parseObject(body);
        final long id = greeting.getId();
        assertThat(greeting.getContent()).isEqualTo(String.format("Hello, (%d) Cloudy World!", id));
    }

    @Test
    public void shouldIncludeName() throws Exception {
        final String body = restTemplate.getForObject("/template/greeting?name=Test", String.class);
        final Greeting greeting = greetingJson.parseObject(body);
        final long id = greeting.getId();
        assertThat(greeting.getContent()).isEqualTo(String.format("Hello, (%d) Sunny Test!", id));
    }
}
