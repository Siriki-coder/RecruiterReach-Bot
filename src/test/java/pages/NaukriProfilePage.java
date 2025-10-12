package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor; // New Import Added!
import java.io.File;

/**
 * Page Object Model for all interactions on the Naukri Profile Page.
 * Extends BasePage to inherit driver, wait, and configuration utilities.
 */
public class NaukriProfilePage extends BasePage {

    // --- Page Locators (Use private final By for best practice) ---
    private final By loginButton = By.xpath("//a[text()='Login']");
    private final By usernameField = By.cssSelector("input[placeholder='Enter your active Email ID / Username']");
    private final By passwordField = By.cssSelector("input[placeholder='Enter your password']");
    private final By submitButton = By.xpath("//button[text()='Login']");
    
    private final By profileIcon = By.cssSelector(".nI-gNb-drawer__icon"); // User profile icon after login
    private final By viewProfileLink = By.xpath("//a[text()='View & Update Profile']");
    
    // Headline update elements
    private final By editHeadlineButton = By.xpath("//span[text()='Resume headline']//following-sibling::span[text()='editOneTheme']");
    private final By headlineInputField = By.xpath("//textarea[contains(@placeholder,'Minimum 5 words')]");
    private final By saveButton = By.xpath("//button[text()='Save']");
    
    // Resume Upload element (file input field)
    private final By uploadResumeInputField = By.id("attachCV"); 
    private final By uploadSuccessMessage = By.xpath("//div[contains(text(),'Resume Uploaded Successfully')]");


    /**
     * Navigates to the base URL defined in config.properties.
     */
    public void navigateToLogin() {
        String url = getConfig("url");
        logger.info("Action: Navigating to URL: " + url);
        driver.get(url);
    }

    /**
     * Performs the login sequence using credentials from config.properties.
     */
    public void login() {
        logger.info("Action: Starting login process.");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
            
            wait.until(ExpectedConditions.presenceOfElementLocated(usernameField))
                .sendKeys(getConfig("username"));
                
            driver.findElement(passwordField).sendKeys(getConfig("password"));
                
            driver.findElement(submitButton).click();
            
            wait.until(ExpectedConditions.presenceOfElementLocated(profileIcon));
            logger.info("Verification: Login successful. Profile icon found.");
        } catch (Exception e) {
            logger.severe("Error during login: " + e.getMessage());
            // Re-throw or use TestNG assertion to fail the test if login fails
            throw new RuntimeException("Login failed.", e);
        }
    }

    /**
     * Toggles and updates the profile headline to keep the profile 'fresh'.
     */
    public void updateProfileHeadline() {
        logger.info("Action: Navigating to profile view.");
        wait.until(ExpectedConditions.elementToBeClickable(profileIcon)).click();
        wait.until(ExpectedConditions.elementToBeClickable(viewProfileLink)).click();
        
        try {
            logger.info("Action: Clicking the edit headline button.");
            wait.until(ExpectedConditions.elementToBeClickable(editHeadlineButton)).click();
            
            WebElement headlineElement = wait.until(ExpectedConditions.presenceOfElementLocated(headlineInputField));
            String currentHeadline = headlineElement.getAttribute("value");
            String newHeadline = toggleHeadline(currentHeadline);

            logger.info("Data: Toggling headline from: '" + currentHeadline + "' to: '" + newHeadline + "'");
            
            headlineElement.clear();
            headlineElement.sendKeys(newHeadline);
            
            driver.findElement(saveButton).click();
            
            wait.until(ExpectedConditions.presenceOfElementLocated(editHeadlineButton));
            logger.info("Verification: Profile headline updated successfully.");
        } catch (Exception e) {
            logger.warning("Warning: Could not update headline. Element structure may have changed. " + e.getMessage());
        }
    }
    
    /**
     * Core functionality to upload the resume file specified in config.properties.
     * Uses JavaScript executor to ensure the file input field is accessible.
     */
    public void uploadResume() {
        String relativeResumePath = getConfig("resume.path");
        File file = new File(relativeResumePath);
        String absoluteResumePath = file.getAbsolutePath();

        // 1. Check if the file exists locally
        if (!file.exists()) {
            logger.severe("Error: Resume file not found at expected absolute path: " + absoluteResumePath);
            return;
        }

        logger.info("Data: Preparing to upload file from: " + absoluteResumePath);

        // 2. Ensure we are on the profile page
        if (!driver.getCurrentUrl().contains("profile")) {
             wait.until(ExpectedConditions.elementToBeClickable(profileIcon)).click();
             wait.until(ExpectedConditions.elementToBeClickable(viewProfileLink)).click();
        }

        try {
            // 3. Find the file input field
            WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(uploadResumeInputField));
            
            // 4. FIX: Cast driver to JavascriptExecutor to gain access to executeScript()
            logger.info("Action: Using Javascript to ensure the hidden file input is visible and focused.");
            ((JavascriptExecutor) driver).executeScript("arguments[0].style.display='block'; arguments[0].scrollIntoView(true);", fileInput);

            // 5. Send the absolute path of the file to the input element
            fileInput.sendKeys(absoluteResumePath);

            logger.info("Action: File path sent to the input field. Waiting for upload completion...");
            
            // 6. Wait for a success indicator or a reasonable time
            wait.until(ExpectedConditions.visibilityOfElementLocated(uploadSuccessMessage));
            logger.info("Verification: Resume Uploaded Successfully message appeared.");

        } catch (Exception e) {
            logger.severe("Fatal Error during resume upload: " + e.getMessage());
            // This is a critical failure point
            throw new RuntimeException("Failed to complete resume upload process.", e);
        }
    }

    /**
     * Utility method to generate a slightly different headline to trigger an update.
     */
    private String toggleHeadline(String headline) {
        // Simple logic to force an update by toggling a character at the end
        if (headline.endsWith(".")) {
            return headline.substring(0, headline.length() - 1) + "!";
        } else if (headline.endsWith("!")) {
            return headline.substring(0, headline.length() - 1) + "~";
        } else {
            return headline + ".";
        }
    }
}
