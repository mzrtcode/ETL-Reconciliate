package co.com.itau.utils;

import org.springframework.batch.core.scope.context.ChunkContext;

public class ChunkContextUtil {

    private ChunkContextUtil() {}


    @SuppressWarnings("unchecked")
    public static <T> T getChunkContext(ChunkContext chunkContext, String key) {
        return (T) chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get(key);
    }

    public static void setChunkContext(ChunkContext chunkContext, String key, Object value) {
        chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put(key, value);
    }

}
