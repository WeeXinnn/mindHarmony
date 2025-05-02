package my.utar.edu.mindharmony.userwellbeingtracking;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import my.utar.edu.mindharmony.R;


public class JournalEntryAdapter extends ArrayAdapter<MoodEntry> {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

    public JournalEntryAdapter(Context context, int resource, List<MoodEntry> entries) {
        super(context, resource, entries);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_journal_entry, parent, false);
        }

        MoodEntry entry = getItem(position);
        if (entry != null) {
            TextView dateView = convertView.findViewById(R.id.entryDate);
            TextView moodView = convertView.findViewById(R.id.entryMood);
            TextView notesView = convertView.findViewById(R.id.entryNotes);
            dateView.setText(dateFormat.format(new Date(entry.date)));
            moodView.setText(String.format("Mood: %s", entry.emoji));
            // Ensure full text is displayed
            notesView.setText(entry.journalText != null ? entry.journalText : entry.note != null ? entry.note : "");
            notesView.setMaxLines(Integer.MAX_VALUE); // Allow unlimited lines
            notesView.setEllipsize(null); // Disable truncation with "..."

        }

        return convertView;
    }
}
