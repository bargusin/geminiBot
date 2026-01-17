package ru.rapidcoder.gemini.bot;

public class SettingsBot {

    private String botName;

    private String tokenId;

    private String encryptedKey;

    private String dataBaseFile;

    private String storagePath;

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public String getDataBaseFile() {
        return dataBaseFile;
    }

    public void setDataBaseFile(String dataBaseFile) {
        this.dataBaseFile = dataBaseFile;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
}
