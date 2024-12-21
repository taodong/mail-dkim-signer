package io.github.taodong.mail.dkim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DkimSignatureTest {

    @Test
    void getValue() throws DkimSigningException {
        DkimSignature signature = new DkimSignature();
        signature.addTagValue(HeaderTag.DOMAIN, "duotail.com");
        signature.addTagValue(HeaderTag.CANONICALIZATION, "relaxed/simple");
        signature.addTagValue(HeaderTag.USERNAME, "tao.dong@duotail.com");
        signature.addTagValue(HeaderTag.SELECTOR, "selector1");
        signature.addTagValue(HeaderTag.HEADERS, "from:to:subject");
        signature.addTagValue(HeaderTag.BODY_HASH, "hash");
        signature.addTagValue(HeaderTag.SIGNATURE, "signature");

        String expected = "v=1; a=rsa-sha256; d=duotail.com; c=relaxed/simple; i=tao.dong@duotail.com; s=selector1; h=from:to:subject; bh=hash; b=signature";
        assertEquals(expected, signature.getValue());
    }

    @Test
    void getValue_validateFailed() {
        DkimSignature signature = new DkimSignature();
        signature.addTagValue(HeaderTag.DOMAIN, "duotail.com");
        signature.addTagValue(HeaderTag.CANONICALIZATION, "relaxed/simple");
        signature.addTagValue(HeaderTag.USERNAME, "tao.dong@duotail.com");
        signature.addTagValue(HeaderTag.HEADERS, "from:to:subject");
        signature.addTagValue(HeaderTag.BODY_HASH, "hash");
        signature.addTagValue(HeaderTag.SIGNATURE, "signature");

        assertThrows(DkimSigningException.class, signature::getValue);
    }

    @Test
    void getTagValue() {
        DkimSignature signature = new DkimSignature();
        assertEquals("1", signature.getTagValue(HeaderTag.VERSION));
        assertEquals("rsa-sha256", signature.getTagValue(HeaderTag.ALGORITHM));
        assertNull(signature.getTagValue(HeaderTag.DOMAIN));
    }

    @Test
    void getBeforeHashValue() throws DkimSigningException {
        DkimSignature signature = new DkimSignature();
        signature.addTagValue(HeaderTag.DOMAIN, "duotail.com");
        signature.addTagValue(HeaderTag.CANONICALIZATION, "relaxed/simple");
        signature.addTagValue(HeaderTag.USERNAME, "tao.dong@duotail.com");
        signature.addTagValue(HeaderTag.SELECTOR, "selector1");
        signature.addTagValue(HeaderTag.HEADERS, "from:to:subject");
        signature.addTagValue(HeaderTag.BODY_HASH, "hash");

        String expected = "v=1; a=rsa-sha256; d=duotail.com; c=relaxed/simple; i=tao.dong@duotail.com; s=selector1; h=from:to:subject; bh=hash";
        assertEquals(expected, signature.getBeforeHashValue());
    }

    @Test
    void getBeforeHashValue_missingTagValue() {
        DkimSignature signature = new DkimSignature();
        signature.addTagValue(HeaderTag.DOMAIN, "duotail.com");
        signature.addTagValue(HeaderTag.CANONICALIZATION, "relaxed/simple");
        signature.addTagValue(HeaderTag.USERNAME, "tao.dong@duotail.com");
        signature.addTagValue(HeaderTag.SELECTOR, "selector1");
        signature.addTagValue(HeaderTag.HEADERS, "from:to:subject");

        assertThrows(DkimSigningException.class, signature::getBeforeHashValue);
    }


}