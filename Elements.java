//
// Copyright 2016, Yahoo Inc.
// Copyrights licensed under the New BSD License.
// See the accompanying LICENSE file for terms.
//

package github.com.jminusminus.markdown;

public class Elements {

    protected String[] tokens;
    protected int length;
    protected int index = 0;

    Elements(String elements) {
        this.tokens = elements.split(" ");
        this.length = this.tokens.length;
    }

    // Returns HTML.
    public String toString() {
        String elements = "";
        String element;
        while (this.index < this.length) {
            element = this.parseToken();
            if (element.isEmpty() == false) {
                elements += " " + element;
            }
        }
        return elements.trim();
    }

    // Walks the tokens to find the next element and returns a HTML string for what it finds.
    protected String parseToken() {
        if (this.tokens[this.index].isEmpty()) {
            this.index++;
            return "";
        }
        switch (this.tokens[this.index].charAt(0)) {
            case '!': // Check for image
                return this.parseImage();
            case '[': // Check for link
                return this.parseLink();
            case '_': // Check for emphasis
                return this.parseEmphasis();
            case '`': // Check for code
                return this.parseCode();
            default: // Text
                this.index++;
                return this.tokens[this.index - 1];
        }
    }

    // Look at future tokens to find the last round bracket "[]()"
    // [http://foo.com/](some text)
    // <a href="http://foo.com/">text</a>
    protected String parseLink() {
        String element = this.findLink();
        String text = this.findFirstSection(element, '[', ']');
        String url = this.findLastSection(element, '(', ')');
        text = new Elements(text).toString();
        return "<a href=\"" + url + "\">" + text + "</a>";
    }

    // Look at future tokens to find the last round bracket "![]()"
    // ![http://foo.com/](some text)
    // <img src="http://foo.com/" alt="text">
    protected String parseImage() {
        String element = this.findLink();
        String text = this.findFirstSection(element, '[', ']');
        String url = this.findFirstSection(element, '(', ')');
        return "<img src=\"" + url + "\" alt=\"" + text + "\">";
    }

    // Count the number of underscores and then look for the matching count in future tokens.
    // _some text_
    // <em>some text</em>
    // __some text__
    // <b>some text</b>
    protected String parseEmphasis() {
        String token = "";
        String element = "";
        int start = 1;
        int end = 0;
        while (this.index < this.length) {
            element += this.tokens[this.index] + " ";
            end = element.length();
            if (element.charAt(end - 1) == '_') {
                break;
            }
            this.index++;
        }
        if (element.charAt(1) == '_' && element.charAt(end - 3) == '_') {
            return "<strong>" + element.substring(2, end - 3) + "</strong>";
        }
        return "<em>" + element.substring(start, end - 2) + "</em>";
    }

    // Count the number of back ticks and then look for the matching count in future tokens.
    // `some text`
    // <code>some text</code>
    protected String parseCode() {
        String token = "";
        String element = "";
        int end = 0;
        while (this.index < this.length) {
            element = (element + " " + this.tokens[this.index]).trim();
            end = element.length() - 1;
            this.index++;
            if (element.charAt(end) == '`') {
                break;
            }
        }
        return "<code>" + element.substring(1, end) + "</code>";
    }

    // Look at future tokens to find the last round bracket "[]()"
    protected String findLink() {
        String token = "";
        String element = "";
        int end = 0;
        while (this.index < this.length) {
            element = (element + " " + this.tokens[this.index]).trim();
            end = element.length() - 1;
            this.index++;
            if (element.charAt(end) == ']') {
                return element.substring(0, end);
            }
            if (element.charAt(end) == ')') {
                break;
            }
        }
        return element.trim();
    }

    // Finds a section between the start and end chars provided.
    // [some text] == some text
    // [some [] text] == some [] text
    protected String findLastSection(String element, char start, char end) {
        int sectionStart = element.length() - 1;
        int sectionEnd = -1;
        int sections = 0;
        while (sectionStart >= 0) {
            if (element.charAt(sectionStart) == end) {
                sections++;
                if (sectionEnd == -1) {
                    sectionEnd = sectionStart;
                }
            } else if (element.charAt(sectionStart) == start) {
                sections--;
            }
            if (sectionEnd > -1 && sections == 0) {
                return element.substring(sectionStart + 1, sectionEnd);
            }
            sectionStart--;
        }
        return element;
    }

    // Finds a section between the start and end chars provided.
    // [some text] == some text
    // [some [] text] == some [] text
    protected String findFirstSection(String element, char start, char end) {
        int sectionStart = -1;
        int sectionEnd = 0;
        int sections = 0;
        while (sectionEnd < element.length()) {
            if (element.charAt(sectionEnd) == start) {
                sections++;
                if (sectionStart == -1) {
                    sectionStart = sectionEnd;
                }
            } else if (element.charAt(sectionEnd) == end) {
                sections--;
            }
            if (sectionStart > -1 && sections == 0) {
                return element.substring(sectionStart + 1, sectionEnd);
            }
            sectionEnd++;
        }
        return element;
    }
}
