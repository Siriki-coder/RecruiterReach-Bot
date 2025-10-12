package pages;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * BasePage is the foundation of the Page Object Model (POM).
 * It manages WebDriver initialization, configuration loading, and common utilities (like waits).
 * All other Page Objects should extend this class.
 */
public class BasePage {

    protected static WebDriver driver;
    protected static WebDriverWait wait;
    protected static Properties config;
    protected static final Logger logger = Logger.getLogger(BasePage.class.getName());

    public BasePage() {
        if (config == null) {
            loadConfig();
        }
    }

    /**
     * Loads the properties from the config.properties file.
     */
    private void loadConfig() {
        config = new Properties();
        try (FileInputStream fis = new FileInputStream("src/test/java/resources/config.properties")) {
            config.load(fis);
            logger.info("Configuration file loaded successfully.");
        } catch (IOException e) {
            logger.severe("Could not load config.properties: " + e.getMessage());
            // Exit immediately if configuration fails, as tests cannot run without it
            throw new RuntimeException("Framework initialization failed: Cannot read config.properties", e);
        }
    }

    /**
     * Initializes the WebDriver instance (Chrome is hardcoded for simplicity).
     */
    public void initializeDriver() {
        if (driver == null) {
            logger.info("Initializing WebDriver using WebDriverManager.");

            WebDriverManager.chromedriver().setup();

            // ChromeOptions for headless execution
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless"); // Run without GUI
            options.addArguments("--disable-gpu"); // Recommended for some systems
            options.addArguments("--window-size=1920,1080"); // Set window size
            options.addArguments("--no-sandbox"); // Useful for running on Linux VM
            options.addArguments("--disable-dev-shm-usage"); // Useful for running on Docker/VM

            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            logger.info("WebDriver initialized in headless mode.");
        }
    }


    /**
     * Retrieves a value from the loaded configuration properties.
     */
    protected String getConfig(String key) {
        return config.getProperty(key);
    }

    /**
     * Closes the browser and cleans up the WebDriver instance.
     */
    public void quitDriver() {
        if (driver != null) {
            logger.info("Quitting WebDriver.");
            driver.quit();
            driver = null;
        }
    }
}
