/*
 * Copyright (C) 2013 Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.shop.util.persistence;

import de.shop.util.ShopRuntimeException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public enum MimeType {
    JPEG(MimeType.JPEG_STR),
    PJPEG(MimeType.PJPEG_STR),
    PNG(MimeType.PNG_STR),
    MP4(MimeType.MP4_STR),
    WAV(MimeType.WAV_STR);
    
    private static final String JPEG_STR = "image/jpeg";
    private static final String PJPEG_STR = "image/pjpeg";
    private static final String PNG_STR = "image/png";
    private static final String MP4_STR = "video/mp4";
    private static final String WAV_STR = "audio/wav";
    
    private static final String WIRD_NICHT_UNTERSTUETZT = " wird nicht unterstuetzt";
    
    private static final Map<String, MimeType> NAME_CACHE = new HashMap<>();
    static {
        Arrays.stream(MimeType.values())
              .forEach(mimeType -> {
            NAME_CACHE.put(mimeType.value, mimeType);
            NAME_CACHE.put(mimeType.name(), mimeType);
        });
    }

    private final String value;

    private MimeType(String value) {
        this.value = value;
    }
    
    public static MimeType build(String value) {
        return NAME_CACHE.get(value);
    }
    
    public String getExtension() {
        switch (this) {
            case JPEG:  return "jpg";
            case PJPEG: return "jpg";
            case PNG:   return "png";
            case MP4:   return "mp4";
            default:    throw new ShopRuntimeException("Der MIME-Type " + this + WIRD_NICHT_UNTERSTUETZT);
        }
    }
    
    public static MimeType buildFromExtension(String extension) {
        switch (extension) {
            case "jpg":
            case "jpeg":
                return JPEG;
            case "png":
                return PNG;
            case "mp4":
                return MP4;
            default:
                throw new ShopRuntimeException("Die Extension " + extension + WIRD_NICHT_UNTERSTUETZT);
        }
    }
    
    public MultimediaType getMultimediaType() {
        if (value.startsWith("image/")) {
            return MultimediaType.IMAGE;
        }
        if (value.startsWith("video/")) {
            return MultimediaType.VIDEO;
        }
        if (value.startsWith("audio/")) {
            return MultimediaType.AUDIO;
        }
        
        throw new ShopRuntimeException("Der MultimediaType " + this + WIRD_NICHT_UNTERSTUETZT);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
