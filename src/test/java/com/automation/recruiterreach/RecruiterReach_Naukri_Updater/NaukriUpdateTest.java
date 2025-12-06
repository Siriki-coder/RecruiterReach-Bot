package com.automation.recruiterreach.RecruiterReach_Naukri_Updater;


import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.NaukriProfilePage;

/**
 * Test class to execute hourly updates for the Naukri profile.
 * Follows TestNG structure (BeforeClass, Test, AfterClass).
 */
public class NaukriUpdateTest {
    NaukriProfilePage naukriPage;

    @BeforeMethod
    public void setup() {
        // Initializes the Page Object, which loads the configuration and sets up the driver
        naukriPage = new NaukriProfilePage();
        naukriPage.initializeDriver();
    }

    @Test(priority = 1, description = "Logs in and toggles the profile headline for a soft update.")
    public void hourlyProfileUpdateTest() {
        naukriPage.navigateToLogin();
        naukriPage.login();
        naukriPage.updateProfileHeadline();
    }
    
    @Test(priority = 2, description = "Logs in and uploads a new resume file for a strong update signal.")
    public void hourlyResumeUploadTest() {
        // We ensure a fresh session for the resume upload
        naukriPage.navigateToLogin();
        naukriPage.login();
        naukriPage.uploadResume();
    }

    @AfterMethod
    public void tearDown() {
        // Ensures the browser is properly closed after all tests in the class complete
        naukriPage.quitDriver();
    }
}
