package com.example.leonamlucius.compesa_facil.services;

import java.util.Calendar;

import org.springframework.stereotype.Service;

import com.example.leonamlucius.compesa_facil.dto.CalendarResponse;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitForSelectorState;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class CalendarService {

    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
                        .setSlowMo(250));
    }

    public CalendarResponse takeScreenshot(String searchTerm) {
        try (BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setViewportSize(1280, 720))) {
            Page page = context.newPage();
            page.navigate("https://giscomp.compesa.com.br/calendario.html");
            Locator input = page.locator("input[type='text']#search_input").first();
            input.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            input.fill(searchTerm);
            input.press("Enter");

            Locator calendar = page.locator(".user").first();
            calendar.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            byte[] screenshot = calendar.screenshot(
                    new Locator.ScreenshotOptions().setType(ScreenshotType.PNG));
            return new CalendarResponse(
                    screenshot,
                    searchTerm,
                    Calendar.getInstance().getTime());
        }
    }

    @PreDestroy
    public void cleanup() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

}
