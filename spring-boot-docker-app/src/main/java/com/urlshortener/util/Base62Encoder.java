package com.urlshortener.util;

import io.seruco.encoding.base62.Base62;
import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {
    private final Base62 base62 = Base62.createInstance();

    public String encode(long number) {
        return new String(base62.encode(String.valueOf(number).getBytes()));
    }

    public long decode(String encoded) {
        return Long.parseLong(new String(base62.decode(encoded.getBytes())));
    }
}