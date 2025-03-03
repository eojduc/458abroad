# Security Guide for HCC Abroad

### 1. Introduction

This document provides an overview of the security measures implemented for HCC Abroad. It describes specific security features, details changes made to meet external standards, and outlines known weaknesses or warnings. This guide is intended to support requirements such as 1.6.1 (Qualys SSL rating) and 1.11 (session timeouts), along with any additional security measures implemented by the development team.

### 2. Security Features

#### 2.1 SSL/TLS Encryption and Certificate Management
- A Rating Achievement: The server has been configured to achieve an A rating on the Qualys SSL Server Test (req 1.6.1). This is ensured by:
    - Certificate & Encryption: Using certificates issued by Let’s Encrypt and enforcing secure TLS protocols and ciphers.
    - Nginx Configuration:
    - SSL termination is performed by Nginx (listening on port 443) using strong settings provided by Certbot’s configuration files (options-ssl-nginx.conf and ssl-dhparams.pem).
	-	Documentation: All changes required to achieve this grade are recorded, and a copy of the Qualys test report is included in the appendix.

#### 2.2 Session Management	
- Session Timeout Requirements (req 1.11)
    - Inactivity Timeout: Sessions expire after 15 minutes of inactivity.
	- Absolute Timeout: Sessions are forcibly terminated 24 hours after login, regardless of activity.
	-	User Notification: Upon expiration, a dialogue is displayed to inform the user, and they are redirected to the login page.
-	Implementation Details:

While the Shibboleth configuration (in Shibboleth2.xml) currently specifies a session lifetime of 28,800 seconds (8 hours) and an inactivity timeout of 3,600 seconds (1 hour), the application-level session management logic enforces the required 15-minute and 24-hour limits. The rationale is to allow the SSO system to handle authentication while the application layer manages user-facing session behavior.

#### 2.3 Single Sign-On (SSO) and Authentication
-	Shibboleth Integration:
	-	The system uses Shibboleth for Single Sign-On (SSO), ensuring that only authenticated users can access protected resources.
	-	Routing Based on Session:
	-	No SSO Session: Requests without a Shibboleth session are directly proxied to the Java application (running on port 8081).
	-	Active SSO Session: Requests with a valid Shibboleth session are routed via Apache (running on port 8080), where Shibboleth validates the session before proxying requests to the Java backend.
-	Configuration Highlights:
	-	Nginx uses a cookie-based map to determine the proper backend.
	-	Apache is configured with Shibboleth module handlers for /Shibboleth.sso endpoints, ensuring secure handling of SSO transactions.

#### 2.4 Reverse Proxy and Network Segmentation
-	Nginx as Reverse Proxy:
	-	All external traffic is funneled through Nginx, which terminates SSL connections and applies security headers.
	-	Requests are routed either to the Java application directly or via Apache based on SSO session status.
-	Apache Virtual Host Configuration:
	-	Apache is dedicated to handling SSO endpoints and uses secure request headers (e.g., X-Forwarded-Proto) to maintain the integrity of the original request.
-	Logging and Monitoring:
	-	Both Nginx and Apache log access and errors, providing critical information for debugging and security audits.

### 3. Implementation Details

#### 3.1 Nginx Configuration
-	SSL Settings:
	-	The Nginx server block listens on port 443 with SSL managed by Certbot.
	-	SSL certificates and keys are located in /etc/letsencrypt/live/[458abroad]/.
	-	Standard SSL parameters (ciphers, DH parameters, etc.) are included to comply with current best practices.
-	Request Routing:
	-	A cookie-based mapping determines whether requests are proxied directly to the Java application or routed via Apache for SSO processing.

#### 3.2 Apache Virtual Host
-	Shibboleth Handling:
	-	The Apache virtual host (listening on port 8080) is set up to process Shibboleth endpoints.
	-	Specific locations (e.g., /Shibboleth.sso) are configured to use the Shibboleth module for SSO operations, ensuring that only authenticated requests reach the backend.

#### 3.3 Shibboleth Service Provider (SP) Configuration
-	Session Settings:
	-	The Shibboleth SP is configured in shibboleth2.xml with session parameters, assertion consumer service endpoints, and various security-related handlers.
	-	Note: Although the Shibboleth session configuration specifies an 8-hour lifetime and a 1-hour inactivity timeout, application-level enforcement guarantees compliance with the 15-minute inactivity and 24-hour absolute session timeouts.
	-	Metadata and Credential Handling:
	-	Metadata is fetched and validated from the identity provider, ensuring the integrity of SAML assertions.
	-	Credential resolvers are configured to handle both signing and encryption securely.

### 4. Security Testing and Compliance

#### 4.1 Qualys SSL Server Test
-	Compliance:
	-	The current server configuration meets the requirement outlined in 1.6.1 by achieving an A rating on the Qualys SSL Server Test.
-	Changes Documented:
	-	Adjustments in Nginx’s SSL configuration (e.g., usage of strong ciphers and proper DH parameters).
	-	Implementation of managed certificates from Let’s Encrypt.
	-	The full test report is attached as an appendix along with an image of the A rating.

#### 4.2 Session Timeout Enforcement
-	Requirement 1.11 Compliance:
	-	The application’s session management ensures that users are logged out after 15 minutes of inactivity and 24 hours from login, with appropriate user notifications and redirection.
	-	Although the Shibboleth SP has its own session settings, the front-end and application logic are designed to enforce the specified limits.

### 5. Known Weaknesses and Warnings
-	Session Configuration Discrepancies:
	-	The Shibboleth configuration specifies different default timeout values (8 hours lifetime, 1 hour inactivity) compared to the application’s enforced limits (24-hour lifetime, 15-minute inactivity). It is critical that the application-layer session management correctly overrides the Shibboleth defaults to prevent security gaps.
-	Configuration Maintenance:
	-	Regular reviews of SSL settings, software patches (for Nginx, Apache, and Shibboleth), and the session management mechanisms are essential. Any misconfiguration or outdated software may lead to vulnerabilities.
-	Dependency on Third-Party Services:
	-	The security of SSO operations depends on the reliability and security of the identity provider (IdP). Continuous monitoring and periodic security assessments of the IdP are recommended.