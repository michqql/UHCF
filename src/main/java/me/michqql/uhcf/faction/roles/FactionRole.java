package me.michqql.uhcf.faction.roles;

import java.util.Locale;

public enum FactionRole {

    NONE(0), ENEMY(0, "&c"),
    TRUCE(1, "&d"), ALLY(2, "&a"),
    RECRUIT(10, "&b"), MEMBER(11, "&b"),
    ADMIN(99, "#2AD2D2"), LEADER(100, "&3");

    private final int weight;
    private final String prefix;

    FactionRole(int weight) {
        this.weight = weight;
        this.prefix = "&e";
    }

    FactionRole(int weight, String prefix) {
        this.weight = weight;
        this.prefix = prefix;
    }

    public int getWeight() {
        return weight;
    }

    public boolean hasPermission(FactionPermission permission) {
        return isHigherOrEqualRanking(permission.defaultRole);
    }

    public boolean isHigherOrEqualRanking(FactionRole other) {
        return this.weight >= other.weight;
    }

    public boolean isGuildRole() {
        return switch(this) {
            case RECRUIT, MEMBER, ADMIN, LEADER -> true;
            default -> false;
        };
    }

    public FactionRole getPromotion() {
        return switch(this) {
            case RECRUIT -> MEMBER;
            case MEMBER -> ADMIN;
            case ADMIN -> LEADER;
            default -> NONE;
        };
    }

    public FactionRole getDemotion() {
        return switch(this) {
            case MEMBER -> RECRUIT;
            case ADMIN -> MEMBER;
            case LEADER -> ADMIN;
            default -> NONE;
        };
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return prefix + getName();
    }

    public String getName() {
        String name = super.toString();
        return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
    }
}
