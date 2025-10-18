package com.saki.sakiaicodetoolsbackend.context;

import com.saki.sakiaicodetoolsbackend.model.entity.User;

/**
 * 用户上下文管理类。
 * <p>
 * 该类使用 ThreadLocal 来存储当前线程的用户信息，确保在多线程环境下用户数据的线程隔离。
 * 通过静态方法提供用户信息的设置、获取和清理功能，方便在业务逻辑中获取当前登录用户信息。
 * </p>
 * <p>
 * <strong>使用示例：</strong>
 * <pre>
 * {@code
 * // 设置当前用户
 * UserContext.setUser(currentUser);
 *
 * // 获取当前用户
 * User user = UserContext.getUser();
 *
 * // 清理用户上下文
 * UserContext.clear();
 * }
 * </pre>
 * </p>
 * <p>
 * <strong>注意事项：</strong>
 * <ul>
 *   <li>必须在请求处理完成后调用 {@link #clear()} 方法清理 ThreadLocal，避免内存泄漏</li>
 *   <li>该类设计为线程安全，每个线程拥有独立的用户上下文</li>
 *   <li>通常与拦截器或切面配合使用，自动管理用户上下文生命周期</li>
 * </ul>
 * </p>
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-18
 */
public class UserContext {

    /**
     * ThreadLocal 实例，用于存储当前线程的用户信息。
     * <p>
     * 每个线程都有自己独立的 {@link User} 对象副本，确保线程安全。
     * </p>
     */
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的用户信息。
     * <p>
     * 该方法将用户对象存储到当前线程的 ThreadLocal 中，后续在同一线程中可以通过 {@link #getUser()} 方法获取。
     * </p>
     *
     * @param user 当前登录的用户对象，不能为 null
     * @throws IllegalArgumentException 如果 user 参数为 null
     */
    public static void setUser(User user) {
        USER_HOLDER.set(user);
    }

    /**
     * 获取当前线程的用户信息。
     * <p>
     * 如果当前线程没有设置用户信息，则返回 null。
     * </p>
     *
     * @return 当前线程的用户对象，如果未设置则返回 null
     */
    public static User getUser() {
        return USER_HOLDER.get();
    }

    /**
     * 清理当前线程的用户信息。
     * <p>
     * 该方法移除当前线程的 ThreadLocal 中存储的用户信息，防止内存泄漏。
     * 在请求处理完成后必须调用此方法，通常在拦截器或切面的 finally 块中调用。
     * </p>
     */
    public static void clear() {
        USER_HOLDER.remove();
    }
}
