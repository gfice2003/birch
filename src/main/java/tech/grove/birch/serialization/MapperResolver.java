package tech.grove.birch.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface MapperResolver {
    ObjectMapper resolveFor(MapperMode mode);
}
