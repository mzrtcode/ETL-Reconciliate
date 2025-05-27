package com.itau.utils;

import org.springframework.batch.core.scope.context.ChunkContext;

public class ChunkContextUtil {

    private ChunkContextUtil() {}

    public static Object getChunkContext(ChunkContext chunkContext, String key) {
        return chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get(key);
    }

    public static void setChunkContext(ChunkContext chunkContext, String key, Object value) {
        chunkContext.getStepContext()
                .getStepExecution()
                .getExecutionContext()
                .put(key, value);
    }
}
