package dev.selenium.bidirectional;

import static org.openqa.selenium.devtools.events.CdpEventTypes.domMutation;
import static org.openqa.selenium.remote.http.Contents.utf8String;

import com.google.common.net.MediaType;
import dev.selenium.BaseTest;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.HasAuthentication;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UsernameAndPassword;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.devtools.events.DomMutationEvent;
import org.openqa.selenium.logging.HasLogEvents;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.http.Route;

class BiDiCdpTest extends BaseTest {

  @BeforeEach
  public void setup() {
    driver = new ChromeDriver();
  }

  @Test
  void testBasicAuth() {
    Predicate<URI> uriPredicate = uri -> uri.getHost().contains("the-internet.herokuapp.com");

    ((HasAuthentication) driver).register(uriPredicate,
        UsernameAndPassword.of("admin", "admin"));
    driver.get("https://the-internet.herokuapp.com/basic_auth");

    WebElement element = driver.findElement(By.tagName("p"));
    String expected = "Congratulations! You must have the proper credentials.";

    Assertions.assertEquals(expected, element.getText());
  }

  @Test
  public void testDomMutation() throws Exception {
    AtomicReference<DomMutationEvent> seen = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(1);
    ((HasLogEvents) driver).onLogEvent(domMutation(mutation -> {
      seen.set(mutation);
      latch.countDown();
    }));

    driver.get("https://www.selenium.dev");

    WebElement span = driver.findElement(By.cssSelector("span"));
    ((JavascriptExecutor) driver)
        .executeScript("arguments[0].setAttribute('cheese', 'gouda');", span);

    Assertions.assertTrue(latch.await(10, TimeUnit.SECONDS));
    Assertions.assertEquals("gouda", span.getDomAttribute("cheese"));
    Assertions.assertEquals("cheese", seen.get().getAttributeName());
    Assertions.assertEquals("gouda", seen.get().getCurrentValue());
  }

  @Test
  public void testNetworkInterceptor() {
    try (NetworkInterceptor ignore = new NetworkInterceptor(
        driver,
        Route.matching(req -> req.getUri().contains("selenium"))
            .to(() -> req -> new HttpResponse()
                .setStatus(200)
                .addHeader("Content-Type", MediaType.HTML_UTF_8.toString())
                .setContent(utf8String("Creamy, delicious cheese!"))))) {

      driver.get("https://www.selenium.dev/");

      String source = driver.getPageSource();
      Assertions.assertTrue(source.contains("delicious cheese!"));
    }
  }
}
