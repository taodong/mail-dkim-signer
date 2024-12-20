package io.github.taodong.mail.dkim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DkimSignHeaderTest {

    @Test
    void testEqualAndHashCode() {
        DkimSignHeader header1 = new DkimSignHeader("From");
        DkimSignHeader header2 = new DkimSignHeader("From", true);
        assertEquals(header1, header2);
        assertEquals(header1.hashCode(), header2.hashCode());
    }

}