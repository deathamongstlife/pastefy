package cc.allyapps.pastely.services;

import cc.allyapps.pastely.model.database.Paste;
import org.apache.commons.text.StringEscapeUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * ExportService - Handles exporting pastes to various formats
 * Supports PDF, HTML, and image exports
 */
public class ExportService {

    /**
     * Export paste as standalone HTML with syntax highlighting
     */
    public static String exportAsHtml(Paste paste, String highlightTheme) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>").append(StringEscapeUtils.escapeHtml4(paste.getTitle())).append("</title>\n");
        html.append("    <style>\n");
        html.append(getHtmlStyles(highlightTheme));
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <header>\n");
        html.append("            <h1>").append(StringEscapeUtils.escapeHtml4(paste.getTitle())).append("</h1>\n");
        html.append("            <div class=\"meta\">\n");
        html.append("                <span class=\"type\">").append(paste.getType()).append("</span>\n");
        html.append("                <span class=\"date\">").append(paste.getCreatedAt()).append("</span>\n");
        html.append("            </div>\n");
        html.append("        </header>\n");
        html.append("        <pre class=\"code\"><code>");
        html.append(StringEscapeUtils.escapeHtml4(paste.getContent()));
        html.append("</code></pre>\n");
        html.append("        <footer>\n");
        html.append("            <p>Exported from Pastely</p>\n");
        html.append("        </footer>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Get CSS styles for HTML export
     */
    private static String getHtmlStyles(String theme) {
        return """
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    margin: 0;
                    padding: 20px;
                    background: #f5f5f5;
                    color: #333;
                }
                .container {
                    max-width: 900px;
                    margin: 0 auto;
                    background: white;
                    border-radius: 8px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                    overflow: hidden;
                }
                header {
                    padding: 24px;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: white;
                }
                header h1 {
                    margin: 0 0 12px 0;
                    font-size: 28px;
                }
                .meta {
                    display: flex;
                    gap: 16px;
                    font-size: 14px;
                    opacity: 0.9;
                }
                .code {
                    margin: 0;
                    padding: 24px;
                    background: #f8f9fa;
                    overflow-x: auto;
                    font-family: 'Monaco', 'Menlo', monospace;
                    font-size: 14px;
                    line-height: 1.6;
                }
                footer {
                    padding: 16px 24px;
                    background: #f8f9fa;
                    text-align: center;
                    color: #666;
                    font-size: 13px;
                }
                """;
    }

    /**
     * Export paste as image (PNG)
     * Returns base64 encoded image data
     */
    public static byte[] exportAsImage(Paste paste, int width, int height) throws IOException {
        // This is a placeholder - actual implementation would use a library like
        // Selenium or Playwright to render HTML to image
        // For now, we'll generate a simple SVG that can be converted to PNG

        String svg = generateSvg(paste, width, height);
        return svg.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Generate SVG representation of paste
     */
    private static String generateSvg(Paste paste, int width, int height) {
        StringBuilder svg = new StringBuilder();

        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width)
                .append("\" height=\"").append(height).append("\">\n");
        svg.append("  <rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>\n");

        // Header background
        svg.append("  <rect width=\"100%\" height=\"80\" fill=\"url(#gradient)\"/>\n");
        svg.append("  <defs>\n");
        svg.append("    <linearGradient id=\"gradient\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"0%\">\n");
        svg.append("      <stop offset=\"0%\" style=\"stop-color:#667eea;stop-opacity:1\" />\n");
        svg.append("      <stop offset=\"100%\" style=\"stop-color:#764ba2;stop-opacity:1\" />\n");
        svg.append("    </linearGradient>\n");
        svg.append("  </defs>\n");

        // Title
        String title = paste.getTitle();
        if (title.length() > 40) {
            title = title.substring(0, 40) + "...";
        }
        svg.append("  <text x=\"20\" y=\"35\" font-family=\"Arial\" font-size=\"24\" fill=\"#ffffff\" font-weight=\"bold\">")
                .append(StringEscapeUtils.escapeXml11(title)).append("</text>\n");

        // Type and date
        svg.append("  <text x=\"20\" y=\"60\" font-family=\"Arial\" font-size=\"12\" fill=\"#ffffff\">")
                .append(paste.getType()).append(" • ").append(paste.getCreatedAt()).append("</text>\n");

        // Content preview
        String content = paste.getContent();
        String[] lines = content.split("\n");
        int y = 110;
        int maxLines = Math.min(lines.length, (height - 140) / 20);

        for (int i = 0; i < maxLines; i++) {
            String line = lines[i];
            if (line.length() > 80) {
                line = line.substring(0, 80) + "...";
            }
            svg.append("  <text x=\"20\" y=\"").append(y).append("\" font-family=\"monospace\" font-size=\"12\" fill=\"#333\">")
                    .append(StringEscapeUtils.escapeXml11(line)).append("</text>\n");
            y += 20;
        }

        svg.append("</svg>");
        return svg.toString();
    }

    /**
     * Export paste as PDF
     * Returns PDF bytes
     */
    public static byte[] exportAsPdf(Paste paste) throws IOException {
        // This would use a library like iText or Flying Saucer
        // For now, return a simple placeholder
        String html = exportAsHtml(paste, "light");

        // In a real implementation, we would convert HTML to PDF here
        // For example using Flying Saucer:
        // ITextRenderer renderer = new ITextRenderer();
        // renderer.setDocumentFromString(html);
        // renderer.layout();
        // ByteArrayOutputStream os = new ByteArrayOutputStream();
        // renderer.createPDF(os);
        // return os.toByteArray();

        return html.getBytes(StandardCharsets.UTF_8);
    }
}
