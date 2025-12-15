package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.text.FormattedText;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A component which holds all the formatted text instances for use externally.
 */
public class ExternalText implements AvatarComponent<ExternalText> {
    protected final Avatar<?> self;
    public final Map<UUID, FormattedText> texts = new HashMap<>();

    public static Type<ExternalText> TYPE = new Type<>(ExternalText::new);

    public Type<ExternalText> getType() {
        return TYPE;
    }

    public ExternalText(Avatar<?> avatar, AvatarModules modules) {
        this.self = avatar;
    }
}
