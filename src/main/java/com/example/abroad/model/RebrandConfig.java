package com.example.abroad.model;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class RebrandConfig {
  private String footerText;
  private String headName;
  private String homeTitle;
  private String homeSubtitle;

  // Markdown content
  private String homeCardContent;

  private String adminContent;
  private String studentContent;

  // Colors
  private String primaryColor;
  private String secondaryColor;
  private String accentColor;
  private String base100;


  // Getters and Setters
  public String getFooterText() {
    return unescapeBasicHtml(footerText);
  }
  public String getHomeTitle() { return homeTitle; }

  public void setFooterText(String footerText) {
    this.footerText = footerText;
  }

  public void setHomeTitle(String homeTitle) { this.homeTitle = homeTitle; }

  public String getHomeSubtitle() {
    return homeSubtitle;
  }

  public void setHomeSubtitle(String homeSubtitle) {
    this.homeSubtitle = homeSubtitle;
  }

  public void setHomeCardContent(String homeCardContent) {
    this.homeCardContent = homeCardContent;
  }

  public String getHomeCardContent() {
    return unescapeBasicHtml(homeCardContent);
  }

  public String getHomeCardContentHTML() {
    if (homeCardContent == null || homeCardContent.isEmpty()) {
      return "";
    }
    String parsedHomeCardContent = homeCardContent.replace("\\n", "\n");

    Parser parser = Parser.builder().build();
    Node document = parser.parse(parsedHomeCardContent);

    HtmlRenderer renderer = HtmlRenderer.builder().build();

    String parsedHtml = renderer.render(document);

    // Replace elements with additional classes for styling
    parsedHtml = parsedHtml.replaceAll("<h2>", "<h2 class=\"card-title text-lg mb-3\">");
    parsedHtml = parsedHtml.replaceAll("<ul>", "<ul class=\"text-left space-y-1 mb-6 text-sm\" style=\"list-style-position: inside;\">");
    parsedHtml = parsedHtml.replaceAll("<ol>", "<ol class=\"text-left space-y-1 mb-6 text-sm\" style=\"list-style-type: decimal; list-style-position: inside;\">");
    parsedHtml = parsedHtml.replaceAll("<hr\\s*/?>", "<div class=\"divider my-2\"></div>");

    return parsedHtml;
  }

  public String getHeadName() {
    return headName;
  }

  public void setHeadName(String headName) {
    this.headName = headName;
  }

  public String getAdminContent() {
    return adminContent;
  }

  public void setAdminContent(String adminContent) {
    this.adminContent = adminContent;
  }

  public String getStudentContent() {
    return studentContent;
  }

  public void setStudentContent(String studentContent) {
    this.studentContent = studentContent;
  }

  private String unescapeBasicHtml(String input) {
    if (input == null) return null;
    return input
        .replace("&copy;", "©")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("\\n", "\n")
        .replace("&#39;", "'"); // apostrophe
  }

  public String getPrimaryColor() {
    return primaryColor;
  }
  public void setPrimaryColor(String primaryColor) {
    this.primaryColor = primaryColor;
  }

  public String getSecondaryColor() {
    return secondaryColor;
  }

  public void setSecondaryColor(String secondaryColor) {
    this.secondaryColor = secondaryColor;
  }

  public String getAccentColor() {
    return accentColor;
  }

  public void setAccentColor(String accentColor) {
    this.accentColor = accentColor;
  }

  public String getBase100() {
    return base100;
  }

  public void setBase100(String base100) {
    this.base100 = base100;
  }
}
