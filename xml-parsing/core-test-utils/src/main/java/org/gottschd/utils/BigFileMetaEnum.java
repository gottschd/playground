package org.gottschd.utils;

import org.apache.commons.text.StringEscapeUtils;

public enum BigFileMetaEnum {
    CDATA_BASED("req_cdata_embedded_template_top.xml", "req_cdata_embedded_template_bottom.xml") {
        @Override
        public String toEscapeOrNotToEscape(String pValue) {
            return pValue;
        }
    },
    ESCAPED_BASED("req_escaped_embedded_template_top.xml", "req_escaped_embedded_template_bottom.xml") {
        @Override
        public String toEscapeOrNotToEscape(String pInput) {
            return StringEscapeUtils.escapeXml11(pInput);
        }
    };

    public final String top_template_filename;
    public final String bottom_template_filename;

    private BigFileMetaEnum(String top_file_name, String bottom_file_name) {
        top_template_filename = top_file_name;
        bottom_template_filename = bottom_file_name;
    }

    public abstract String toEscapeOrNotToEscape(String pValue);
}