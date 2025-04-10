# White-Label Support

## Gui-driven Rebadging

The system allows for a GUI-driven rebadging process for the system admin
to customize the application. This includes changing the logo, colors, and other branding elements to match the institution's identity.

- Login as the system admin.
- Go to "Rebrand UI" in the admin panel.
- Upload/change any institution-specific labeling.

## Integrate Different SSO Provider

Follow the steps below to integrate a different SSO provider from the deployment guide:
[Section 3.6 - Configure Shibboleth for SSO Authentication](deployment.md#36-configure-shibboleth-for-sso-authentication)

## Integrate Different Transcript Provider

By default, the system uses Ulink as the transcript provider. To integrate a different transcript provider, follow the steps below:
1. Create a new class that implements the `TranscriptService` interface.
2. Implement the required methods in your new class.
```java
  /**
   * Retrieves the transcript data for a given student.
   *
   * @param student The student's username.
   * @return A list of CourseInfo records containing the transcript data.
   * @throws TranscriptServiceException If an error occurs while retrieving the data.
   */
  List<CourseInfo> retrieveTranscriptData(String student) throws TranscriptServiceException;

  /**
   * Authenticates a student with the external service using their username and PIN.
   *
   * @param username The student's username.
   * @param pin      The student's PIN.
   * @return true if authentication is successful, false otherwise.
   * @throws TranscriptServiceException If an error occurs during authentication.
   */
  Boolean authenticate(String username, String pin) throws TranscriptServiceException;
}
```
3. Remove or unplug the previous provider "UlinkTranscriptService" by deleting the file.


## Configure Email Provider Account

Our email provider is Mailgun. Create an account at [Mailgun](https://www.mailgun.com/) and set up your domain.
Use your API key for the configuration in the application.properties file. Make sure application.properties is properly
hidden since it contains sensitive information.
```properties
mailgun.api.key=YOUR_API_KEY
mailgun.domain=YOUR_DOMAIN
```


