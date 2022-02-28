package me.michqql.uhcf.faction.roles;

public enum FactionPermission {

    MANAGE_CLAIMS(FactionRole.ADMIN),
    MANAGE_MEMBERS(FactionRole.ADMIN),
    MANAGE_RELATIONS(FactionRole.ADMIN),
    MANAGE_ROLES(FactionRole.ADMIN),
    START_RAIDS(FactionRole.ADMIN),

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
