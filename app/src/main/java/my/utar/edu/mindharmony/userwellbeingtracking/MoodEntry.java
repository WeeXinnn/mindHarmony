package my.utar.edu.mindharmony.userwellbeingtracking;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "mood_entries")
public class MoodEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "date")
    public long date; // UTC timestamp in milliseconds

    @ColumnInfo(name = "timezone")
    private String timezone = "Asia/Kuala_Lumpur"; // Default to MYT

    @ColumnInfo(name = "rating")
    public int rating;

    @ColumnInfo(name = "emoji")
    public String emoji;

    @Ignore
    public String emojiName;

    @Ignore
    public int emojiRes;

    @ColumnInfo(name = "note")
    public String note;

    @ColumnInfo(name = "journal_text")
    public String journalText;

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone != null ? timezone : "Asia/Kuala_Lumpur";
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getEmojiName() {
        return emojiName;
    }

    public void setEmojiName(String emojiName) {
        this.emojiName = emojiName;
    }

    public int getEmojiRes() {
        return emojiRes;
    }

    public void setEmojiRes(int emojiRes) {
        this.emojiRes = emojiRes;
    }

    public String getJournalText() {
        return journalText;
    }

    public void setJournalText(String journalText) {
        this.journalText = journalText;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // Helper method to get formatted Malaysia time
    public String getFormattedMalaysiaTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone(timezone));
        return sdf.format(new java.util.Date(date));
    }
}