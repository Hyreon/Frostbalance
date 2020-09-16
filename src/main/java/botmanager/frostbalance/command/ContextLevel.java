package botmanager.frostbalance.command;

public enum ContextLevel {

    ANY,
    PRIVATE_MESSAGE, //must be sent via direct message
    PUBLIC_MESSAGE, //must be sent inside a guild
    NATIONAL_MESSAGE, //must be done within your nation


}
