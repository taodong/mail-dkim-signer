package io.github.taodong.mail.dkim.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DkimSignatureTest {

    @Test
    void getValue() throws DkimSigningException {
        DkimSignature signature = new DkimSignature();
        signature.addTagValue(HeaderTags.VERSION, "1");
        signature.addTagValue(HeaderTags.ALGORITHM, "rsa-sha256");
        signature.addTagValue(HeaderTags.DOMAIN, "duotail.com");
        signature.addTagValue(HeaderTags.CANONICALIZATION, "relaxed/simple");
        signature.addTagValue(HeaderTags.USERNAME, "tao.dong@duotail.com");
        signature.addTagValue(HeaderTags.SELECTOR, "selector1");
        signature.addTagValue(HeaderTags.HEADERS, "from:to:subject");
        signature.addTagValue(HeaderTags.BODY_HASH, "hash");
        signature.addTagValue(HeaderTags.SIGNATURE, "signature");

        String expected = "v=1; a=rsa-sha256; d=duotail.com; c=relaxed/simple; i=tao.dong@duotail.com; s=selector1; h=from:to:subject; bh=hash; b=signature";
        assertEquals(expected, signature.getValue());
    }
}