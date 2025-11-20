package com.url.shortener.models;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class ShortenerResponse implements Serializable
{
    @Serial
    private static final long serialVersionUID = -6915717598908337226L;

    private String url;
    private long createdAt;

}
