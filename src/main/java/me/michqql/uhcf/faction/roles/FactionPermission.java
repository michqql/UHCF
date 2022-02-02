package me.michqql.uhcf.faction.roles;

public enum FactionPermission {

    CLAIM_LAND(FactionRole.ADMIN),
    UNCLAIM_LAND(FactionRole.ADMIN),
    INVITE_MEMBERS(FactionRole.ADMIN),
    KICK_MEMBERS(FactionRole.ADMIN),
    MANAGE_RELATIONS(FactionRole.ADMIN),
    MANAGE_ROLES(FactionRole.ADMIN),
    PERSONAL_CHEST_BYPASS(FactionRole.LEADER),
    PERSONAL_CHEST_CREATE(FactionRole.ADMIN),
    CHEST_ACCESS(FactionRole.MEMBER),
    BLOCK_ACCESS(FactionRole.MEMBER),
    INTERACTIONS(FactionRole.RECRUIT);

    final FactionRole defaultRole;

    FactionPermission(FactionRole defaultRole) {
        this.defaultRole = defaultRole;
    }

    FactionPermission() {
        this.defaultRole = FactionRole.LEADER;
    }

    public FactionRole getDefaultRole() {
        return defaultRole;
    }
}
