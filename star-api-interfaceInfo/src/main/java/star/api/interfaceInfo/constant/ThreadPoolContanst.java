package star.api.interfaceInfo.constant;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 16 日
 */
public interface ThreadPoolContanst {
    /**
     * 核心线程数
     */
    int CORE_POOL_SIZE = 3;

    /**
     * 最大线程数
     */
    int MAXIMUM_POOL_SIZE = 5;

    /**
     * 空闲时长
     */
    int KEEP_ALIVE_TIME = 60;

    /**
     * 阻塞队列容量
     */
    int QUEUE_CAPACITY = 10;
}
