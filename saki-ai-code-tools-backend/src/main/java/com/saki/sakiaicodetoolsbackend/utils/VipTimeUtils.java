package com.saki.sakiaicodetoolsbackend.utils;

import java.time.LocalDateTime;

/**
 * VIP 时间计算工具类。
 * 支持自然月续费、到期时间自动对齐到凌晨 0 点。
 *
 * <p>示例：
 * <ul>
 *   <li>开通时间：2025-10-20 11:15:00</li>
 *   <li>到期时间：2025-11-21 00:00:00</li>
 * </ul>
 *
 * <p>底层基于 Java 8+ LocalDateTime API，自动处理闰月、月底越界等情况。
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-20
 */
public final class VipTimeUtils {

    private VipTimeUtils() {
    }

    /**
     * 计算 VIP 到期时间（精确到自然月 + 当日 0 点）。
     *
     * @param startTime 开始时间（一般为当前时间）
     * @param months    续费月数（默认 1）
     * @return 对齐到 0 点的到期时间
     */
    public static LocalDateTime calculateVipEndTime(LocalDateTime startTime, int months) {
        if (startTime == null) {
            throw new IllegalArgumentException("startTime 不能为空");
        }
        if (months <= 0) {
            throw new IllegalArgumentException("months 必须大于 0");
        }

        // 下个月同日 +1天 → 0点
        return startTime
                .plusMonths(months)
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    /**
     * 计算一个月 VIP 到期时间（便捷方法）。
     *
     * @param startTime 开始时间
     * @return 到期时间（次月同日 +1 天 后的 0 点）
     */
    public static LocalDateTime calculateVipEndTime(LocalDateTime startTime) {
        return calculateVipEndTime(startTime, 1);
    }

    /**
     * 判断当前时间是否已过期。
     *
     * @param vipEndTime 到期时间
     * @return true 表示已过期
     */
    public static boolean isVipExpired(LocalDateTime vipEndTime) {
        return vipEndTime != null && LocalDateTime.now().isAfter(vipEndTime);
    }
}
