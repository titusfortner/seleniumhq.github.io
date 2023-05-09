package dev.selenium.bidirectional;

import dev.selenium.BaseTest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v113.emulation.Emulation;
import org.openqa.selenium.devtools.v113.log.Log;
import org.openqa.selenium.devtools.v113.runtime.Runtime;

class CdpTest extends BaseTest {

  @BeforeEach
  public void setup() {
    driver = new ChromeDriver();
  }

  @Test
  public void consoleLogListener() {
    DevTools devTools = ((HasDevTools)driver).getDevTools();
    devTools.createSession();
    devTools.send(Runtime.enable());
    devTools.send(Log.enable());

    List<String> exceptions = new ArrayList<>();

    devTools.addListener(Log.entryAdded(),
        logEntry -> exceptions.add(logEntry.getText()));

    driver.get("https://www.selenium.dev/selenium/web/bidi/logEntryAdded.html");
    driver.findElement(By.id("consoleLog")).click();

    String expected = "Failed to load resource: the server responded with a status of 404 ()";
    Assertions.assertEquals(expected, exceptions.get(0));
  }

  @Test
  public void jsExceptionListener() {
    DevTools devTools = ((HasDevTools) driver).getDevTools();
    devTools.createSession();

    List<JavascriptException> jsExceptionsList = new ArrayList<>();
    Consumer<JavascriptException> addEntry = jsExceptionsList::add;
    devTools.getDomains().events().addJavascriptExceptionListener(addEntry);

    driver.get("https://www.selenium.dev/selenium/web/clicks.html");

    WebElement link2click = driver.findElement(By.id("normal"));
    ((JavascriptExecutor) driver).executeScript(
        "arguments[0].setAttribute(arguments[1], arguments[2]);",
        link2click, "onclick", "throw new Error('Hello, world!')");
    link2click.click();

    String error = jsExceptionsList.get(0).getMessage();
    Assertions.assertTrue(error.contains("Error: Hello, world!"));
  }
}
