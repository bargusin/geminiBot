package ru.rapidcoder.gemini.bot.command;

public final class CommandId {

    private CommandId() { }

    public static final String START = "/start";
    public static final String CANCEL = "/cancel";
    public static final String BACK_TO_MAIN = "back_to_main";
    public static final String HELP = "/help";
    public static final String SETTINGS = "/settings";
    public static final String UNKNOWN = "unknownCommand";

    public static final String CHANGE_GEMINI_API_KEY_ID = "change_setting_gemini_api_key_id";
    public static final String CHANGE_QWEN_API_KEY_ID = "change_setting_qwen_api_key_id";

    public static final String MENU_GENERATOR_PICTURE = "menu_generator_picture";
    public static final String MENU_GENERATED_PICTURES = "menu_generated_pictures";
    public static final String SHOW_PICTURE = "picture";
    public static final String DOWNLOAD_ALL_PICTURES = "download_all_pictures";

    public static final String MENU_GENERATOR_VIDEO = "menu_generator_video";
    public static final String MENU_GENERATED_VIDEOS = "menu_generated_videos";
    public static final String SHOW_VIDEO = "video";
    public static final String DOWNLOAD_ALL_VIDEOS = "download_all_videos";
}
