package com.url.shortener.models;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@ToString
public class ShortenerRequest implements Serializable
{
    @Serial
    private static final long serialVersionUID = -5251859423398492563L;

    private String url;
}
