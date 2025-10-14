package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor; // New Import Added!
import java.io.File;
import java.util.List;

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
    private final By uploadSuccessMessage = By.xpath("//div[@class='msgBox success']//p[@class='msg']]");
    
    private By resumeName = By.xpath("//div[@class='resume-name-inline']//div");
    private By resumeUploadDate = By.xpath("//div[contains(@class,'updateOn typ')]");
    private By attachCVButton = By.id("attachCV");
    private By uploadResumeInputField = By.xpath("//input[@type='file' and @id='attachCV']");

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
     * Uploads the resume file specified in config.properties.
     * Robust version: handles dynamic DOMs, hidden elements, and iframes.
     */
    public void uploadResume() {
        String relativeResumePath = getConfig("resume.path");
        File file = new File(relativeResumePath);
        String absoluteResumePath = file.getAbsolutePath();

        if (!file.exists()) {
            logger.severe("❌ Resume file not found at: " + absoluteResumePath);
            return;
        }
        logger.info("📄 Preparing to upload from: " + absoluteResumePath);

        try {
            // 1. Navigate to profile page if needed
            if (!driver.getCurrentUrl().contains("profile")) {
                logger.info("🔗 Navigating to Profile page...");
                wait.until(ExpectedConditions.elementToBeClickable(profileIcon)).click();
                wait.until(ExpectedConditions.elementToBeClickable(viewProfileLink)).click();
            }

            // 2. Handle potential iframe
            List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
            for (WebElement frame : iframes) {
                if (frame.isDisplayed()) {
                    driver.switchTo().frame(frame);
                    logger.info("🪟 Switched to iframe for resume upload area.");
                    break;
                }
            }

            // 3. Try locating the Attach/Upload button
            WebElement attachButton = null;
            try {
                attachButton = wait.until(ExpectedConditions.visibilityOfElementLocated(attachCVButton));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", attachButton);
                wait.until(ExpectedConditions.elementToBeClickable(attachButton)).click();
                logger.info("🖱️ Clicked on Attach/Upload Resume button.");
            } catch (Exception e) {
                logger.warning("⚠️ Attach button not found or not clickable. Trying fallback locators...");
                // Fallback: directly look for input type=file
            }

            // 4. Locate file input
            WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(uploadResumeInputField));
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.display='block'; arguments[0].removeAttribute('hidden'); arguments[0].scrollIntoView(true);", 
                fileInput
            );

            // 5. Send file path
            fileInput.sendKeys(absoluteResumePath);
            logger.info("📤 File sent to input field.");

            // 6. Wait for uploaded file details
            WebElement uploadedFileName = wait.until(ExpectedConditions.visibilityOfElementLocated(resumeName));
            WebElement uploadedDate = wait.until(ExpectedConditions.visibilityOfElementLocated(resumeUploadDate));

            logger.info("✅ Resume uploaded successfully!");
            logger.info("📌 File Name: " + uploadedFileName.getText());
            logger.info("🕒 Uploaded On: " + uploadedDate.getText());

            // Exit iframe if switched
            driver.switchTo().defaultContent();

        } catch (Exception e) {
            logger.severe("❗ Resume upload failed: " + e.getMessage());
            driver.switchTo().defaultContent();
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
