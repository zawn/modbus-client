package org.modbus.jackson;

import java.util.LinkedHashMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author zhangzhenli
 */
public class JacksonTest {
    private static final Logger logger = LoggerFactory.getLogger(JacksonTest.class);

    @Test
    public void testJackson() throws JsonProcessingException {
        logger.debug("testJackson() called");
        ObjectMapper objectMapper = new ObjectMapper();

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("name", "zhangzhenli");
        map.put("gender", "male");
        String writeValueAsString = objectMapper.writeValueAsString(map);
        logger.debug(writeValueAsString);
    }
}
