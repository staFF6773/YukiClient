package com.yukiclient.badge;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks which players on the current server are known to be using YukiClient.
 *
 * <p>The data is populated from the server-side companion plugin via plugin messages
 * and cleared whenever the local player disconnects.</p>
 */
public final class ClientBadgeManager {

    private static final Set<UUID> YUKI_USERS = Collections.synchronizedSet(new HashSet<UUID>());

    private ClientBadgeManager() {
    }

    /**
     * Clears all tracked users. Should be called on disconnect or new server connection.
     */
    public static void clear() {
        YUKI_USERS.clear();
    }

    /**
     * Marks a player as a YukiClient user.
     *
     * @param uuid the player's UUID.
     */
    public static void addUser(UUID uuid) {
        YUKI_USERS.add(uuid);
    }

    /**
     * Removes a player from the tracked set.
     *
     * @param uuid the player's UUID.
     */
    public static void removeUser(UUID uuid) {
        YUKI_USERS.remove(uuid);
    }

    /**
     * Replaces the tracked set with the provided collection.
     *
     * @param uuids the complete set of YukiClient users.
     */
    public static void setUsers(Set<UUID> uuids) {
        YUKI_USERS.clear();
        YUKI_USERS.addAll(uuids);
    }

    /**
     * Returns whether the given player is using YukiClient.
     *
     * @param uuid the player's UUID.
     * @return true if the player is a known YukiClient user.
     */
    public static boolean isUser(UUID uuid) {
        return YUKI_USERS.contains(uuid);
    }

    /**
     * @return an unmodifiable snapshot of the currently tracked users.
     */
    public static Set<UUID> getUsers() {
        return Collections.unmodifiableSet(new HashSet<UUID>(YUKI_USERS));
    }
}
