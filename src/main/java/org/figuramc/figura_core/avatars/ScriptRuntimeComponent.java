package org.figuramc.figura_core.avatars;

public interface ScriptRuntimeComponent<Self extends AvatarComponent<Self>> extends AvatarComponent<Self> {

    /**
     * Initialize the given runtime module, running its initializer code.
     * This should not be able to access any MC resources, or any other avatars.
     * If this requirement is upheld, we can run this phase on an off-thread!
     */
    void initModule(AvatarModules.RuntimeModule module) throws AvatarError;

}
