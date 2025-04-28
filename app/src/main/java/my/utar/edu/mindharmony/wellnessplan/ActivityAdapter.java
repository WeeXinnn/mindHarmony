package my.utar.edu.mindharmony.wellnessplan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import my.utar.edu.mindharmony.R;
import my.utar.edu.mindharmony.models.Activity;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<Activity> activityList = new ArrayList<>();
    private static final String PREFS_NAME = "UserPrefs";

    public void setActivityList(List<Activity> list) {
        this.activityList = list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activityList.get(position);
        Context context = holder.itemView.getContext();

        holder.title.setText(activity.getName());

        boolean isCompleted = hasCompletedToday(context, activity.getName());

        if (isCompleted) {
            holder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.pink));
        } else {
            holder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SingleActivity.class);
            intent.putExtra("activity_name", activity.getName());
            intent.putExtra("activity_desc", activity.getDescription());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        View container;
        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.activityTitle);
            container = itemView.findViewById(R.id.activityContainer);
        }
    }

    private boolean hasCompletedToday(Context context, String activityName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = "last_completed_date_" + activityName.replaceAll("\\s+", "_").toLowerCase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return today.equals(prefs.getString(key, ""));
    }

}