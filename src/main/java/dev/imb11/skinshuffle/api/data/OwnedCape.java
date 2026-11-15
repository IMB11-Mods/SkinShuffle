package dev.imb11.skinshuffle.api.data;

/**
 * A cape owned by the current Minecraft account, as reported by the Minecraft services profile endpoint.
 *
 * @param id     The cape's unique id, used when activating it.
 * @param alias  The human-readable name, e.g. "Migrator".
 * @param url    The texture URL of the cape.
 * @param active Whether this cape is currently equipped on the account.
 */
public record OwnedCape(String id, String alias, String url, boolean active) {
}
